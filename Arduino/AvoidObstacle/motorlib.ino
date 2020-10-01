#include "DualVNH5019MotorShield.h"
#include "PinChangeInterrupt.h"

#define LEFT_ENCODER  3
#define RIGHT_ENCODER 11

// Motor timing
unsigned long currTime = 0;                 // updated on every loop
unsigned long startTimeL = 0;               // start timing A interrupts
unsigned long startTimeR = 0;               // start timing B interrupts
unsigned long tickCountL = 0;               // count the A interrupts
unsigned long tickCountR = 0;               // count the B interrupts
unsigned long distanceTicksL = 0;           //distance L
unsigned long distanceTicksR = 0;           //distance R
unsigned long timeWidthL = 0;               // motor A period
unsigned long timeWidthR = 0;               // motor B period                  
unsigned long printTime = 0;
unsigned long testTime = 0;

float setDistance = 50;
float setTickDistance = 0;

const unsigned long SAMPLE_COUNT = 20;

// PID   
const unsigned long SAMPLE_TIME = 1000;  // time between PID updates  
const unsigned long INT_COUNT = 20;     // sufficient interrupts for accurate timing  
double setpointInit;
double prevOutputL = 0; 
double outputL = 0;             // output is rotational speed in Hz   
double prevOutputR = 0;
double outputR = 0;             // output is rotational speed in Hz  
double outputSpeedL = 0;
double outputSpeedR = 0;
/*double KpL = 2.5, KiL = 0.1, KdL = 0.1;  
double KpR = 4, KiR = 0.1, KdR = 0.07; 
double kL1 = KpL + KiL + KdL;
double kL2 = -KpL - 2 * KdL;
double kL3 = KdL; 
double kR1 = KpR + KiR + KdR;
double kR2 = -KpR - 2 * KdR;
double kR3 = KdR;*/ 
double errorL1 = 0;
double errorL2 = 0;
double errorL3 = 0;
double errorR1 = 0;
double errorR2 = 0;
double errorR3 = 0;
double currRPML;
double currRPMR;

DualVNH5019MotorShield md;


/*void setup() {

  Serial.begin(115200);
  md.init();
  pinMode (LEFT_ENCODER, INPUT); //set digital pin 3 as input
  pinMode (RIGHT_ENCODER, INPUT); //set digital pin 11 as input
  attachPCINT(digitalPinToPCINT(LEFT_ENCODER), incTicksL, HIGH);
  attachPCINT(digitalPinToPCINT(RIGHT_ENCODER), incTicksR, HIGH);
  delay(3000);
  setpointInit = 80;
  setTickDistance = distToTicks(setDistance);
  Serial.print("Distance is "); Serial.print(setDistance); Serial.print(" cm, or "); Serial.print(setTickDistance); Serial.println(" ticks.");
  md.setSpeeds(-calculateSpeedL(setpointInit), -calculateSpeedR(setpointInit));
  prevOutputL = calculateSpeedL(setpointInit);
  prevOutputR = calculateSpeedR(setpointInit);
  Serial.print("Moving forward at ");
  Serial.print(setpointInit);
  Serial.print(" RPM at speeds ");
  Serial.print(-calculateSpeedL(setpointInit));
  Serial.println(-calculateSpeedR(setpointInit));
  stopIfFault();
  startTimeL = micros();
  startTimeR = micros();
  printTime = micros();

}*/

/*void loop() {
  
  currTime = micros();
  if ((distanceTicksL + distanceTicksR)/2 >= setTickDistance) {
    md.setBrakes(400, 400);
    md.setSpeeds(0, 0);
    Serial.print("Distance of "); Serial.print(setDistance); Serial.println(" cm reached.");
  }
  else if ((currTime - printTime) >=100000) {
    currRPML = calculateRPM(timeWidthL);
    currRPMR = calculateRPM(timeWidthR);
    Serial.print("Current speeds: ");
    Serial.print(outputL); Serial.print(", ");
    Serial.println(outputR);
    pidController();
    Serial.print("Errors in RPM: ");
    Serial.print(errorL1); Serial.print(", "); Serial.println(errorR1);
    printTime = currTime;
    stopIfFault();
  }
  
}*/

