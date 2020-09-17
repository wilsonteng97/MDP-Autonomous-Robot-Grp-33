import cv2
import imagezmq
image_processing_server_url = 'tcp://169.254.243.236:5555'
image_sender = imagezmq.ImageSender(connect_to=image_processing_server_url)

img = cv2.imread('raw-images/id_14 (Y).jpg')
reply = image_sender.send_image(
                        'image from RPi',
                        img
                    )

reply = reply.decode('utf-8')

print(reply)
