#include <MsTimer2.h>
#include <PID_v1.h>
#include <EnableInterrupt.h>
#include <DualVNH5019MotorShield.h>

//Pin attach
const int LEFT_PULSE = 3; // LEFT M1 Pulse
const int RIGHT_PULSE = 11; // RIGHT M2 Pulse

//RPM Setpoints
const int FAST_RPM = 115;
const int MAX_RPM = 120;
const int MIN_RPM = 90;
const int TURN_RPM = 80;
const int ROTATE_RPM = 60;

//Distance to cover in ticks
const int TURN_TICKS_L = 792;
const int TURN_TICKS_R = 788;
const int TICKS[10] = {558, 1120, 1710, 2310, 2915, 3515, 4155, 4735, 5350};

//PID tunings
const double kp = 20, ki = 0.0, kd = 0.01; // Arena 1 STEP
const double rtKp = 3, rtKi = 0.0, rtKd = 0.002; // Arena 1 TURN RIGHT
const double fKp = 10, fKi = 0.0, fKd = 0.0; // Arena 1 FAST

int MOVE_FAST_SPEED_L = calculateSpeedL(FAST_RPM);
int MOVE_MAX_SPEED_L = calculateSpeedL(MAX_RPM);
int MOVE_MIN_SPEED_L = calculateSpeedL(MIN_RPM);
int TURN_MAX_SPEED_L = calculateSpeedL(TURN_RPM);
int MOVE_FAST_SPEED_R = calculateSpeedR(FAST_RPM);
int MOVE_MAX_SPEED_R =  calculateSpeedR(MAX_RPM);
int MOVE_MIN_SPEED_R = calculateSpeedR(MIN_RPM);
int TURN_MAX_SPEED_R = calculateSpeedR(TURN_RPM);
//const int MOVE_FAST_SPEED = 370;
//const int MOVE_MAX_SPEED = 352;
//const int MOVE_MIN_SPEED = 200;
//const int TURN_MAX_SPEED = 260;
int ROTATE_MAX_SPEED_L = calculateSpeedL(ROTATE_RPM);
int ROTATE_MAX_SPEED_R = calculateSpeedR(ROTATE_RPM);
const int ROTATE_MAX_SPEED = 150;

double tick_L = 0;
double tick_R = 0;
double speed_O = 0;
double previous_tick_L = 0;
double previous_error = 0;

DualVNH5019MotorShield md;
PID myPID(&tick_L, &speed_O, &tick_R, kp, ki, kd, DIRECT);

//--------------------------Motor Codes-------------------------------
void setupMotorEncoder() {
  md.init();
  pinMode(LEFT_PULSE, INPUT);
  pinMode(RIGHT_PULSE, INPUT);
  enableInterrupt(LEFT_PULSE, leftMotorTime, CHANGE);
  enableInterrupt(RIGHT_PULSE, rightMotorTime, CHANGE);
}

void stopMotorEncoder() {
  disableInterrupt(LEFT_PULSE);
  disableInterrupt(RIGHT_PULSE);
}

void setupPID() {
  myPID.SetMode(AUTOMATIC);
  myPID.SetOutputLimits(-370, 370);
  myPID.SetSampleTime(5);
}

void moveForward(int distance) {
  initializeTick();
  initializeMotor_Start();
  
  myPID.SetTunings(kp, ki, kd);
  
  distance = cmToTicks(distance);
  double currentSpeedL = 0;
  double currentSpeedR = 0;
  if (distance < TICKS[1]) {
    myPID.SetTunings(kp, ki, kd);
    currentSpeedL = MOVE_MIN_SPEED_L;
    currentSpeedR = MOVE_MIN_SPEED_R;
    md.setSpeeds(-calculateSpeedL(10), -calculateSpeedR(10));
    delay(10);
    md.setSpeeds(-calculateSpeedL(20), -calculateSpeedR(20));
    delay(10);
    md.setSpeeds(-calculateSpeedL(30), -calculateSpeedR(30));
    delay(10);
    md.setSpeeds(-calculateSpeedL(40), -calculateSpeedR(40));
    delay(10);
    md.setSpeeds(-calculateSpeedL(50), -calculateSpeedR(50));
    delay(10);
  //while (tick_L <= distance || tick_R <= distance) {
    md.setSpeeds(-calculateSpeedL(75), -calculateSpeedR(75));
    delay(10);
  } else {
    myPID.SetTunings(fKp, fKi, fKd);
    currentSpeedL = MOVE_MAX_SPEED_L;
    currentSpeedR = MOVE_MAX_SPEED_R;
    md.setSpeeds(-calculateSpeedL(10), -calculateSpeedR(10));
    delay(10);
    md.setSpeeds(-calculateSpeedL(20), -calculateSpeedR(20));
    delay(10);
     md.setSpeeds(-calculateSpeedL(30), -calculateSpeedR(30));
    delay(10);
    md.setSpeeds(-calculateSpeedL(40), -calculateSpeedR(40));
    delay(10);
    md.setSpeeds(-calculateSpeedL(50), -calculateSpeedR(50));
    delay(50);
    //while (tick_L <= distance || tick_R <= distance) {
    md.setSpeeds(-calculateSpeedL(75), -calculateSpeedR(75));
    delay(50);
    md.setSpeeds(-calculateSpeedL(100), -calculateSpeedR(100));
    delay(50); 
  }
  
  int last_tick_L = 0;
  while (tick_L <= distance || tick_R <= distance) {
    //PID TESTING
    //Serial.println(tick_L - tick_R);
    //  delay(5);
    if (myPID.Compute() || tick_L == last_tick_L) {
      md.setSpeeds(-(currentSpeedL - speed_O), -(currentSpeedR + speed_O)); 
    }
  }
  if(distance<1155){
    initializeMotor_End();
  }
  else{
    initializeFastMotor_End();
  }
    
}

