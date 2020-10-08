
void setup() {
  setupSerialConnection();
  setupMotorEncoder();
  setupSensorInterrupt();
  setupPID();
  delay(20);

}

boolean readSomething = false;
void loop() {
  delay(2);
  if (!Serial) {
    Serial.println("Waiting for connection");
    Serial.flush();
  }
/*  
    // Alg|Ard|0|{1-10} (Steps) [Alg|Ard|0|3]
    //2nd Character of the Array is the Command

    // W : FORWARD
    // A: TURN_LEFT
    // D: TURN_RIGHT
    // S: BACKWARD
    // V: ALIGN_FRONT
    // B: ALIGN_RIGHT
    // 6: SEND_SENSORS
    */
while (Serial.available()>0){
 char command=Serial.read();
  int value=1;    
    switch (command) {
      case 'W':
        value=takeInt();
        moveForward(value * 10);
        delay(10);
        returnSensorReading_Raw();
        alignRight();
        delay(10);
        break;
      case 'A':
        value=takeInt();
        for (int k = 0; k < value; k++) {
          turnLeft();
        }
        delay(10);
        returnSensorReading_Raw();
        break;
      case 'D':
        value=takeInt();
        for (int k = 0; k < value; k++) {
          turnRight();
        }
        delay(10);
        returnSensorReading_Raw();
        break;
      case 'S':
        value=takeInt();
        moveBackwards(value * 10);
        delay(10);
        returnSensorReading_Raw();
        break;
      case 'V':
        alignFront();
        returnSensorReading_Raw();
        break;
      case 'B':
        alignRight();
        returnSensorReading_Raw();
        break;
      case 'Z':
        returnSensorReading_Raw();
        break;
      case 'U':
        md.setSpeeds(0,0);
        md.setBrakes(400,400);
        break;
    }    
  }
}



//--------------------------Serial Codes-------------------------------
void setupSerialConnection() {
  Serial.begin(9600);
  while (!Serial);
}

int takeInt(){
  char hold[2];
  int value=0;
  delay(10);
  char i=Serial.read();
  hold[0]=i;
  hold[1]='\0';
  value = atoi(hold);
  return value;
}

void returnSensorReading_Raw() {
  getFrontIR1();
  Serial.print(getFrontIR1_Block());
  Serial.print("|");
  getFrontIR2();
  Serial.print(getFrontIR2_Block());
  Serial.print("|");
  getFrontIR3();
  Serial.print(getFrontIR3_Block());
  Serial.print("|");
  getLeftIR1();
  Serial.print(getLeftIR1_Block());
  Serial.print("|");
  getRightIR2();
  Serial.print(getRightIR2_Block());
  Serial.print("|");
  getRightIR1();
  Serial.println(getRightIR1_Block());  
  Serial.flush();
}
