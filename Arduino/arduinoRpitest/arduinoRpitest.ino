
void setup() {
  setupSerialConnection();
  delay(20);

}

boolean readSomething = false;
int incomingByte=0;
void loop() {
  delay(2);
  if (!Serial) {
    Serial.println("Waiting for connection");
  }
  if (Serial){
    Serial.println("Connection established. Awaiting message");
    delay(1000);
  }
  if (Serial.available() > 0) {
    // read the incoming byte:
    incomingByte = Serial.read();

    // say what you got:
    Serial.print("Connection established. Received: ");
    Serial.println(incomingByte, DEC);
    delay(1000);

  }
}



//--------------------------Serial Codes-------------------------------
void setupSerialConnection() {
  Serial.begin(9600);
  while (!Serial);
}