void moveBackwards(int distance) {
  initializeTick();
  initializeMotor_Start();
  
  myPID.SetTunings(kp, ki, kd);
  
  distance = cmToTicks(distance);
  double currentSpeedL = 0;
  double currentSpeedR = 0;
  if (distance < 1155 ) {
    currentSpeedL = MOVE_MIN_SPEED_L;
    currentSpeedR = MOVE_MIN_SPEED_R;
  } else {
    currentSpeedL = MOVE_MAX_SPEED_L;
    currentSpeedR = MOVE_MAX_SPEED_R;
  }

  for (int i = 0; i <= 100; i+=20) {
    for (int j = 0; j <= 150; j+=30) {
      md.setSpeeds(i,j);
    }
  }
  
  //double offset = 0;
  int last_tick_L = 0;
  while (tick_L <= distance || tick_R <= distance) {
    //Serial.println(tick_L - tick_R);
    //delay(5);
    if (myPID.Compute() || tick_L == last_tick_L) {
      md.setSpeeds((currentSpeedL - speed_O), (currentSpeedR + speed_O)); 
    }
  }
  initializeMotor_End();
}

/*void moveForwardFast(int distance) {
  initializeTick();
  initializeMotor_Start();
  distance = cmToTicks(distance);
  double currentSpeed = 0;
  if (distance < 30) {
    currentSpeed = MOVE_MAX_SPEED;
  } else {
    currentSpeed = MOVE_FAST_SPEED;
  }
  double offset = 0;
  int last_tick_L = 0;
  while (tick_L <= distance || tick_R <= distance) {
    if (distance - tick_L < 150)
      currentSpeed = 150;
    if ((tick_L - last_tick_L) >= 10 || tick_L == 0 || tick_L == last_tick_L) {
      last_tick_L = tick_L;
      offset += 0.1;
    }
    if (myPID.Compute() || tick_L == last_tick_L) {
      if (offset >= 1)
        md.setSpeeds(currentSpeed + speed_O, currentSpeed - speed_O);
      else
        md.setSpeeds(offset * (currentSpeed + speed_O), offset * (currentSpeed - speed_O));
    }
  }
  initializeMotor_End();
}*/

/*void moveBackwardsFast(int distance) {
  initializeTick();
  initializeMotor_Start();
  distance = cmToTicks(distance);
  double currentSpeed = 0;
  if (distance < 30) {
    currentSpeed = MOVE_MAX_SPEED;
  } else {
    currentSpeed = MOVE_FAST_SPEED;
  }
  double offset = 0;
  long last_tick_L = 0;
  while (tick_L <= distance || tick_R <= distance) {
    if (distance - tick_L < 150)
      currentSpeed = 150;
    if ((tick_L - last_tick_L) >= 10 || tick_L == 0 || tick_L == last_tick_L) {
      last_tick_L = tick_L;
      offset += 0.1;
    }
    if (myPID.Compute() || tick_L == last_tick_L) {
      if (offset >= 1)
        md.setSpeeds(-(currentSpeed + speed_O), -(currentSpeed - speed_O));
      else
        md.setSpeeds(-(offset * (currentSpeed + speed_O)), -(offset * (currentSpeed - speed_O)));
    }
  }
  initializeMotor_End();
}*/

