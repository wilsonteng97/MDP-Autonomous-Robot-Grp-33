boolean readSomething = false;

void setup() {
  setupSerialConnection();
  setupMotorEncoder();
  delay(3000);
}

void loop() {
  delay(2);
  if (!Serial) {
    Serial.println("Waiting for connection");
  }

  while (Serial.available()>0){
  char command=Serial.read();
  Serial.println(command);
  int value=1;

      switch (command) {
      case 'W':
        Serial.print("move forward");
        moveForward(value * 10);
        delay(10);
        returnSensorReading_Raw();
        break;
      /*case 'A':
        for (int k = 0; k < value; k++) {
          turnLeft();
        }
        delay(10);
        returnSensorReading_Raw();
        break;
      case 'D':
        for (int k = 0; k < value; k++) {
          turnRight();
        }
        delay(10);
        returnSensorReading_Raw();
        break;*/
      case 'S':
        moveBackwards(value * 10);
        delay(10);
        returnSensorReading_Raw();
        break;
      /*case 'V':
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
        break;*/
    }    
  }

}

void setupSerialConnection() {
  Serial.begin(9600);
  while (!Serial);
}

void returnSensorReading_Raw() {
  Serial.print("Ard|Alg|S|1:");
  Serial.print(getFrontIR1());
  Serial.print(":");
  Serial.print(getFrontIR1_Block());
  Serial.print(",2:");
  Serial.print(getFrontIR2());
  Serial.print(":");
  Serial.print(getFrontIR2_Block());
  Serial.print(",3:");
  Serial.print(getFrontIR3());
  Serial.print(":");
  Serial.print(getFrontIR3_Block());
  Serial.print(",4:");
  Serial.print(getRightIR1());
  Serial.print(":");
  Serial.print(getRightIR1_Block());
  Serial.print(",5:");
  Serial.print(getRightIR2());
  Serial.print(":");
  Serial.print(getRightIR2_Block());
  Serial.print(",6:");
  Serial.print(getLeftIR1());
  Serial.print(":");
  Serial.println(getLeftIR1_Block());
  //  Serial.println("Ard|And|S|");
  Serial.flush();
}


void returnSensorReading() {
  Serial.print("Ard|Alg|S|1:");
  Serial.print((int)getFrontIR1());
  Serial.print(":");
  Serial.print(getFrontIR1_Block());
  Serial.print(",2:");
  Serial.print((int)getFrontIR2());
  Serial.print(":");
  Serial.print(getFrontIR2_Block());
  Serial.print(",3:");
  Serial.print((int)getFrontIR3());
  Serial.print(":");
  Serial.print(getFrontIR3_Block());
  Serial.print(",4:");
  Serial.print((int)getRightIR1());
  Serial.print(":");
  Serial.print(getRightIR1_Block());
  Serial.print(",5:");
  Serial.print((int)getRightIR2());
  Serial.print(":");
  Serial.print(getRightIR2_Block());
  Serial.print(",6:");
  Serial.print((int)getLeftIR1());
  Serial.print(":");
  Serial.print(getLeftIR1_Block());
  Serial.print("\n");
  //  Serial.println("Ard|And|S|");
  Serial.flush();
}
