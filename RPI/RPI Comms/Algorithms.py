import socket

LOCALE = 'UTF-8'
AlgoBufferSize = 512 
WIFI_IP = '192.168.33.1'
WIFI_PORT = 5040

#To kill port: sudo lsof -i:5040 -> sudo kill -9 <pid>
class Algorithm:
	def __init__(self, host=WIFI_IP, port=WIFI_PORT):
		self.host=host
		self.port=port
		
		self.clientsocket=None
		self.socket=None
		self.clientaddress=None

		self.server=socket.socket(socket.AF_INET, socket.SOCK_STREAM)

		self.server.bind((self.host, self.port))
		self.server.listen(4)

	def connect(self):
		while True:
			retry = False
			try:
				print("Establishing connection with Algo")

				if self.clientsocket is None:
					self.clientsocket, self.clientaddress = self.server.accept()
					print(self.clientaddress)
					print('Connected')
					retry = False

			except Exception as err:
				print('Error! {}'.format(str(err)))
				if self.clientsocket is not None:
					self.clientsocket.close()
					self.clientsocket = None
				retry = True


			if not retry:
				break
			else:
				print('Retrying connection...')

	def read(self):
		try:
			message = self.clientsocket.recv(AlgoBufferSize).strip()
			print("From Algo: {}".format(message))
			if len(message) > 0:
				return message


		except Exception as err:
			print('Failed to read from PC: {}'.format(str(err)))
			raise err
		

	def write(self, message):
		try:
			print("To Algo: {}".format(message))
			self.clientsocket.send(message)
		except Exception as err:
			raise err