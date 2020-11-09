import config
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.applications import MobileNetV2
from tensorflow.keras.layers import AveragePooling2D
from tensorflow.keras.layers import Dropout
from tensorflow.keras.layers import Flatten
from tensorflow.keras.layers import Dense
from tensorflow.keras.layers import Input
from tensorflow.keras.models import Model
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.applications.mobilenet_v2 import preprocess_input
from tensorflow.keras.preprocessing.image import img_to_array
from tensorflow.keras.preprocessing.image import load_img
from tensorflow.keras.utils import to_categorical
from sklearn.preprocessing import LabelBinarizer
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report
from imutils import paths
import matplotlib.pyplot as plt
import numpy as np
import argparse
import pickle
import os

ap = argparse.ArgumentParser()
ap.add_argument("-p", "--plot", type=str, default="plot.png",
	help="path to output loss/accuracy plot")
args = vars(ap.parse_args())

INIT_LR = 1e-4
EPOCHS = 10
BS = 65

# grab the list of images in dataset
imgPaths = list(paths.list_images(config.BASE_PATH))
data = []
classLabels = []

# loop over image paths
for imgPath in imgPaths:
	# extract class label from the filename
	label = imgPath.split(os.path.sep)[-2]

	# preprocess input image
	image = load_img(imgPath, target_size=config.INPUT_DIMENSIONS)
	
	image = img_to_array(image)
	image = preprocess_input(image)

	data.append(image)
	classLabels.append(label)

data = np.array(data, dtype="float32")
classLabels = np.array(classLabels)

# one-hot encode classLabels
labelBin = LabelBinarizer()
classLabels = labelBin.fit_transform(classLabels)

(trainX, testX, trainY, testY) = train_test_split(data, classLabels,
	test_size=0.20, stratify=classLabels, random_state=42)

# construct training image generator for data augmentation
dataAug = ImageDataGenerator(
	rotation_range=20,
	zoom_range=0.15,
	width_shift_range=0.2,
	height_shift_range=0.2,
	shear_range=0.15,
	fill_mode="nearest")

# load the MobileNetV2 network leaving off head FC layer sets
modelBase = MobileNetV2(weights="imagenet", include_top=False,
	input_tensor=Input(shape=(224, 224, 3)))

# construct head of model
modelHead = modelBase.output
modelHead = AveragePooling2D(pool_size=(7, 7))(modelHead)
modelHead = Flatten(name="flatten")(modelHead)
modelHead = Dense(128, activation="relu")(modelHead)
modelHead = Dropout(0.5)(modelHead)
modelHead = Dense(16, activation="softmax")(modelHead)

# place head FC model on top of base model
model = Model(inputs=modelBase.input, outputs=modelHead)

# loop over all layers in the base model and freeze them
for layer in modelBase.layers:
	layer.trainable = False

# compile model
opt = Adam(lr=INIT_LR)
model.compile(loss="categorical_crossentropy", optimizer=opt, metrics=["accuracy"])

# train head
head = model.fit(
	dataAug.flow(trainX, trainY, batch_size=BS),
	steps_per_epoch=len(trainX) // BS,
	validation_data=(testX, testY),
	validation_steps=len(testX) // BS,
	epochs=EPOCHS)

# make predictions test set
predIndex = model.predict(testX, batch_size=BS)

# find index of the label with largest predicted probability
predIndex = np.argmax(predIndex, axis=1)

print(classification_report(testY.argmax(axis=1), predIndex,
	target_names=labelBin.classes_))

# save to disk
model.save(config.MODEL_PATH, save_format="h5")
f = open(config.ENCODER_PATH, "wb")
f.write(pickle.dumps(labelBin))
f.close()

# plot training loss and accuracy
N = EPOCHS
plt.style.use("ggplot")
plt.figure()
plt.plot(np.arange(0, N), head.history["loss"], label="train_loss")
plt.plot(np.arange(0, N), head.history["val_loss"], label="val_loss")
plt.plot(np.arange(0, N), head.history["accuracy"], label="train_acc")
plt.plot(np.arange(0, N), head.history["val_accuracy"], label="val_acc")
plt.title("Training Loss and Accuracy")
plt.xlabel("Epoch #")
plt.ylabel("Loss/Accuracy")
plt.legend(loc="lower left")
plt.savefig(args["plot"])