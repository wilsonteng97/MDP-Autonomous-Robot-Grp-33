
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
          delay(10);
          alignRight();
        }
        delay(10);
        returnSensorReading_Raw();
        break;
      case 'D':
        value=takeInt();
        for (int k = 0; k < value; k++) {
          turnRight();
          delay(10);
          alignRight();
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
        returnSensorReading_Debug();
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

void returnSensorReading_Debug() {
  Serial.print(getFrontIR1());
//  Serial.print(getFrontIR1_Block());
  Serial.print("|");
  Serial.print(getFrontIR2());
//  Serial.print(getFrontIR2_Block());
  Serial.print("|");
  Serial.print(getFrontIR3());
//  Serial.print(getFrontIR3_Block());
  Serial.print("|");
  Serial.print(getLeftIR1());
  Serial.print("|");
  Serial.print(getLeftIR1_Block());
  Serial.print("|");
  Serial.print(getRightIR2());
//  Serial.print(getRightIR2_Block());
  Serial.print("|");
  Serial.println(getRightIR1());
//  Serial.println(getRightIR1_Block());  
  Serial.flush();
}
