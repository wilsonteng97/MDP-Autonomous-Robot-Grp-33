import cv2
import time

image_path = 'raw-images/id_3 (Right arrow).jpg'
IMAGE_ENCODING = '.png'
image = cv2.imread(image_path)
image2 = image

height, width, channels = image.shape
print(image.shape)
# Number of pieces Horizontally 
CROP_W_SIZE  = 3 
# Number of pieces Vertically to each Horizontal  
CROP_H_SIZE = 1

for ih in range(CROP_H_SIZE ):
    for iw in range(CROP_W_SIZE ):

        x = int(width/CROP_W_SIZE * iw )
        y = int(height/CROP_H_SIZE * ih)
        h = int((height / CROP_H_SIZE))
        w = int((width / CROP_W_SIZE ))
        print(x,y,h,w)
        image = image[y:y+h, x:x+w]


        NAME = image_path[:image_path.rfind(".")]+ "_crop_" + str(time.time()) + IMAGE_ENCODING
        cv2.imwrite(NAME,image)
        image = image2