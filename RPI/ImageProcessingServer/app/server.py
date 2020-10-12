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

from config import *
from image_receiver import imagezmq_custom as imagezmq
from utils import label_map_util
from utils import visualization_utils as vis_util

#packages for detect object
from pyimagesearch import wbf
from pyimagesearch import config
from tensorflow.keras.applications.mobilenet_v2 import preprocess_input
from tensorflow.keras.preprocessing.image import img_to_array
from tensorflow.keras.models import load_model

MODEL_NAME = 'model'
INFERENCE_GRAPH = 'frozen_inference_graph.pb'
LABEL_MAP = 'labelmap.pbtxt'

# Number of classes the object detector can identify
NUM_CLASSES = 15

IMAGE_ENCODING = '.png'
STOPPING_IMAGE = 'stop_image_processing.png'

MAX_NUM_SYMBOLS = 3

IMAGE_WIDTH = 1920  # 400
IMAGE_HEIGHT = 1080  # 225

DISPLAY_IMAGE_WIDTH = 400

# red colour symbols tend to have lower confidence scores
MIN_CONFIDENCE_THRESHOLD = 0.50

# usually for non-red symbols, confidence of > 90%.
# however, once in a blue moon, confidence score may drop very low.
# no false positive with confidence higher than 70% though
# therefore, set confidence score this low
NON_RED_CONFIDENCE_THRESHOLD = 0.70

# used for filtering symbols that are 5 grids away
# sitution: [S]    [ ]  <R
# where [ ] be obstacle,
#       S be symbol
#       R be robot
#       < be camera direction
# 3 grids - extreme case (correct): ~750
# 5 grids (wrong; as shown in situation): ~695
YMAX_THRESHOLD = 775

SYMBOL_ON_LEFT_OF_IMAGE_THRESHOLD = 780  # left xmax compared to middle xmin
SYMBOL_ON_RIGHT_OF_IMAGE_THRESHOLD = 1090  # right xmin compared to middle xmax

MAIN_IMAGE_DIR = 'frames'
RAW_IMAGE_DIR = 'raw'
PROCESSED_IMAGE_DIR = 'processed'

RAW_IMAGE_PREFIX = 'frame'
PROCESSED_IMAGE_PREFIX = 'processed'

DISPLAY_DURATION_MILLISECONDS = 3000

LEFT_OBSTACLE = 'left_obstacle'
MIDDLE_OBSTACLE = 'middle_obstacle'
RIGHT_OBSTACLE = 'right_obstacle'

NO_SYMBOL = '-1'


sys.path.append("..")
cwd_path = os.getcwd()


