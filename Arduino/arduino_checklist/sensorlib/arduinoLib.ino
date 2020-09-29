
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
  //  getFrontIR1();
  //  getFrontIR3();
//  returnSensorReading_Raw();
/*  if (Serial.available() > 0) {
    char command = '0';
    int value = -1;
    char arr[15];
    int c = 0;
    char hold = Serial.read();
    if (hold == '[') {
      Serial.println('R');
      Serial.flush();
      delay(1);
      hold = Serial.read();
      while (hold != ']') {
        arr[c] = hold;
        c ++;
        hold = Serial.read();
        delay(2);
      }
    } else {
      while (Serial.available() > 0) {
        if ((char)Serial.peek() == '[' )
          return;
        Serial.println("DISCARDING");
        Serial.read();
        delay(1);
        return;
      }
    }
    command = arr[8];
    char value_ca[c - 10 + 1];
    for (int g = 10; g < c; g++) {
      value_ca[g - 10] = arr[g];
    }
    value_ca[c - 10] = '\0';
    value = atoi(value_ca);
        Serial.println(command);
        Serial.println(value);

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
        moveForward(value * 10);
        delay(10);
        returnSensorReading_Raw();
        break;
      case 'A':
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
        break;
      case 'S':
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

void returnSensorReading_Raw() {
  Serial.print(getFrontIR1_Block());
  Serial.print("|");
  Serial.print(getFrontIR2_Block());
  Serial.print("|");
  Serial.print(getFrontIR3_Block());
  Serial.print("|");
  Serial.print(getLeftIR1_Block());
  Serial.print("|");
  Serial.print(getRightIR2_Block());
  Serial.print("|");
  Serial.print(getRightIR1_Block());  
  Serial.print(":");
  Serial.flush();
}
