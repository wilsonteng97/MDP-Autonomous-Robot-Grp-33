import time
from datetime import datetime
from multiprocessing import Process, Value, Queue, Manager

from Algorithms import Algorithm
from Arduino import Arduino
# from Android import Android


from socket import error as SocketError
import errno

ANDROID_HEADER = 'AND'.encode()
ARDUINO_HEADER = 'ARD'.encode()
ALGORITHM_HEADER = 'ALG'.encode()

class MultiProcessCommunication:
    def __init__(self):
        #Connect to Arduino, Algo and Android
        # self.arduino = Arduino()
        self.algorithm = Algorithm()
        # self.android = Android()

        self.manager = Manager()
        
        #Messages from various modules are placed in this queue before being read
        self.message_queue = self.manager.Queue()
        #Messages to android are placed in this queue
        # self.to_android_message_queue = self.manager.Queue()

        # self.read_arduino_process = Process(target=self._read_arduino)
        self.read_algorithm_process = Process(target = self._read_algorithm)
        # self.read_android_process = Process(target=self._read_android)

        self.write_process = Process(target=self._write_target)
        # self.write_android_process = Process(target=self._write_android)
        print('Multi Process initialized')


#		self.status = Status.IDLE
#		self.dropped_connection = Value('i',0)


    def start(self):
        try:
            #Connect to arduio, algo and android
            # self.arduino.connect()
            self.algorithm.connect()
            # self.android.connect()
            
            #Start the process to listen and read from algo, android and arduino
            # self.read_arduino_process.start()
            self.read_algorithm_process.start()
            # self.read_android_process.start()

            #Start the process to write to algo and arduino
            self.write_process.start()

            #Start the process to write to android
            # self.write_android_process.start()

            print('Comms started. Reading from algo and android and arduino.')

        except Exception as err:
            raise err

    def _format_for(self, target, message):
        #Function to return a dictionary containing the target and the message
        return {
            'target': target,
            'payload': message,
        }

# 	def _read_arduino(self):
# 		'''
# 		Arduino only needs to send messages to Algo (PC)
# 		'''
# 		while True:
# 			try:
# 				rawmessage = self.arduino.read()

# 				if rawmessage == None:
# 					continue

# 				message_list = rawmessage.splitlines()

# 				for message in message_list:
# 					if len(message) <= 0:
# 						continue
# 					else:
# 						self.message_queue.put_nowait(self._format_for(ALGORITHM_HEADER, message))
# 			except Exception as err:
# 				print("_read_arduino failed - {}".format(str(err)))
# 				break


    def _read_algorithm(self):
        '''
        To-dos: Layout messages to relay
        '''
        while True:
            try:
                raw_message = self.algorithm.read()
                
                if raw_message is None:
                    continue

                message_list = raw_message.splitlines()
        
                for message in message_list:

                    if len(message) <= 0:
                        continue

                else:
                    if(message[0] == 'M'.encode()[0]):
                        print('Sending to Android')
                        self.to_android_message_queue.put_nowait(message[1:])
                    else:
                        print(message)

            except Exception as err:
                raise err


# 	def algorithm_to_android(self, message):
# 		#Send message from Algo (PC) to Android
# 		'''
# 		Todos - Account for messages - E.g. Algo - Android : Turn right
# 		'''
# 		MESSAGE_SEPARATOR = ""
# 		messages_to_send = message.split(MESSAGE_SEPARATOR)

# 		for message_to_send in messages_to_send:
# 			if len(message_to_send) <= 0:
# 				continue
# 			else:
# 				self.to_android_message_queue.put_nowait(message_to_send)


# 	def _read_android(self):
# 		while True:
# 			try:
# 				rawmessage = self.android.read()

# 				if rawmessage == None:
# 					continue

# 				message_list = rawmessage.splitlines()

# 				for message in message_list:
# 					if len(message) <= 0:
# 						continue

# 					else:
# 						self.message_queue.put_nowait(self._format_for(ARDUINO_HEADER, message))
# #						self.message_queue.put_nowait(self._format_for(ALGORITHM_HEADER,message))
# 			except Exception as err:
# 				print('_read_android error - {}'.format(str(err)))
# 				break

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

    # def _write_android(self):
    #     while True:
    #         try:
    #             if not self.to_android_message_queue.empty():
    #                 message = self.to_android_message_queue.get_nowait()
    #                 self.android.write(message)
    #         except Exception as error:
    #             print('Process write_android failed: ' + str(error))
    #             break


    # def testing_arduino(self):
    #     while True:
    #         message = input('Input message to send to arduino')
    #         self.arduino.write(message.encode())

    #         if message == 'q':
    #             break

    def testing_algo(self):
        while True:
            message = input("Input message to send to Algo:\n")
            self.algorithm.write(message.encode())

            if message == 'q':
                break

    # def testing_android(self):
    #     while True:
    #         message = input("Input message to send to Android:\n")
    #         self.android.write(message.encode())

    #         if message == 'q':
    #             break