void moveForward(int distance) {
  md.setSpeeds(-calculateSpeedL(setpointInit), -calculateSpeedR(setpointInit));
  prevOutputL = calculateSpeedL(setpointInit);
  prevOutputR = calculateSpeedR(setpointInit);
  errorL1 = setpointInit - currRPML;
  errorR1 = setpointInit - currRPMR;
  currTime = testTime = startTimeL = startTimeR = micros();
  int distanceInTicks = distToTicks(distance);
  while((distanceTicksL + distanceTicksR)/2 < distanceInTicks) {
    //Serial.println(distanceTicksL);
    currTime = micros();
    if ((currTime - testTime) >= 100000) {
      currRPML = calculateRPM(timeWidthL);
      currRPMR = calculateRPM(timeWidthR);
      pidControllerForward();
      testTime = currTime;
      stopIfFault();
    }
    else {
      continue;
    }
  }
  distanceTicksL = distanceTicksR = 0;
  md.setBrakes(400, 400);
  md.setSpeeds(0, 0);
  Serial.println("!");
}

void moveBackwards(int distance) {
  md.setSpeeds(calculateSpeedL(setpointInit), calculateSpeedR(setpointInit));
  prevOutputL = calculateSpeedL(setpointInit);
  prevOutputR = calculateSpeedR(setpointInit);
  currTime = testTime = startTimeL = startTimeR = micros();
  int distanceInTicks = distToTicks(distance);
  while((distanceTicksL + distanceTicksR)/2 < distanceInTicks) {
    //Serial.println(distanceTicksL);
    currTime = micros();
    if ((currTime - testTime) >= 100000) {
      currRPML = calculateRPM(timeWidthL);
      currRPMR = calculateRPM(timeWidthR);
      pidControllerBackwards();
      testTime = currTime;
      stopIfFault();
    }
    else {
      continue;
    }
  }
  distanceTicksL = distanceTicksR = 0;
  md.setBrakes(400, 400);
  md.setSpeeds(0, 0);
  Serial.println("!");
}

void rotateRight(int angle) {
  md.setSpeeds(-calculateSpeedL(setpointInit), calculateSpeedR(setpointInit));
  currTime = testTime = startTimeL = startTimeR = micros();
  int angleInTicks =rotToTicks(angle);
  while((distanceTicksL + distanceTicksR)/2 < angleInTicks) {
    //Serial.println(distanceTicksL);
    currTime = micros();
    if ((currTime - testTime) >= 100000) {
      currRPML = calculateRPM(timeWidthL);
      currRPMR = calculateRPM(timeWidthR);
      pidControllerRight();
      testTime = currTime;
      stopIfFault();
    }
    else {
      continue;
    }
  }
  distanceTicksL = distanceTicksR = 0;
  md.setBrakes(400, 400);
}

void rotateLeft(int angle) {
  md.setSpeeds(calculateSpeedL(setpointInit), -calculateSpeedR(setpointInit));
  currTime = testTime = startTimeL = startTimeR = micros();
  int angleInTicks =rotToTicks(angle);
  while((distanceTicksL + distanceTicksR)/2 < angleInTicks) {
    //Serial.println(distanceTicksL);
    currTime = micros();
    if ((currTime - testTime) >= 100000) {
      currRPML = calculateRPM(timeWidthL);
      currRPMR = calculateRPM(timeWidthR);
      pidControllerLeft();
      testTime = currTime;
      stopIfFault();
    }
    else {
      continue;
    }
  }
  distanceTicksL = distanceTicksR = 0;
  md.setBrakes(400, 400);
}

/*void moveBackward(int distance) {
  md.setSpeeds(calculateSpeedL(setpointInit), calculateSpeedR(setpointInit));
  currTime = testTime = startTimeL = startTimeR = micros();
  int distanceInTicks = distToTicks(distance);
  while((distanceTicksL + distanceTicksR)/2 < distanceInTicks) {
  //  Serial.println(distanceTicksL);
    currTime = micros();
    if ((currTime - testTime) >= 10000) {
      currRPML = calculateRPM(timeWidthL);
      currRPMR = calculateRPM(timeWidthR);
      pidControllerBackward();
      testTime = currTime;
      stopIfFault();
    }
    else {
      continue;
    }
  }
  distanceTicksL = distanceTicksR = 0;
  md.setBrakes(400, 400);
  md.setSpeeds(0, 0);
}*/

