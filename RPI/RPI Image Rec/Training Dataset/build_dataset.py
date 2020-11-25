import iou as iou_fn
import config
from bs4 import BeautifulSoup
from imutils import paths
import cv2
import os

# loop over output positive and negative directories
for directoryPath in (config.POSITVE_PATH, config.NEGATIVE_PATH):
	# create if output directory does not exist yet
	if not os.path.exists(directoryPath):
		os.makedirs(directoryPath)

# grab all image paths in the input images directory
imgPaths = list(paths.list_images(config.ORIGINAL_IMAGES))

# initialize total number of positive and negative images currently in disk
totalPositive = 0
totalNegative = 0

# loop over image paths
for (i, imgPath) in enumerate(imgPaths):
	# progress report
	print("Processing image {}/{}...".format(i + 1, len(imgPaths)))

	# extract filename from file path and derive path to XML annotation file
	filename = imgPath.split(os.path.sep)[-1]
	filename = filename[:filename.rfind(".")]
	annotationPath = os.path.sep.join([config.ORIGINAL_ANNOTATIONS, "{}.xml".format(filename)])

	# initialize list of ground-truth bounding boxes
	contents = open(annotationPath).read()
	soup = BeautifulSoup(contents, "html.parser")
	boundBoxes = []

	# extract image dimensions
	w = int(soup.find("width").string)
	h = int(soup.find("height").string)

	# loop over all 'object' elements
	for obj in soup.find_all("object"):
		# extract label and bounding box coordinates
		label = obj.find("name").string
		xMin = int(obj.find("xmin").string)
		yMin = int(obj.find("ymin").string)
		xMax = int(obj.find("xmax").string)
		yMax = int(obj.find("ymax").string)

		# truncate any bounding box coordinates that fall outside image boundary
		xMin = max(0, xMin)
		yMin = max(0, yMin)
		xMax = min(w, xMax)
		yMax = min(h, yMax)

		# update ground-truth bounding boxes list
		boundBoxes.append((xMin, yMin, xMax, yMax))

	# load input image from disk
	image = cv2.imread(imgPath)

	# run selective search
	selectiveSearch = cv2.ximgproc.segmentation.createSelectiveSearchSegmentation()
	selectiveSearch.setBaseImage(image)
	selectiveSearch.switchToSelectiveSearchFast()
	rects = selectiveSearch.process()
	proposedRects= []

	# loop over the rectangles generated by selective search
	for (x, y, w, h) in rects:
		proposedRects.append((x, y, x + w, y + h))

	# to count number of positive and negative region of interests saved
	positiveROIs = 0
	negativeROIs = 0

	# loop over max number of region proposals
	for proposedRect in proposedRects[:config.MAX_PROPOSALS_NO]:
		# unpack the proposed rectangle bounding box
		(proposedStartX, proposedStartY, proposedEndX, proposedEndY) = proposedRect

		# loop over the ground-truth bounding boxes
		for boundBox in boundBoxes:
			iou = iou_fn.calculate_iou(boundBox, proposedRect)
			(boundStartX, boundStartY, boundEndX, boundEndY) = boundBox

			roi = None
			outputPath = None

			if iou > 0.7 and positiveROIs <= config.MAX_POSITIVE_NO:
				# extract ROI and derive positive output path
				roi = image[proposedStartY:proposedEndY, proposedStartX:proposedEndX]
				filename = "{}.png".format(totalPositive)
				outputPath = os.path.sep.join([config.POSITVE_PATH, filename])

				# increment positive counters
				positiveROIs += 1
				totalPositive += 1

			fullOverlap = proposedStartX >= boundStartX
			fullOverlap = fullOverlap and proposedStartY >= boundStartY
			fullOverlap = fullOverlap and proposedEndX <= boundEndX
			fullOverlap = fullOverlap and proposedEndY <= boundEndY

			if not fullOverlap and iou < 0.05 and \
				negativeROIs <= config.MAX_NEGATIVE_NO:
				# extract ROI and derive negative output path
				roi = image[proposedStartY:proposedEndY, proposedStartX:proposedEndX]
				filename = "{}.png".format(totalNegative)
				outputPath = os.path.sep.join([config.NEGATIVE_PATH,
					filename])

				# increment negative counters
				negativeROIs += 1
				totalNegative += 1

			if roi is not None and outputPath is not None:
				# resize the ROI to the input dimensions of the CNN
				roi = cv2.resize(roi, config.INPUT_DIMENSIONS,
					interpolation=cv2.INTER_CUBIC)
				cv2.imwrite(outputPath, roi)