void moveForwardCalibrate(int distance1) {
  initializeTick();
  initializeMotor_Start();
  
  myPID.SetTunings(kp, ki, kd);
  
  double distance = distance1/19.48*562.25;
  if (distance < 1)
    return;  
  double currentSpeedL = 0;
  double currentSpeedR = 0;
  if (distance < 6000) {
    currentSpeedL = 100;
    currentSpeedR = 100;
  } else {
    currentSpeedL = MOVE_MAX_SPEED_L;
    currentSpeedR = MOVE_MAX_SPEED_R;
  }
  double offset = 0;
  int last_tick_L = 0;
  while (tick_L <= distance || tick_R <= distance) {
    if ((tick_L - last_tick_L) >= 10 || tick_L == 0 || tick_L == last_tick_L) {
      last_tick_L = tick_L;
      offset += 0.1;
    }
    if (myPID.Compute() || tick_L == last_tick_L) {
      if (offset >= 1 || distance < 110)
        md.setSpeeds(-(currentSpeedL - speed_O), -(currentSpeedR + speed_O));
      else
        md.setSpeeds(-1* offset * (currentSpeedL - speed_O),-1* offset * (currentSpeedR + speed_O));
    }
  }
  initializeMotor_End();
}


void moveBackwardsCalibrate(int distance1) {
  initializeTick();
  initializeMotor_Start();

   myPID.SetTunings(kp, ki, kd);
  
  double distance = distance1/19.48*562.25;
  if (distance < 1)
    return;  
  double currentSpeedL = 0;
  double currentSpeedR = 0;
  if (distance < 6000) {
    currentSpeedL = 100;
    currentSpeedR = 100;
  } else {
    currentSpeedL = MOVE_MAX_SPEED_L;
    currentSpeedR = MOVE_MAX_SPEED_R;
  }
  double offset = 0;
  int last_tick_L = 0;
  while (tick_L <= distance || tick_R <= distance) {
    if ((tick_L - last_tick_L) >= 10 || tick_L == 0 || tick_L == last_tick_L) {
      last_tick_L = tick_L;
      offset += 0.1;
    }
    if (myPID.Compute() || tick_L == last_tick_L) {
      if (offset >= 1 || distance < 110)
        md.setSpeeds((currentSpeedL - speed_O), (currentSpeedR + speed_O));
      else
        md.setSpeeds(offset * (currentSpeedL - speed_O), offset * (currentSpeedR + speed_O));
    }
  }
  initializeMotor_End();
}

void turnRight() {
  initializeTick();
  initializeMotor_Start();

  myPID.SetTunings(rtKp, rtKi, rtKd);
  
  double currentSpeedL = TURN_MAX_SPEED_L;
  double currentSpeedR = TURN_MAX_SPEED_R;
  double offset = 0;

  while (tick_L < TURN_TICKS_R || tick_R < TURN_TICKS_R) {
    //Serial.println(tick_L - tick_R);
    //delay(5);
    if (myPID.Compute())
      md.setSpeeds(-(currentSpeedL - speed_O), (currentSpeedR + speed_O));
  }
  //initializeMotor_End();
  initializeLeftTurnEnd();
}

void turnLeft() {
  initializeTick();
  initializeMotor_Start();

  myPID.SetTunings(kp, ki, kd);
  
  double currentSpeedL = TURN_MAX_SPEED_L;
  double currentSpeedR = TURN_MAX_SPEED_R;
  double offset = 0;
  
  while (tick_L < (TURN_TICKS_L) || tick_R < (TURN_TICKS_L)) {

   // Serial.println(tick_L - tick_R);
   // delay(5);
    if (myPID.Compute())
      md.setSpeeds((currentSpeedL - speed_O), -(currentSpeedR + speed_O));
  }
  //initializeMotor_End();
  initializeRightTurnEnd();
}

void rotateLeft(int distance) {
  initializeTick();
  initializeMotor_Start();

  myPID.SetTunings(rtKp, rtKi, rtKd);
  
  double currentSpeed = ROTATE_MAX_SPEED;
  double offset = 0;
  if (distance < 1.0)
    return;
  while (tick_L < distance || tick_R < distance) {
    if (myPID.Compute())
      md.setSpeeds(-(currentSpeed + speed_O), currentSpeed - speed_O);
  }
  initializeMotor_End();
}

void rotateRight(int distance) {
  initializeTick();
  initializeMotor_Start();

   myPID.SetTunings(rtKp, rtKi, rtKd);
  
  double currentSpeed = ROTATE_MAX_SPEED;
  double offset = 0;
  if (distance < 1.0)
    return;
  while (tick_L < distance || tick_R < distance) {
    if (myPID.Compute())
      md.setSpeeds((currentSpeed + speed_O), -(currentSpeed - speed_O));
  }
  initializeMotor_End();
}

