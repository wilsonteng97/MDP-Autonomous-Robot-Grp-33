import cv2
import imagezmq
import socket

image_processing_server_url = 'tcp://192.168.33.96:5555'
image_sender = imagezmq.ImageSender(connect_to=image_processing_server_url)

print("Loading image...")
img = cv2.imread(r'C:\\\\Users\\xiaoqing\\Documents\\school stuff\\Year 3 Sem 1\\CZ3004 MDP\\image-rec-2\\images\\id_13.jpg')
print("Image loaded!")
rpi_name = socket.gethostname()
print(rpi_name)
reply = image_sender.send_image(
                        rpi_name,
                        img
                    )
print("Image sent!")
reply = reply.decode('utf-8')

print(reply)