void setupMotorEncoder() {
  md.init();
  pinMode (LEFT_ENCODER, INPUT); //set digital pin 3 as input
  pinMode (RIGHT_ENCODER, INPUT); //set digital pin 11 as input
  attachPCINT(digitalPinToPCINT(LEFT_ENCODER), incTicksL, HIGH);
  attachPCINT(digitalPinToPCINT(RIGHT_ENCODER), incTicksR, HIGH);
  setpointInit = 80;
}

void incTicksL() {
  tickCountL++;
  if (tickCountL >= SAMPLE_COUNT) {
    timeWidthL = currTime - startTimeL;
    startTimeL = currTime;
    tickCountL = 0;
  }
  distanceTicksL++;
}

void incTicksR() {
  tickCountR++;
  if (tickCountR >= SAMPLE_COUNT) {
    timeWidthR = currTime - startTimeR;
    startTimeR = currTime;
    tickCountR = 0;
  }
  distanceTicksR++;
}
double calculateRPM(double timeWidth) {
  double rpm = 60000000 / ((double)timeWidth / (double)SAMPLE_COUNT) / 562.25 / 2;
  return rpm;
}

double calculateTimeWidth(double rpm) {
  double timeWidth = rpm * 2 * 562.25 / (double)SAMPLE_COUNT / 60000000;
}

double calcRPMfromSpeedL(double speedL) {
  double rpmL = 0.2951 * speedL - 8.1446;
  return rpmL;
}

double calcRPMfromSpeedR(double speedR) {
  double rpmR = 0.2828 * speedR - 10.843;
  return rpmR;
}

double calculateSpeedL(double rpm) {
  double speedL = 3.384 * rpm + 27.879;
  return speedL;
}

double calculateSpeedR(double rpm) {
  double speedR = 3.5165 * rpm + 39.366;
  return speedR;
}

void pidControllerForward() {

  double KpL = 2, KiL = 0, KdL = 0;  
  double KpR = 2, KiR = 0, KdR = 0; 
  double kL1 = KpL + KiL + KdL;
  double kL2 = -KpL - 2 * KdL;
  double kL3 = KdL; 
  double kR1 = KpR + KiR + KdR;
  double kR2 = -KpR - 2 * KdR;
  double kR3 = KdR;
  
  Serial.print(".");
  //Serial.print("Setpoint RPM = ");
  //Serial.println(setpointInit);

  
  //PID control law
  
  outputL = prevOutputL + kL1 * errorL1 + kL2 * errorL2 + kL3 * errorL3;
  outputR = prevOutputR + kR1 * errorR1 + kR2 * errorR2 + kR3 * errorR3;
  
  //outputSpeedL = calculateSpeedL(outputL);
  //outputSpeedR = calculateSpeedR(outputR);

  md.setSpeeds(-outputL, -outputR);

  prevOutputL = outputL;
  prevOutputR = outputR;
  
  errorL3 = errorL2;
  errorL2 = errorL1;
  errorR3 = errorR2;
  errorR2 = errorR1;
  
}

void pidControllerBackwards() {

  double KpL = 2, KiL = 0, KdL = 0;  
  double KpR = 0, KiR = 0, KdR = 0; 
  double kL1 = KpL + KiL + KdL;
  double kL2 = -KpL - 2 * KdL;
  double kL3 = KdL; 
  double kR1 = KpR + KiR + KdR;
  double kR2 = -KpR - 2 * KdR;
  double kR3 = KdR;
  
  Serial.print(".");
  //Serial.print("Setpoint RPM = ");
  //Serial.println(setpointInit);

  errorL1 = setpointInit - currRPML;
  errorR1 = setpointInit - currRPMR;
  
  //PID control law
  
  outputL = prevOutputL + kL1 * errorL1 + kL2 * errorL2 + kL3 * errorL3;
  outputR = prevOutputR + kR1 * errorR1 + kR2 * errorR2 + kR3 * errorR3;

  //outputSpeedL = calculateSpeedL(outputL);
  //outputSpeedR = calculateSpeedR(outputR);

  md.setSpeeds(outputL, outputR);

  prevOutputL = outputL;
  prevOutputR = outputR;
  
  errorL3 = errorL2;
  errorL2 = errorL1;
  errorR3 = errorR2;
  errorR2 = errorR1;
  
}

