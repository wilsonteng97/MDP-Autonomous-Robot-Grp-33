from pyimagesearch.iou import compute_iou
from pyimagesearch import config
from bs4 import BeautifulSoup
from imutils import paths
import cv2
import os
import pandas as pd

imagePaths = list(paths.list_images(config.ORIG_BASE_PATH))

# loop over the image paths
for (i, imagePath) in enumerate(imagePaths):
	# show a progress report
	print("[INFO] processing image {}/{}...".format(i + 1,
		len(imagePaths)))

	# extract the filename from the file path and use it to derive
	# the path to the XML annotation file
	filename = imagePath.split(os.path.sep)[-1]
	# filename = filename[:filename.rfind(".")]
	print(filename)
	annotPath = pd.read_csv(config.ORIG_BASE_PATH+"_labels.csv")
	gtBoxes = []
	filename2 = annotPath['filename'] == filename
	annotPath = annotPath[filename2]
	xMin = int(annotPath['xmin'].values[0])
	print(type(xMin))
	gtBoxes.append(xMin)
	break
print(gtBoxes)