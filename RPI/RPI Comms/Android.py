from bluetooth import *
import os

LOCALE = 'UTF-8'
ANDROID_SOCKET_BUFFER_SIZE = 1024
RFCOMM_CHANNEL = 4
UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee"


class Android:
    def __init__(self):
        self.serversocket = None
        self.clientsocket = None

        os.system("sudo hciconfig hci0 piscan")
        self.serversocket = BluetoothSocket(RFCOMM)
        self.serversocket.bind(("", RFCOMM_CHANNEL))
        self.serversocket.listen(RFCOMM_CHANNEL)
        port = self.serversocket.getsockname()[1]

        advertise_service( self.serversocket, "MDP-Server",
        service_id = UUID,
        service_classes = [ UUID, SERIAL_PORT_CLASS ],
        profiles = [ SERIAL_PORT_PROFILE ],
        # protocols = [ OBEX_UUID ]
        )
        print("Waiting for connection on RFCOMM channel %d" % port)


    def connect(self):
        try:
            print("Connecting to Android...")

            if self.clientsocket == None:
                self.clientsocket, client_info = self.serversocket.accept()

                print("Accepted connection from ", client_info)
                retry = False

        except Exception as err:
            print("Connection to Android failed. Error = {}".format(str(err)))

            if self.clientsocket is not None:
                self.clientsocket.close()
                self.clientsocket = None
    
    def read(self):
        message = self.clientsocket.recv(ANDROID_SOCKET_BUFFER_SIZE).strip()
        print("From Android = {}".format(message))

        if message == None:
            return None

        if len(message) > 0:
            return message
        
        return None

    def write(self, message):
        print("To Android: {}".format(message))
        try:
             self.clientsocket.send(message)
        except Exception as err:
            print("Fail to send to Android... {}".format(str(err)))
            raise err

    def disconnect(self):
        try:
            if self.clientsocket is not None:
                self.clientsocket.close()
                self.clientsocket = None

        except Exception as err:
            print("android.disconnect() failed:" +str(err))
