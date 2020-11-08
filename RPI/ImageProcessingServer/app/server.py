import os
import shutil
import sys
from datetime import datetime

import cv2
import imutils
import numpy as np
import tensorflow as tf
import pickle
import PIL
import glob
from PIL import Image
from math import ceil, floor

from image_receiver import imagezmq_custom as imagezmq

#packages for detect object
import config
import wbf
from tensorflow.keras.applications.mobilenet_v2 import preprocess_input
from tensorflow.keras.preprocessing.image import img_to_array
from tensorflow.keras.models import load_model
import timeit

IMAGE_ENCODING = '.png'
STOPPING_IMAGE = 'stop_image_processing.png'

IMAGE_WIDTH = 500  # 400
IMAGE_HEIGHT = 1080  # 225

RAW_IMAGE_PREFIX = 'frame'
PROCESSED_IMAGE_IDS = []
PROCESSED_IMAGE_PREFIX = 'processed'
sys.path.append("..")
cwd_path = os.getcwd()

class ImageProcessingServer:
    def __init__(self):
        self.image_hub = imagezmq.CustomImageHub()

        # load the our fine-tuned model and label binarizer from disk
        print("Loading model...")
        self.model = load_model(config.MODEL_PATH)
        lb = pickle.loads(open(config.ENCODER_PATH, "rb").read())

    def start(self):
        print('\nStarted image processing server.\n')
        while True:
            print('Waiting for image from RPi...')

            cdt,frame = self.image_hub.recv_image()

            if(cdt == "END"):
                # stitch images to show all identified obstacles
                self.stitch_images()
                print("Stitching Images...")
                print("Image Processing Server Ended")
                break
                
            print('Connected and received frame at time: ' + str(datetime.now()))

            frame = imutils.resize(frame, width=IMAGE_WIDTH)

            # form image file path for saving
            raw_image_name = cdt.replace(":","") + IMAGE_ENCODING
            raw_image_path = os.path.join('captured_images', raw_image_name)
            # save raw image
            save_success = cv2.imwrite(raw_image_path, frame)

            # split using bounding boxes
            cdt_list = list(cdt.split(":"))
            cut_width = 3
            cut_height = 3
            start_time = timeit.default_timer()
            reply = self.detect_image(self.model,frame,raw_image_name,cut_width,cut_height,cdt_list)
            print("Time to process: ", timeit.default_timer() - start_time)
            if not reply:
                self.image_hub.send_reply("None")
            else:
                self.image_hub.send_reply(str(reply))

    def detect_image(self, model, image, raw_image_name, cut_width, cut_height,cdt_list):
        # adjust brightness
        brightness = np.average(np.linalg.norm(image, axis=2)) / np.sqrt(3)
        while brightness > 125:
            hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
            hsv[...,2] = hsv[...,2]*0.9
            image = cv2.cvtColor(hsv, cv2.COLOR_HSV2BGR)
            brightness = np.average(np.linalg.norm(image, axis=2)) / np.sqrt(3)

        # run selective search on the image to generate bounding box proposal regions
        ss = cv2.ximgproc.segmentation.createSelectiveSearchSegmentation()
        ss.setBaseImage(image)
        ss.switchToSingleStrategy()
        rects = ss.process()

        # initialize the list of region proposals to classify and their respective bounding boxes
        proposals = []
        boxes = []

        # loop over the region proposal bounding box coordinates
        for (x, y, w, h) in rects[:config.MAX_PROPOSALS_INFER]:
            # extract the region from the input image, convert it from BGR to RGB channel ordering
            roi = image[y:y + h, x:x + w]
            roi = cv2.cvtColor(roi, cv2.COLOR_BGR2RGB)
            # resize input image to the input dimensions of our trained CNN
            roi = cv2.resize(roi, config.INPUT_DIMS,
                interpolation=cv2.INTER_CUBIC)
            # further preprocess by the ROI
            roi = img_to_array(roi)
            roi = preprocess_input(roi)
            #normalize box coordinates
            x_2 = (x+w)/image.shape[1]
            y_2 = (y+h)/image.shape[0]
            x = x/image.shape[1]
            y = y/image.shape[0]

            # update our proposals and bounding boxes lists
            proposals.append(roi)
            boxes.append((x, y, x_2, y_2))

        # convert the proposals and bounding boxes into numpy arrays
        proposals = np.array(proposals, dtype="float32")
        boxes = np.array(boxes, dtype="float32")

        # classify each of the proposal ROIs using fine-tuned model
        proba = model.predict(proposals)

        # find the index of predictions that are positive for the classes
        indexes = np.where(proba[:,:15]>config.MIN_PROBA)[0]

        # use the indexes to extract all bounding boxes with associated class and probabilities
        boxes = boxes[indexes]
        proba = proba[indexes][:, :15]

        # get labels and scores of each bounding box
        labels = [0]*len(boxes)
        for i in range(len(boxes)):
            for j in range(15):
                if proba[i][j]>config.MIN_PROBA:
                    labels[i]=j+1
        scores = [0]*len(boxes)
        for i in range(len(boxes)):
            for j in range(15):
                if proba[i][j]>config.MIN_PROBA:
                    if proba[i][j] > scores[i]:
                        scores[i] = proba[i][j]

        boxes_list = [boxes]
        scores_list = [scores]
        labels_list = [labels]
        reply_list = []
        boxes, scores, labels = wbf.weighted_boxes_fusion(boxes_list, scores_list, labels_list)

        for i in range(len(boxes)):
            boxes[i][0]=boxes[i][0]*image.shape[1]
            boxes[i][1]=boxes[i][1]*image.shape[0]
            boxes[i][2]=boxes[i][2]*image.shape[1]
            boxes[i][3]=boxes[i][3]*image.shape[0]
        boxes = np.array(boxes, dtype="int32")
        for i in range(len(boxes)):
            # draw the bounding box and label with class id and coordinates
            (startX, startY, endX, endY) = boxes[i]
            cv2.rectangle(image, (startX, startY), (endX, endY),
                (0, 255, 0), 2)
            y = startY - 10 if startY - 10 > 10 else startY + 10
            text = str(labels[i])
            box_width = abs(startX-endX)
            box_height = abs(startY-endY)
            # split image into 3 and add coordinates to text
            for w in range(cut_width):
                w = w+1
                section_width = float(image.shape[1])/cut_width*w
                if startX<section_width:
                    if (box_width/2)<(section_width - startX):
                        if cdt_list[2*w-1]=="-1":
                            text = ""
                            break
                        else:
                            text = text + ", (" + cdt_list[2*w-2] + ", " + cdt_list[2*w-1] + ")"
                            break
            if len(text)!=0:
                cv2.putText(image, text, (startX, y),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.45, (0, 255, 0), 2)
                # only add return image id to rpi if image id has not been detected before    
                if labels[i] not in PROCESSED_IMAGE_IDS:
                    reply_list.append(text)
                    PROCESSED_IMAGE_IDS.append(labels[i])
        if len(reply_list)!=0:
            processed_image_path = 'processed_images/' + raw_image_name[:raw_image_name.rfind(".")] + "_processed" + IMAGE_ENCODING
            save_success = cv2.imwrite(processed_image_path, image)
        return reply_list

    def stitch_images(self):
        frame_width = 1920
        images_per_row = 5
        padding = 0

        # read processed_images from disk
        os.chdir('processed_images')
        images = glob.glob("*.png")

        img_width, img_height = Image.open(images[0]).size
        # set scaling factor
        scale_factor = (frame_width-(images_per_row-1)*padding)/(images_per_row*img_width)
        scaled_img_width = ceil(img_width*scale_factor)
        scaled_img_height = ceil(img_height*scale_factor)

        number_of_rows = ceil(len(images)/images_per_row)
        frame_height = ceil(scale_factor*img_height*number_of_rows)

        new_img = Image.new('RGB', (frame_width, frame_height))

        i,j=0,0
        for num, img in enumerate(images):
            if num%images_per_row==0:
                i=0
            img = Image.open(img)
            # resize image
            img.thumbnail((scaled_img_width,scaled_img_height)) 
            y_cord = (j//images_per_row)*scaled_img_height
            new_img.paste(img, (i,y_cord))
            i=(i+scaled_img_width)+padding
            j+=1
        new_img.save("stitched_output.png", "PNG", quality=80, optimize=True, progressive=True)
        os.chdir("..")