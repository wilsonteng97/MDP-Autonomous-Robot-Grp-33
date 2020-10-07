
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