class ImageProcessingServer:
    def __init__(self):
        self.image_hub = imagezmq.CustomImageHub()
        self.inference_graph_path = os.path.join(cwd_path, MODEL_NAME, INFERENCE_GRAPH)
        self.labels_path = os.path.join(cwd_path, MODEL_NAME, LABEL_MAP)

        # Load the label map.
        #
        # Label maps map indices to category names, so that when our convolution
        # network predicts `0`, we know that this corresponds to `white up arrow`.
        #
        # Here we use internal utility functions, but anything that returns a
        # dictionary mapping integers to appropriate string labels would be fine

        label_map = label_map_util.load_labelmap(self.labels_path)
        categories = label_map_util.convert_label_map_to_categories(
            label_map,
            max_num_classes=NUM_CLASSES,
            use_display_name=True
        )

        self.category_index = label_map_util.create_category_index(categories)

        # Load the Tensorflow model into memory.
        detection_graph = tf.Graph()

        with detection_graph.as_default():
            od_graph_def = tf.compat.v1.GraphDef()

            with tf.io.gfile.GFile(self.inference_graph_path, 'rb') as fid:
                serialized_graph = fid.read()
                od_graph_def.ParseFromString(serialized_graph)
                tf.import_graph_def(od_graph_def, name='')

            self.session = tf.compat.v1.Session(graph=detection_graph)

        # Define input and output tensors (i.e. data) for the object detection classifier

        # Input tensor is the image
        self.image_tensor = detection_graph.get_tensor_by_name('image_tensor:0')

        # Output tensors are the detection bounding_boxes, scores, and classes
        # Each box represents a part of the image where a particular object was detected

        # Each score represents level of confidence for each of the objects.
        # The score is shown on the result image, together with the class label.

        # Number of objects detected is also given
        self.session_params = [
            detection_graph.get_tensor_by_name('detection_boxes:0'),
            detection_graph.get_tensor_by_name('detection_scores:0'),
            detection_graph.get_tensor_by_name('detection_classes:0'),
            detection_graph.get_tensor_by_name('num_detections:0'),
        ]


        #self._initialise_directories()
        self.frame_list = []

    def _initialise_directories():
        image_dir_path = os.path.join(cwd_path, MAIN_IMAGE_DIR)

    def start(self):
        print('\nStarted image processing server.\n')
        while True:
            print('Waiting for image from RPi...')
            cdt,frame = self.image_hub.recv_image()
            print('Connected and received frame at time: ' + str(datetime.now()))
            # print("image coordinates: ", cdt)
            frame = imutils.resize(frame, width=IMAGE_WIDTH)
            frame_expanded = np.expand_dims(frame, axis=0)

            #form image file path for saving
            # raw_image_name = RAW_IMAGE_PREFIX + str(len(self.frame_list)) + IMAGE_ENCODING
            raw_image_name = cdt + IMAGE_ENCODING
            raw_image_path = os.path.join('captured_images', raw_image_name)
            # save raw image
            save_success = cv2.imwrite(raw_image_path, frame)

            # split images
            # need to find out how to get crop width and crop height numbers
            # cdt_list = list(cdt.split("|"))
            # split_no = len(cdt_list)/2
            # self.split_image(frame,cdt_list,crop_width=1,crop_height=1)
            # for i in range(split_no):
            #     split_image_name = cdt_list[0] + "_" + cdt_list[1] + IMAGE_ENCODING
            #     split_image = cv2.imread('captured_image/'+split_image_name)
            #     image_id = self.detect_image(split_image,split_image_name)	
            #     if image_id == 0:
            #         self.image_hub.send_reply("None")
            #     else:
            #         reply = str(image_id) + "|" + cdt_list[0] + "|" + cdt_list[1]
            #         self.image_hub.send_reply(reply)
            #     cdt_list = cdt_list[2:0]                

            # split using bounding boxes
            reply = self.detect_image(frame,raw_image_name,cut_width=3,cut_height=3)	
            if reply == 0:
                self.image_hub.send_reply("None")
            else:
                self.image_hub.send_reply(reply)
            
            # to add in when image sending ends
            # self.stitch_images()

    def _get_true_positives(self, bbox_list, class_list, score_list):
            """
            params:
            - bbox_list (list): [
                [top_left_y (float), top_left_x (float), bot_right_y (float), bot_right_x (float)],
                ...,
            ]
            - class_list (list): [class_id (int), ]
            - score_list (list): [confidence_score (float)]
            return: (
                { LEFT_OBSTACLE: SYMBOL, MIDDLE_OBSTACLE: SYMBOL, RIGHT_OBSTACLE: SYMBOL },
                true positive bounding boxes (list),
                true positive classes (list),
                true positive confidence scores (list),
            )
            """
            bounding_boxes, classes, scores = [], [], []

            # -1 means no detection for that obstacle
            obstacle_symbol_map = {
                LEFT_OBSTACLE: NO_SYMBOL,
                MIDDLE_OBSTACLE: NO_SYMBOL,
                RIGHT_OBSTACLE: NO_SYMBOL,
            }

            num_symbols = 0

            left_xmax = float('-inf')
            right_xmin = float('inf')

            for bbox, class_id, score in zip(bbox_list, class_list, score_list):
                if num_symbols >= 3:
                    break

                top_left_y, top_left_x, bot_right_y, bot_right_x = tuple(bbox)

                top_left_y = top_left_y * IMAGE_HEIGHT
                top_left_x = top_left_x * IMAGE_WIDTH
                bot_right_y = bot_right_y * IMAGE_HEIGHT
                bot_right_x = bot_right_x * IMAGE_WIDTH

                not_red = class_id != 2 and class_id != 8 and class_id != 11

                # false positive if:
                # confidence score is lower than a generic threshold (for all classes)
                # confidence score is lower than a higher threshold (for non-reds)
                # the bottom y-coordinate is lower than its repective threshold (too far)
                if ((score <= MIN_CONFIDENCE_THRESHOLD)
                    or (not_red and score < NON_RED_CONFIDENCE_THRESHOLD) \
                    or (bot_right_y < YMAX_THRESHOLD) \
                    ):
                    continue  # false positive -> skip

                if (bot_right_x < SYMBOL_ON_LEFT_OF_IMAGE_THRESHOLD):  # symbol left
                    # obstacle already has a symbol of higher confidence,
                    # and is directly to the left of middle
                    if obstacle_symbol_map[LEFT_OBSTACLE] != NO_SYMBOL and bot_right_x < left_xmax:
                        continue

                    left_xmax = bot_right_x
                    obstacle_symbol_map[LEFT_OBSTACLE] = str(class_id)

                elif (top_left_x  > SYMBOL_ON_RIGHT_OF_IMAGE_THRESHOLD):  # symbol right
                    # obstacle already has a symbol of higher confidence,
                    # and is directly to the right of middle
                    if obstacle_symbol_map[RIGHT_OBSTACLE] != NO_SYMBOL and top_left_x > right_xmin:
                        continue

                    right_xmin = top_left_x
                    obstacle_symbol_map[RIGHT_OBSTACLE] = str(class_id)

                else:  # symbol middle
                    # obstacle already has a symbol of higher confidence
                    if obstacle_symbol_map[MIDDLE_OBSTACLE] != NO_SYMBOL:
                        continue

                    obstacle_symbol_map[MIDDLE_OBSTACLE] = str(class_id)

                bounding_boxes.append(bbox)
                classes.append(class_id)
                scores.append(score)

                print(
                    'id: ', class_id,
                    'confidence: ', '{:.3f}'.format(score),
                    '\n',
                    'xmin: ', '{:.3f}'.format(top_left_x),
                    'xmax: ', '{:.3f}'.format(bot_right_x),
                    'ymax: ', '{:.3f}'.format(bot_right_y),
                    '\n',
                )

                num_symbols += 1

            return obstacle_symbol_map, bounding_boxes, classes, scores
    def detect_image(self, image, raw_image_name, cut_width, cut_height):
        # load the our fine-tuned model and label binarizer from disk
        print("[INFO] loading model and label binarizer...")
        model = load_model(config.MODEL_PATH)
        lb = pickle.loads(open(config.ENCODER_PATH, "rb").read())

        # resize and rotate image
        image = imutils.resize(image, width=500)
        image = imutils.rotate(image, 180)
        brightness = np.average(np.linalg.norm(image, axis=2)) / np.sqrt(3)
        while brightness > 125:
            hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
            hsv[...,2] = hsv[...,2]*0.9
            image = cv2.cvtColor(hsv, cv2.COLOR_HSV2BGR)
            brightness = np.average(np.linalg.norm(image, axis=2)) / np.sqrt(3)

        # run selective search on the image to generate bounding box proposal regions
        print("[INFO] running selective search...")
        ss = cv2.ximgproc.segmentation.createSelectiveSearchSegmentation()
        ss.setBaseImage(image)
        ss.switchToSelectiveSearchFast()
        rects = ss.process()

        # initialize the list of region proposals that we'll be classifying
        # along with their associated bounding boxes
        proposals = []
        boxes = []

        # loop over the region proposal bounding box coordinates generated by
        # running selective search
        for (x, y, w, h) in rects[:config.MAX_PROPOSALS_INFER]:
            # extract the region from the input image, convert it from BGR to
            # RGB channel ordering, and then resize it to the required input
            # dimensions of our trained CNN
            roi = image[y:y + h, x:x + w]
            roi = cv2.cvtColor(roi, cv2.COLOR_BGR2RGB)
            roi = cv2.resize(roi, config.INPUT_DIMS,
                interpolation=cv2.INTER_CUBIC)
            # further preprocess by the ROI
            roi = img_to_array(roi)
            roi = preprocess_input(roi)

            # update our proposals and bounding boxes lists
            proposals.append(roi)
            boxes.append((x, y, x + w, y + h))
            
            
        # convert the proposals and bounding boxes into NumPy arrays
        proposals = np.array(proposals, dtype="float32")
        boxes = np.array(boxes, dtype="float32")
        print("[INFO] proposal shape: {}".format(proposals.shape))

        # classify each of the proposal ROIs using fine-tuned model
        print("[INFO] classifying proposals...")
        proba = model.predict(proposals)

        # find the index of all predictions that are positive for the classes
        print("[INFO] applying NMS...")
        idxs = np.where(proba[:,:15]>config.MIN_PROBA)[0]

        # use the indexes to extract all bounding boxes and associated class
        # label probabilities associated with the class
        boxes = boxes[idxs]
        proba = proba[idxs][:, :15]

        for i in range(len(boxes)):
            boxes[i][0]=boxes[i][0]/image.shape[1]
            boxes[i][1]=boxes[i][1]/image.shape[0]
            boxes[i][2]=boxes[i][2]/image.shape[1]
            boxes[i][3]=boxes[i][3]/image.shape[0]
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
            # draw the bounding box, label, and probability on the image
            (startX, startY, endX, endY) = boxes[i]
            cv2.rectangle(image, (startX, startY), (endX, endY),
                (0, 255, 0), 2)
            y = startY - 10 if startY - 10 > 10 else startY + 10
            text = str(labels[i])
            box_width = abs(startX-endX)
            box_height = abs(startY-endY)
            for w in range(cut_width):
                w = w+1
                section_width = float(image.shape[1])/cut_width*w
                # print(section_width)
                if startX<section_width:
                    if (box_width/2)<(section_width - startX):
                        text = text + ", (" + str(w) + ", "
                        break
            for h in range(cut_height):
                h = h+1
                section_height = float(image.shape[0])/cut_height*h
                # print(section_height)
                if startY<section_height:
                    if (box_height/2)<(section_height - startY):
                        text = text + str(h) + ")"
                        break
            cv2.putText(image, text, (startX, y),
                cv2.FONT_HERSHEY_SIMPLEX, 0.45, (0, 255, 0), 2)
            reply_list.append(text)
        if len(reply_list)!=0:
            # save output image
            processed_image_path = 'processed_images/' + raw_image_name[:raw_image_name.rfind(".")] + "_processed" + IMAGE_ENCODING
            # save processed image
            save_success = cv2.imwrite(processed_image_path, image)
            print('save image successful?', save_success)
            return reply_list
        else:
            return 0

    def split_image(self,image,cdt,crop_w,crop_h):
        image2 = image
        height, width, channels = image.shape
        # Number of pieces Horizontally 
        CROP_W_SIZE  = crop_w 
        # Number of pieces Vertically to each Horizontal  
        CROP_H_SIZE = crop_h
        for ih in range(CROP_H_SIZE ):
            for iw in range(CROP_W_SIZE ):
                x = int(width/CROP_W_SIZE * iw )
                y = int(height/CROP_H_SIZE * ih)
                h = int((height / CROP_H_SIZE))
                w = int((width / CROP_W_SIZE ))
                image = image[y:y+h, x:x+w]
                new_image_name = cdt[0] + "_" + cdt[1] + IMAGE_ENCODING
                new_image_path = 'captured_images/' + new_image_name
                save_success = cv2.imwrite(new_image_path,image)
                print('save image successful?', save_success)
                image = image2
                cdt = cdt[2:]

    def stitch_images(self):
        frame_width = 1920
        images_per_row = 5
        padding = 0

        os.chdir('processed_images')
        images = glob.glob("*.png")

        img_width, img_height = Image.open(images[0]).size
        sf = (frame_width-(images_per_row-1)*padding)/(images_per_row*img_width)       #scaling factor
        scaled_img_width = ceil(img_width*sf)                   #s
        scaled_img_height = ceil(img_height*sf)

        number_of_rows = ceil(len(images)/images_per_row)
        frame_height = ceil(sf*img_height*number_of_rows) 

        new_im = Image.new('RGB', (frame_width, frame_height))

        i,j=0,0
        for num, im in enumerate(images):
            if num%images_per_row==0:
                i=0
            im = Image.open(im)
            #Here I resize my opened image, so it is no bigger than 100,100
            im.thumbnail((scaled_img_width,scaled_img_height))
            #Iterate through a 4 by 4 grid with 100 spacing, to place my image
            y_cord = (j//images_per_row)*scaled_img_height
            new_im.paste(im, (i,y_cord))
            print(i, y_cord)
            i=(i+scaled_img_width)+padding
            j+=1

        new_im.save("stitched_output.png", "PNG", quality=80, optimize=True, progressive=True)
        os.chdir("..")