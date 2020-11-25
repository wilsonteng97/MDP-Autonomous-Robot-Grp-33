from server import ImageProcessingServer

def init():
	print("Image Processing Server Started")
	try:
		server = ImageProcessingServer()
		server.start()
	except KeyboardInterrupt:
		server.end()


if __name__ == '__main__':
	init()
