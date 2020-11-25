import serial

SERIAL_PORT = "/dev/ttyACM0"
BAUD_RATE = '9600'
LOCALE = 'UTF-8'

class Arduino:
    def __init__(self, serial_port=SERIAL_PORT, baud_rate=BAUD_RATE):
        self.serial_port = serial_port
        self.baud_rate = baud_rate
        self.connection = None

    def connect(self):
        count = 1000000
        while True:
            retry = False

            try:
                if count >= 1000000:
                    print('Establishing connection with Arduino')

                self.connection = serial.Serial(self.serial_port, self.baud_rate)

                if self.connection is not None:
                    print('Successfully connected with Arduino: ' + str(self.connection.name))
                    retry = False

            except Exception as error:
                if count >= 1000000:
                    print('Connection with Arduino failed: ' + str(error))

                retry = True

            if not retry:
                break

            if count >= 1000000:
                print('Retrying Arduino connection...')
                count=0

            count += 1

    def read(self):
        try:
            message = self.connection.readline().strip()
            print("From Arduino: {}".format(message))

            if len(message) > 0:
                return message

            return None
       
        except Exception as error:
            print('Arduino read failed: ' + str(error))
            raise error
    
    def write(self, message):
        try:
            print('To Arduino:')
            print(message)
            self.connection.write(message)

        except Exception as error:
            print('Arduino write failed: ' + str(error))
            raise error