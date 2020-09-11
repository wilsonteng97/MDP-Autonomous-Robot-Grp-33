from MultiProcessCommunication import MultiProcessCommunication
import time

def init():
	try:
		multi = MultiProcessCommunication()
		multi.start()
		while True:
			pass
#		multi.testing_arduino
	except Exception as err:
		print('Main.py Error!: {}'.format(str(err)))

if __name__ == '__main__':
	init()