void pidControllerRight() {

  double KpL = 3, KiL = 0.1, KdL = 0.1;  
  double KpR = 3, KiR = 0.1, KdR = 0.1; 
  double kL1 = KpL + KiL + KdL;
  double kL2 = -KpL - 2 * KdL;
  double kL3 = KdL; 
  double kR1 = KpR + KiR + KdR;
  double kR2 = -KpR - 2 * KdR;
  double kR3 = KdR;
  
  //Serial.println("PID Controller is running...");
  //Serial.print("Setpoint RPM = ");
  //Serial.println(setpointInit);

  errorL1 = setpointInit - currRPML;
  errorR1 = setpointInit - currRPMR;
  
  //PID control law
  
  outputL = prevOutputL + kL1 * errorL1 + kL2 * errorL2 + kL3 * errorL3;
  outputR = prevOutputR + kR1 * errorR1 + kR2 * errorR2 + kR3 * errorR3;

  outputSpeedL = calculateSpeedL(outputL);
  outputSpeedR = calculateSpeedR(outputR);

  md.setSpeeds(-outputL, outputR);

  prevOutputL = outputL;
  prevOutputR = outputR;
  
  errorL3 = errorL2;
  errorL2 = errorL1;
  errorR3 = errorR2;
  errorR2 = errorR1;
  
}

void pidControllerLeft() {

  double KpL = 3, KiL = 0.1, KdL = 0.1;  
  double KpR = 3, KiR = 0.1, KdR = 0.1; 
  double kL1 = KpL + KiL + KdL;
  double kL2 = -KpL - 2 * KdL;
  double kL3 = KdL; 
  double kR1 = KpR + KiR + KdR;
  double kR2 = -KpR - 2 * KdR;
  double kR3 = KdR;
  
  //Serial.println("PID Controller is running...");
  //Serial.print("Setpoint RPM = ");
  //Serial.println(setpointInit);

  errorL1 = setpointInit - currRPML;
  errorR1 = setpointInit - currRPMR;
  
  //PID control law
  
  outputL = prevOutputL + kL1 * errorL1 + kL2 * errorL2 + kL3 * errorL3;
  outputR = prevOutputR + kR1 * errorR1 + kR2 * errorR2 + kR3 * errorR3;

  outputSpeedL = calculateSpeedL(outputL);
  outputSpeedR = calculateSpeedR(outputR);

  md.setSpeeds(outputL, -outputR);

  prevOutputL = outputL;
  prevOutputR = outputR;
  
  errorL3 = errorL2;
  errorL2 = errorL1;
  errorR3 = errorR2;
  errorR2 = errorR1;
  
}

void alignRight() {
  Serial.println(getRightIR1());
  Serial.println(getRightIR2());
  delay(2);
  double diff = getRightIR1() - getRightIR2();
  int rotated = 0;
  while (abs(diff) >= 0.10 && rotated < 20) {
    rotated++;
    if (diff > 0) {
      rotateRight(abs(diff * 10));
      diff = getRightIR1() - getRightIR2();
      if (getRightIR1_Block() != getRightIR2_Block()) {
//        rotateLeft(abs(diff * 1));
        diff = getRightIR1() - getRightIR2();
      }
    } else {
      rotateLeft(abs(diff * 10));
      diff = getRightIR1() - getRightIR2();
      if (getRightIR1_Block() != getRightIR2_Block()) {
//        rotateRight(abs(diff * 1));
        diff = getRightIR1() - getRightIR2();
      }
    }
    delay(1);
  Serial.println(rotated);
  Serial.println(getRightIR1());
  Serial.println(getRightIR2());
  }
  delay(2);
}

