import cv2
import time
import os

# image_path = 'raw-images/id_3 (Right arrow).jpg'
IMAGE_ENCODING = '.png'
# image = cv2.imread(image_path)
# image2 = image

# height, width, channels = image.shape
# print(image.shape)
# # Number of pieces Horizontally 
# CROP_W_SIZE  = 3 
# # Number of pieces Vertically to each Horizontal  
# CROP_H_SIZE = 1

# for ih in range(CROP_H_SIZE ):
#     for iw in range(CROP_W_SIZE ):

#         x = int(width/CROP_W_SIZE * iw )
#         y = int(height/CROP_H_SIZE * ih)
#         h = int((height / CROP_H_SIZE))
#         w = int((width / CROP_W_SIZE ))
#         print(x,y,h,w)
#         image = image[y:y+h, x:x+w]


#         NAME = image_path[:image_path.rfind(".")]+ "_crop_" + str(time.time()) + IMAGE_ENCODING
#         cv2.imwrite(NAME,image)
#         image = image2
class ImageSplitter:    
    def start(self,image,cdt,image_path,crop_w,crop_h):
        image2 = image
        height, width, channels = image.shape
        # print(image.shape)
        # Number of pieces Horizontally 
        CROP_W_SIZE  = crop_w 
        # Number of pieces Vertically to each Horizontal  
        CROP_H_SIZE = crop_h
        cdt = cdt.replace("|","")
        for ih in range(CROP_H_SIZE ):
            for iw in range(CROP_W_SIZE ):
                x = int(width/CROP_W_SIZE * iw )
                y = int(height/CROP_H_SIZE * ih)
                h = int((height / CROP_H_SIZE))
                w = int((width / CROP_W_SIZE ))
                # print(x,y,h,w)
                image = image[y:y+h, x:x+w]
                new_image_name = cdt[0] + "_" + cdt[1] + IMAGE_ENCODING
                # new_image_name = str(iw) + str(ih) + IMAGE_ENCODING
                new_image_path = 'captured_images/' + new_image_name
                print(new_image_path)
                save_success = cv2.imwrite(new_image_path,image)
                print('save image successful?', save_success)
                # print("image saved",cdt)
                image = image2
                cdt = cdt[2:]