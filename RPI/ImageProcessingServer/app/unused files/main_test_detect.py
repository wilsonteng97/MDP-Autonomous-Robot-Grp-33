from detect_object_function import ImageDetector
import cv2
import imutils.face_utils.helpers

def init():
	print("Image Processing Server Started")
	try:
		imageDetector = ImageDetector()
		image = cv2.imread('raw-images/id_3 (Right arrow).jpg')
		image = imutils.rotate(image,180)
		cv2.imshow("rotated", image)
		# cv2.imshow('raw image', image)
		# processed_image_id = imageDetector.start(image,'id_3 (Right arrow).jpg')
		# print("image id = ", processed_image_id)
	except KeyboardInterrupt:
		imageDetector.end()


if __name__ == '__main__':
	init()
