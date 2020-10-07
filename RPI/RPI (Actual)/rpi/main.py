import os 
import argparse

parser = argparse.ArgumentParser(description='RPI Program')

parser.add_argument(
    '-i', 
    '--image_recognition', 
    choices=IMAGE_PROCESSING_SERVER_URLS.keys(),
    default=None,
)

def init():
    args = parser.parse_args()
    break
    
    
if __name__ = '__main__':
    init()