void alignRight() {
  delay(2);
  double diff = getRightIR1() - getRightIR2();
  int rotated = 0;
  while (abs(diff) >= 0.1 && rotated < 20 && getRightIR1_Block() == getRightIR2_Block() && getRightIR1_Block()< 2) {
    rotated++;
    if (diff > 0) {
      rotateRight(abs(diff * 5));
      diff = getRightIR1() - getRightIR2();
      if (getRightIR1_Block() != getRightIR2_Block()) {
        rotateLeft(abs(diff * 2));
        diff = getRightIR1() - getRightIR2();
      }
    } else {
      rotateLeft(abs(diff * 5));
      diff = getRightIR1() - getRightIR2();
      if (getRightIR1_Block() != getRightIR2_Block()) {
        rotateRight(abs(diff * 2));
        diff = getRightIR1() - getRightIR2();
      }
    }
    delay(1);
  }
  delay(2);
}

void alignFront() {
  delay(2);
  int moved = 0;
  double diff_dis = getMin(getFrontIR1(),20.0,getFrontIR3());
  while ((abs(diff_dis) < 10.0 && moved < 30) || (abs(diff_dis) > 10.4 && moved < 20)){
      if (diff_dis > 10.4) {
        moveForwardCalibrate(1);
          md.setSpeeds(50, -50);
      } else {
        moveBackwardsCalibrate(1);
          md.setSpeeds(-50, 50);
      }
      delay(2);
      diff_dis = getMin(getFrontIR1(),20.0,getFrontIR3());
      moved++;
    }
  double diff = getFrontIR1() - getFrontIR3();
  int rotated = 0;
  while (abs(diff) >= 0.1 && rotated < 20 && getFrontIR1_Block() == getFrontIR3_Block() && getFrontIR1_Block()< 2) {
    rotated++;
    if (diff > 0) {
      rotateLeft(abs(diff * 5));
      diff = getFrontIR1() - getFrontIR3();
      if (getFrontIR1_Block() != getFrontIR3_Block()) {
        rotateRight(abs(diff * 2));
        diff = getFrontIR1() - getFrontIR3();
      }
    } else {
      rotateRight(abs(diff * 5));
      diff = getFrontIR1() - getFrontIR3();
      if (getFrontIR1_Block() != getFrontIR3_Block()) {
        rotateLeft(abs(diff * 2));
        diff = getFrontIR1() - getFrontIR3();
      }
    }
    delay(1);
  }
  delay(2);
  initializeMotor_End();

}

int calculateSpeedL(double rpm) {
  double speedL = 2.8949 * rpm + 20.253;
  return speedL;
}

int calculateSpeedR(double rpm) {
  double speedR = 2.9366 * rpm + 29.016;
  return speedR;
}

void initializeLeftTurnEnd() {
  //  rotateRight(8);
  //  rotateLeft(6);
  md.setSpeeds(0, 0);
  md.setBrakes(400, 400);
  
  delay(5);
}


void initializeRightTurnEnd() {
    //rotateLeft(8);
    //rotateRight(6);
  md.setSpeeds(0, 0);
  md.setBrakes(400, 400);
  
  delay(5);
}

double getMin(double f1, double f2, double f3) {
  if (f1 < f2) {
    if (f1 < f3) {
      return f1;
    } else {
      return f3;
    }
  } else {
    if (f2 < f3) {
      return f2;
    } else {
      return f3;
    }
  }
}

void leftMotorTime() {
  tick_R++;
}

void rightMotorTime() {
  tick_L++;
}

void initializeTick() {
  tick_L = 0;
  tick_R = 0;
  speed_O = 0;
  previous_tick_L = 0;
}

void initializeMotor_Start() {
  md.setBrakes(400, 400);
  md.setSpeeds(0, 0);
  md.setBrakes(0, 0);
}

void initializeMotor_End() {
  
  md.setSpeeds(0, 0);
  md.setBrakes(400, 380);
  
  delay(5);
}

void initializeFastMotor_End() {
  
  md.setSpeeds(0, 0);
  md.setBrakes(400, 400);
  
  delay(5);
}

int cmToTicks(int cm) {
  int dist = (cm / 10) - 1;
  if (dist < 10)
    return TICKS[dist];
  return 0;
}

int cmToTicksCalibrate(int cm) {
  double ret = ((double)cm * TICKS[0] / 10.0) + 0.5;
  return ret;
}
