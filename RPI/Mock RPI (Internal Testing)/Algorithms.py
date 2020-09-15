import socket

LOCALE = 'UTF-8'
AlgoBufferSize = 512 
WIFI_IP = 'localhost'
WIFI_PORT = 5000


#To kill port: sudo lsof -i:5040 -> sudo kill -9 <pid>
class Algorithm:
	def __init__(self, host=WIFI_IP, port=WIFI_PORT):
		self.host=host
		self.port=port
		
		self.clientsocket=None
		self.socket=None
		self.clientaddress=None

		self.server=socket.socket(socket.AF_INET, socket.SOCK_STREAM)

#		self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
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
				print('Another one!')


	def disconnect(self):
		try:
			if self.clientsocket != None:
				self.clientsocket.close()
				self.clientsocket = None


			if self.server is not None:
				self.server.close()
				self.server = None

			print('Disconnected')

		except Exception as err:
			print('Failed to disconnect...{}'.format(str(err)))


	def read(self):
		try:
			message = self.clientsocket.recv(AlgoBufferSize).strip()

			if len(message) > 0:
				return message


		except Exception as err:
			print('Failed to read from PC: {}'.format(str(err)))
			raise err
		

	def write(self, message):
		try:
			self.clientsocket.sendall(message)

		except Exception as err:
			raise err
		








