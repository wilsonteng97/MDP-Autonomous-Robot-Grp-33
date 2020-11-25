from multiprocessing import Process, Value, Queue, Manager

from Algorithms import Algorithm
from Arduino import Arduino
from Android import Android

from socket import error as SocketError

from picamera import PiCamera
import socket
import cv2
import imagezmq
from imutils.video import VideoStream

ANDROID_HEADER = 'AND'.encode()
ARDUINO_HEADER = 'ARD'.encode()
ALGORITHM_HEADER = 'ALG'.encode()

NEWLINE = '\n'.encode()

#Algo To Android
MOVE_FORWARD_AND = '{m:W'.encode()
TURN_LEFT_AND = '{m:A'.encode()
TURN_RIGHT_AND = '{m:D'.encode()

MDF_STRING = 'M'.encode()[0]

#Algo to RPI
TAKE_PICTURE = 'P'.encode()[0]
EXPLORATION_COMPLETE = 'N'.encode()
FASTEST_PATH = 'K'.encode()[0]

#Image Rec IP addresses
image_processing_server_url = 'tcp://192.168.33.217:5555' #Wei Xuan
#image_processing_server_url = 'tcp://192.168.33.96:5555' #Xiao Qing
#image_processing_server_url = 'tcp://192.168.33.76:5555'    # Marcus
class MultiProcessCommunication:
	def __init__(self):
		#Connect to Arduino, Algo and Android
		self.arduino = Arduino()
		self.algorithm = Algorithm()
		self.android = Android()

		self.manager = Manager()
		self.MDF_LIST = self.manager.list([0])
		self.IMAGE_LIST = self.manager.list()
		
		#Messages from various modules are placed in this queue before being read
		self.message_queue = self.manager.Queue()
	
		#Messages to android are placed in this queue
		self.to_android_message_queue = self.manager.Queue()

		self.read_arduino_process = Process(target=self._read_arduino)
		self.read_algorithm_process = Process(target = self._read_algorithm)
		self.read_android_process = Process(target=self._read_android)

		self.write_process = Process(target=self._write_target)
		self.write_android_process = Process(target=self._write_android)
		print('Multi Process initialized')

		self.dropped_connection = Value('i',0)
        
		#For image rec
		self.image_process = Process(target=self._process_pic)
 
       	#Pictures taken by RPICAM put in this queue to avoid sending all at once
		self.image_queue = self.manager.Queue()

	def start(self):
		try:
			#Connect to arduio, algo and android
			self.arduino.connect()
			self.algorithm.connect()
			self.android.connect()
			
			#Start the process to listen and read from algo, android and arduino
			self.read_arduino_process.start()
			self.read_algorithm_process.start()
			self.read_android_process.start()

			#Start the process to write to algo and arduino
			self.write_process.start()

			#Start the process to write to android
			self.write_android_process.start()

			print('Comms started. Reading from algo and android and arduino.')

			self.image_process.start()
			print("Image server connected!")

		except Exception as err:
			raise err

		self._allow_reconnection()

	def _allow_reconnection(self):
		while True:
			try:
				if not self.read_android_process.is_alive():
					self._reconnect_android()
				if not self.write_android_process.is_alive():
					self._reconnect_android()
			except Exception as error:
				print('Error in reconnection')
				raise error

	def _reconnect_android(self):
		self.android.disconnect()

		self.read_android_process.terminate()
		self.write_process.terminate()
		self.write_android_process.terminate()

		self.android.connect()

		self.read_android_process = Process(target=self._read_android)
		self.read_android_process.start()

		self.write_process = Process(target=self._write_target)
		self.write_process.start()

		self.write_android_process = Process(target=self._write_android)
		self.write_android_process.start()

		print('Reconnected to android!')


	def _format_for(self, target, message):
		#Function to return a dictionary containing the target and the message
		return {
			'target': target,
			'payload': message,
		}

	def _read_arduino(self):
		'''
		Reading any messages that Arduino send
		Arduino only needs to send messages to Algo (PC)
		'''
		while True:
			try:
				rawmessage = self.arduino.read()

				if rawmessage == None:
					continue

				message_list = rawmessage.splitlines()
				
				for message in message_list:
					if len(message) <= 0:
						continue
					else:
						self.message_queue.put_nowait(self._format_for(ALGORITHM_HEADER, message + NEWLINE))
			except Exception as err:
				print("_read_arduino failed - {}".format(str(err)))
				break


	def _read_algorithm(self):
		picam = VideoStream(usePicamera=True).start()
		while True:
			try:
				raw_message = self.algorithm.read()
				if raw_message is None:
					continue

				message_list = raw_message.decode().split("|")
				if(len(message_list) > 2):
					print(message_list)
		
				for message in message_list:
					if len(message) <= 0:
						continue

					elif (message[0] == 'P'):
						image = picam.read()
						self.image_queue.put_nowait([image, message[1:].encode()])

					elif (message == 'EF'):
						image = picam.read()
						self.image_queue.put_nowait([ image, "END"])
						

					elif (message[0] == 'M'):
						#If message from PC is the MDF string (Arena)
						MDF_STRING_FINAL = message[1:]
						self.MDF_LIST[0] = MDF_STRING_FINAL
					
						self.to_android_message_queue.put_nowait("{M:"+MDF_STRING_FINAL+ "}|")
						
					elif(message[0] == 'K'):
						self.message_queue.put_nowait(self._format_for(ARDUINO_HEADER, message[1:].encode()))
					else:
						print('from _read_algo = {}'.format(message))
						self.algorithm_to_android(message.encode())
						self.message_queue.put_nowait(self._format_for(ARDUINO_HEADER, message.encode()))

			except Exception as err:
				raise err


	def algorithm_to_android(self, message):
		#Send message from Algo (PC) to Android
		MESSAGE_SEPARATOR = ""
		message_to_send = "{m:"+message.decode()[0]
		message_to_send = message_to_send.encode()
		if len(message_to_send) <= 0:
			return

		elif (message_to_send == TURN_LEFT_AND):
			self.to_android_message_queue.put_nowait("{m:A|")

		elif (message_to_send == TURN_RIGHT_AND):
			self.to_android_message_queue.put_nowait("{m:D|")

		elif (message_to_send == MOVE_FORWARD_AND):
			self.to_android_message_queue.put_nowait("{m:W|")

	def _read_android(self):
		while True:
			try:
				rawmessage = self.android.read()
				if rawmessage == None:
					continue
				message_list = rawmessage.splitlines()
				for message in message_list:
					if len(message) <= 0:
						continue
					elif(message == "MDF".encode()):
						self.to_android_message_queue.put_nowait("{M:"+self.MDF_LIST[0]+ "}|")
					elif(message == "IMAGE".encode()):
						for image in self.IMAGE_LIST:
							self.to_android_message_queue.put_nowait('{s:'+image+'|')
					else:
						print("from _read_Android = {}".format(message+NEWLINE))
						self.message_queue.put_nowait(self._format_for(ALGORITHM_HEADER, message + NEWLINE))

			except Exception as err:
				print('_read_android error - {}'.format(str(err)))
				break

	def _write_target(self):
		while True:
			target = None
			try:
				if not self.message_queue.empty():
					message = self.message_queue.get_nowait()
					target, payload = message['target'], message['payload']
					if target == ALGORITHM_HEADER:
						self.algorithm.write(payload)
					elif target == ARDUINO_HEADER:
						self.arduino.write(payload)
			except Exception as err:
				print('failed {}'.format(err))
				break

	def _write_android(self):
		while True:
			try:
				if not self.to_android_message_queue.empty():
					message = self.to_android_message_queue.get_nowait()
					self.android.write(message)
			except Exception as error:
				print('Process write_android failed: ' + str(error))
				break

	def _process_pic(self):
		image_sender = imagezmq.ImageSender(connect_to=image_processing_server_url)
		image_id_list = []

		while True:
			try:
				if not self.image_queue.empty():
					image_msg = self.image_queue.get_nowait()
					obstacle_coordinates = image_msg[1] #Format = (x,y)
					reply = image_sender.send_image(obstacle_coordinates, image_msg[0])
					reply = reply.decode('utf-8')

					if reply != 'End':
						if(len(reply) != 0):
							if reply != 'None':
								self.IMAGE_LIST.append(reply)
						self.to_android_message_queue.put_nowait('{s:'+reply+'|')
						
					else:
						break

			except Exception as error:
				print("_process_pic failed: {}".format(str(error)))

	def _format_fastest_path_string(self, fp):
		new_fp = ""
		count=1
		fp=fp.decode()
		fp = fp.replace('1|', '')
		fp = fp[1:]
		current_dir = fp[0]
		for i in range(1, len(fp)+1):
			#If index is out of range, append and break  
			if i == len(fp):
				new_fp = new_fp + current_dir + str(count)
				break
		
			#If count == 9, append then reset
			if count == 9:
				new_fp = new_fp + current_dir + str(count)
				count = 0
		
			if fp[i] == current_dir:
				count+=1
			else:
				if count == 0:
					count +=1
					new_fp = new_fp + fp[i] + str(count)
					count=0
		
				else:
					new_fp = new_fp + current_dir + str(count)
					current_dir = fp[i]
					count=1
		print("new fp = {}".format(new_fp))
		return new_fp.encode()