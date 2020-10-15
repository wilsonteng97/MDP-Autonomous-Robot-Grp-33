# from split_image import ImageSplitter
from detect_object_function import ImageDetector
import cv2
import imutils.face_utils.helpers
from pyimagesearch import config
from tensorflow.keras.applications.mobilenet_v2 import preprocess_input
from tensorflow.keras.preprocessing.image import img_to_array
from tensorflow.keras.models import load_model
import pickle
import time

def init():
	print("Image Processing Server Started")
	try:
		# imageSplitter = ImageSplitter()
		imageDetector = ImageDetector()
		start_time = time.time()
		print("[INFO] loading model and label binarizer...")
		model = load_model(config.MODEL_PATH)
		lb = pickle.loads(open(config.ENCODER_PATH, "rb").read())
		print("model loaded at", print(time.time()-start_time))
#		image1 = cv2.imread('raw-images/seven_06.png')
#		image2 = cv2.imread('raw-images/out.jpg')
		image1 = cv2.imread('raw-images\id_5 (Go).jpg')
		image2 = cv2.imread('raw-images\id_6 (6).jpg')
		detect_time = time.time()
		reply = imageDetector.start(model, image1, "seven_06.png",3,1)
		print("image1 and cdts: ", reply, ", time done: ",time.time()-detect_time)
		reply2 = imageDetector.start(model, image2, "out.jpg",5,3)
		print("image2 and cdts: ", reply, ", time done: ",time.time()-detect_time)

	except KeyboardInterrupt:
		# imageSplitter.end()
		imageDetector.end()


if __name__ == '__main__':
	init()