/*void alignFront() {
  delay(2);
  double diff_dis;
  int moved = 0;
  double previous_turn = 0;
  if (getFrontIR1_Block() != 1 || getFrontIR3_Block() != 1 ) {
    do {
      diff_dis = getMin(getFrontIR1(), getFrontIR3(), getFrontIR2()) - DIST_WALL_CENTER_BOX;
      if (diff_dis > 0) {
        moveForwardCalibrate(abs(diff_dis*0.75));
      } else {
        moveBackwardsCalibrate(abs(diff_dis*0.75));
      }
      delay(2);
      diff_dis = getMin(getFrontIR1(), getFrontIR3(), getFrontIR2()) - DIST_WALL_CENTER_BOX;
      moved++;
    } while (abs(diff_dis) > 0.2 && moved < 15);
//    return;
  }
  delay(2);
  turnLeft();
  delay(1000);
  alignRight();
  delay(1000);
  turnRight();
  turnRight();
//  moved = 0;
//  double diff = getFrontIR3()/2 - getFrontIR1();
//  Serial.print("difference between IR1 and 3: ");
//  Serial.println(diff);
//  Serial.println(getFrontIR1());
//  Serial.println(getFrontIR3());
//  delay(1000);
//  int rotated = 0;
//  while (abs(diff) >= 0.10 && rotated < 20) {
//    rotated++;
//    if (diff > 0) {
//      rotateRight(abs(diff * 5));
//      diff = getFrontIR3()/2 - getFrontIR1();
//      if (getFrontIR3_Block() != getFrontIR1_Block()) {
////        rotateLeft(abs(diff * 1));
//        diff = getFrontIR3() - getFrontIR1();
//      }
//    } else {
//      rotateLeft(abs(diff * 5));
//      diff = getFrontIR3()/2 - getFrontIR1();
//      if (getFrontIR3_Block() != getFrontIR1_Block()) {
////        rotateRight(abs(diff * 1));
//        diff = getFrontIR3()/2 - getFrontIR1();
//      }
//    }
//    delay(1);
//  Serial.println(rotated);
//  Serial.println(getFrontIR1());
//  Serial.println(getFrontIR3());
//  }
  delay(2);
//  while (abs(diff) >= 0.2 && moved < 15) {
//    moved++;
//    previous_turn = abs(diff * 5);
//    if (diff > 0) {
//      Serial.print("rotateRight0 "); Serial.print(previous_turn);
//      rotateRight(previous_turn);
//      diff = getFrontIR1() - getFrontIR3();
////      if (getFrontIR1_Block() != getFrontIR3_Block()) {
////        rotateLeft(previous_turn);
////        break;
////      }
//    } else {
//      Serial.print("rotateLeft0 "); Serial.print(previous_turn);
//      rotateLeft(previous_turn);
//      diff = getFrontIR1() - getFrontIR3();
////      if (getFrontIR1_Block() != getFrontIR3_Block()) {
////        rotateLeft(previous_turn);
////        break;
////      }
//    }
//    delay(2);
//  }
//  delay(2);
//  moved = 0;
//  do {
//    diff_dis = getMin(getFrontIR1(), getFrontIR3(), getFrontIR2()) - DIST_WALL_CENTER_BOX;
//    if (diff_dis > 0) {
//      moveForwardCalibrate(abs(diff_dis));
//    } else {
//      moveBackwardsCalibrate(abs(diff_dis));
//    }
//    delay(2);
//    diff_dis = getMin(getFrontIR1(), getFrontIR3(), getFrontIR2()) - DIST_WALL_CENTER_BOX;
//    moved++;
//  } while (abs(diff_dis) > 0.2 && moved < 20);
//  moved = 0;
//  delay(2);
//  diff = getFrontIR1() - getFrontIR2();
//  Serial.print(diff);
//  while (abs(diff) >= 0.2 && moved < 15) {
//    moved++;
//    previous_turn = abs(diff * 5);
//    if (diff > 0) {
//      Serial.print("rotateRight "); Serial.print(previous_turn);
//      rotateRight(previous_turn);
//      diff = getFrontIR1() - getFrontIR2();
////      if (getFrontIR1_Block() != getFrontIR2_Block())
////        rotateLeft(previous_turn);
//    } else {
//      Serial.print("rotateLeft "); Serial.print(previous_turn);
//      rotateLeft(previous_turn);
//      diff = getFrontIR1() - getFrontIR2();
////      if (getFrontIR1_Block() != getFrontIR2_Block())
////        rotateLeft(previous_turn);
//    }
//    delay(2);
//  }
}*/

float distToTicks(float distance) {
  float ticks = distance / 18.85 * 562.25 * 2;
  return ticks;
}

float rotToTicks(float angle) {
  float ticks = angle / 360 * 56.2345 / 18.85 * 562.25 * 2 * 0.96;
  return ticks;
}

void stopIfFault()
{
  if (md.getM1Fault())
  {
    Serial.println("M1 fault");
    while (1);
  }
  if (md.getM2Fault())
  {
    Serial.println("M2 fault");
    while (1);
  }
}
