from MultiProcessCommunication import MultiProcessCommunication
import time
def init():
	try:
		multi = MultiProcessCommunication()
		multi.start()
	except Exception as err:
		print('Main.py Error!: {}'.format(str(err)))

if __name__ == '__main__':
	init()
