from split_image import ImageSplitter
import cv2
import imutils.face_utils.helpers

def init():
	print("Image Processing Server Started")
	try:
		imageSplitter = ImageSplitter()
		image = cv2.imread('captured_images/frame0.png')
		# image = imutils.rotate(image,180)
		# cv2.imshow("rotated", image)
		# cv2.imshow('raw image', image)
		# processed_image_id = imageDetector.start(image,'id_3 (Right arrow).jpg')
		# print("image id = ", processed_image_id)
		imageSplitter.start(image,"1|1|1|2|1|3|2|1|2|2|2|3",'captured_images/frame0.png',3,2)
	except KeyboardInterrupt:
		imageSplitter.end()


if __name__ == '__main__':
	init()
