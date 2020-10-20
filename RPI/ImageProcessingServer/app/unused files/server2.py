import os
import shutil
import sys
from datetime import datetime

import cv2
import imutils
import numpy as np
import tensorflow as tf
import pickle

from config import *
from image_receiver import imagezmq_custom as imagezmq
from utils import label_map_util
from utils import visualization_utils as vis_util
from detect_object_function import ImageDetector

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
            _,frame = self.image_hub.recv_image()
            print('Connected and received frame at time: ' + str(datetime.now()))
            frame = imutils.resize(frame, width=IMAGE_WIDTH)
            frame_expanded = np.expand_dims(frame, axis=0)

            #form image file path for saving
            raw_image_name = RAW_IMAGE_PREFIX + str(len(self.frame_list)) + IMAGE_ENCODING
            raw_image_path = os.path.join('captured_images', raw_image_name)
            # raw_image_path = os.path.join('C:\\\\Users\\xiaoqing\\Documents\\school stuff\\Year 3 Sem 1\\CZ3004 MDP\\MDP-Autonomous-Robot-Grp-33\\RPI\\Image-rec\\images',raw_image_name)
            # save raw image
            save_success = cv2.imwrite(raw_image_path, frame)

            print('Raw image name = {}'.format(raw_image_name))
            # import subprocess
            # cur_dif = os.getcwd()
            # detect_image_dir = os.path.join(cur_dif,'image_receiver')
            # # os.chdir('C:\\\\Users\\xiaoqing\\Documents\\school stuff\\Year 3 Sem 1\\CZ3004 MDP\\MDP-Autonomous-Robot-Grp-33\\RPI\\Image-rec')
            # os.chdir(detect_image_dir)
            # cmd = ['python', 'detect_object_rcnn_wbf.py', '--image', 'captured_images/{}'.format(raw_image_name)]
            # subprocess.run(cmd)

            imageDetector = ImageDetector()
            image_id = imageDetector.start(frame,raw_image_name)
            self.image_hub.send_reply(image_id)

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
