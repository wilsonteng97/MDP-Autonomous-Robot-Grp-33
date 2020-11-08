import os

# base path to input dataset and derive image and annotations directories
ORIG_BASE_PATH = "train"
ORIG_IMAGES = os.path.sep.join([ORIG_BASE_PATH, "images"])
ORIG_ANNOTS = os.path.sep.join([ORIG_BASE_PATH, "annotations"])

# base path to new dataset after running build_dataset and derive paths to output directories
BASE_PATH = "dataset+"
POSITVE_PATH = os.path.sep.join([BASE_PATH, "02_down"])
NEGATIVE_PATH = os.path.sep.join([BASE_PATH, "negatives"])

# number of max proposals used when running selective search
MAX_PROPOSALS = 2000

# max number of positive and negative images to be generated from one image
MAX_POSITIVE = 30
MAX_NEGATIVE = 10

# input dimensions to CNN network
INPUT_DIMS = (224, 224)

# define path to model and label binarizer
MODEL_PATH = "image_detector_new2.h5"
ENCODER_PATH = "label_encoder_new2.pickle"