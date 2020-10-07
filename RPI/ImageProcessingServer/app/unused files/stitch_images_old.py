import cv2
import os
from imutils import paths
from imutils import resize
from pyimagesearch import config
import sys
from PIL import Image

# grab all image paths in the output images directory
imagePaths = list(paths.list_images(config.OUTPUT_PATH))
images = [Image.open(x) for x in imagePaths]
widths, heights = zip(*(i.size for i in images))

total_width = sum(widths)
max_height = max(heights)

new_im = Image.new('RGB', (total_width, max_height))

x_offset = 0
for im in images:
  new_im.paste(im, (x_offset,0))
  x_offset += im.size[0]

new_im.save('stitched_output.jpg')