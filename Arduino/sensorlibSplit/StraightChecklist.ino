#include <MsTimer2.h>
#include <PID_v1.h>
#include <EnableInterrupt.h>
#include <DualVNH5019MotorShield.h>

const int LEFT_PULSE = 3; // LEFT M1 Pulse
const int RIGHT_PULSE = 11; // RIGHT M2 Pulse
const int MOVE_FAST_SPEED_L = 345;
const int MOVE_MAX_SPEED_L = 338;
const int MOVE_MIN_SPEED_L = 281;
const int TURN_MAX_SPEED_L = 259;
const int MOVE_FAST_SPEED_R = 370;
const int MOVE_MAX_SPEED_R = 360;
const int MOVE_MIN_SPEED_R = 293;
const int TURN_MAX_SPEED_R = 280;
const int MOVE_FAST_SPEED = 370;
const int MOVE_MAX_SPEED = 352;
const int MOVE_MIN_SPEED = 200;
const int TURN_MAX_SPEED = 260;
const int ROTATE_MAX_SPEED_L = 136;
const int ROTATE_MAX_SPEED_R = 150;
const int ROTATE_MAX_SPEED = 150;
const int TURN_TICKS_L = 805 * 0.97;
const int TURN_TICKS_R = 805 * 0.99;
const int TICKS[10] = {565, 1155, 1760, 2380, 2985, 3615, 4195, 4775, 5370};
const double DIST_WALL_CENTER_BOX = 1.58;
const double kp = 0.5, ki = 0.0, kd = 0.0; // Arena 1
//KP 0.02 KD 0.009
int TENCM_TICKS_OFFSET = 0;

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
      md.setSpeeds(-i,-j);
    }
  }
  
  //double offset = 0;
  int last_tick_L = 0;
  while (tick_L <= distance || tick_R <= distance) {
    /*if ((tick_L - last_tick_L) >= 10 || tick_L == 0 || tick_L == last_tick_L) {
      last_tick_L = tick_L;
      offset += 0.1;
    }*/
    //Serial.println(speed_O);
    //delay(500);
    if (myPID.Compute() || tick_L == last_tick_L) {
      md.setSpeeds(-(currentSpeedL - speed_O), -(currentSpeedR + speed_O)); 
      /*if (offset >= 1)
        md.setSpeeds(-(currentSpeedL - speed_O), -(currentSpeedR + speed_O));
      else
        md.setSpeeds(-1*offset * (currentSpeedL - speed_O), -1*offset * (currentSpeedR + speed_O));*/
    }
  }
  initializeMotor_End();
}

void moveBackwards(int distance) {
  /*initializeTick();
  initializeMotor_Start();
  distance = cmToTicks(distance);
  double currentSpeed = 0;
  if (distance < 60) {
    currentSpeed = MOVE_MIN_SPEED;
  } else {
    currentSpeed = MOVE_MAX_SPEED;
  }
  double offset = 0;
  long last_tick_L = 0;
  while (tick_L <= distance || tick_R <= distance) {
    if ((tick_L - last_tick_L) >= 10 || tick_L == 0 || tick_L == last_tick_L) {
      last_tick_L = tick_L;
      offset += 0.1;
    }
    if (myPID.Compute() || tick_L == last_tick_L) {
      if (offset >= 1)
        md.setSpeeds((currentSpeed + speed_O), (currentSpeed - speed_O));
      else
        md.setSpeeds((offset * (currentSpeed + speed_O)), (offset * (currentSpeed - speed_O)));
    }
  }
  initializeMotor_End();*/
  initializeTick();
  initializeMotor_Start();
  distance = cmToTicks(distance);
  double currentSpeedL = 0;
  double currentSpeedR = 0;
  if (distance < 60) {
    currentSpeedL = MOVE_MIN_SPEED_L;
    currentSpeedR = MOVE_MIN_SPEED_R;
  } else {
    currentSpeedL = MOVE_MAX_SPEED_L;
    currentSpeedR = MOVE_MAX_SPEED_R;
  }

  for (int i = 0; i <= 300; i++) {
    md.setSpeeds(i, i);
  }
  
  double offset = 0;
  int last_tick_L = 0;
  while (tick_L <= distance || tick_R <= distance) {
    if ((tick_L - last_tick_L) >= 10 || tick_L == 0 || tick_L == last_tick_L) {
      last_tick_L = tick_L;
      offset += 0.1;
    }
    if (myPID.Compute() || tick_L == last_tick_L) {
      if (offset >= 1)
        md.setSpeeds((currentSpeedL - speed_O), (currentSpeedR + speed_O));
      else
        md.setSpeeds(offset * (currentSpeedL - speed_O), offset * (currentSpeedR + speed_O));
    }
  }
  initializeMotor_End();
}

void moveForwardFast(int distance) {
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
}

void moveBackwardsFast(int distance) {
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
}

void moveForwardCalibrate(int distance1) {
  initializeTick();
  initializeMotor_Start();
  double distance = distance1/18.85*562.25;
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
  double distance = distance1/18.85*562.25;
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

  double currentSpeedL = TURN_MAX_SPEED_L;
  double currentSpeedR = TURN_MAX_SPEED_R;
  double offset = 0;

  while (tick_L < TURN_TICKS_R || tick_R < TURN_TICKS_R) {
    //    offset = computePID();
    if (myPID.Compute())
      md.setSpeeds(-(currentSpeedL - speed_O), currentSpeedR);
  }
  initializeMotor_End();
  initializeLeftTurnEnd();
}

void turnLeft() {
  initializeTick();
  initializeMotor_Start();
  double currentSpeedL = TURN_MAX_SPEED_L;
  double currentSpeedR = TURN_MAX_SPEED_R;
  double offset = 0;

  while (tick_L < (TURN_TICKS_L) || tick_R < (TURN_TICKS_L)) {
    //    offset = computePID();
    if (myPID.Compute())
      md.setSpeeds((currentSpeedL - speed_O), -(currentSpeedR));
  }
  initializeMotor_End();
  initializeRightTurnEnd();
}

/*void rotateLeft(int distance) {
  initializeTick();
  initializeMotor_Start();
  double currentSpeedL = ROTATE_MAX_SPEED_L;
  double currentSpeedR = ROTATE_MAX_SPEED_R;
  double offset = 0;
  if (distance < 3)
    return;
  while (tick_L < distance || tick_R < distance) {
    //    offset = computePID();
    if (myPID.Compute())
      md.setSpeeds((currentSpeedL - speed_O), -(currentSpeedR + speed_O);
  }
  initializeMotor_End();
}

void rotateRight(int distance) {
  initializeTick();
  initializeMotor_Start();
  double currentSpeedL = ROTATE_MAX_SPEED_L;
  double currentSpeedR = ROTATE_MAX_SPEED_R;
  double offset = 0;
  if (distance < 3)
    return;
  while (tick_L < distance || tick_R < distance) {
    //    offset = computePID();
    if (myPID.Compute())
      md.setSpeeds(-(currentSpeedL - speed_O), currentSpeedR + speed_O);
  }
  initializeMotor_End();
}*/

void rotateLeft(int distance) {
  initializeTick();
  initializeMotor_Start();
  double currentSpeed = ROTATE_MAX_SPEED;
  double offset = 0;
  if (distance < 3)
    return;
  while (tick_L < distance || tick_R < distance) {
    //    offset = computePID();
    if (myPID.Compute())
      md.setSpeeds(-(currentSpeed + speed_O), currentSpeed - speed_O);
  }
  initializeMotor_End();
}

void rotateRight(int distance) {
  initializeTick();
  initializeMotor_Start();
  double currentSpeed = ROTATE_MAX_SPEED;
  double offset = 0;
  if (distance < 3)
    return;
  while (tick_L < distance || tick_R < distance) {
    //    offset = computePID();
    if (myPID.Compute())
      md.setSpeeds((currentSpeed + speed_O), -(currentSpeed - speed_O));
  }
  initializeMotor_End();
}

void alignRight() {
  delay(2);
  double diff = getRightIR1() - getRightIR2();
  int rotated = 0;
  while (abs(diff) >= 0.1 && rotated < 20 && getRightIR1_Block() == getRightIR2_Block() && getRightIR1_Block()< 3) {
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
        rotateRight(abs(diff * 4));
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
  double diff_dis = getMin(getFrontIR1(),getFrontIR2(),getFrontIR3());
  while ((abs(diff_dis) < 6.2 && moved < 30) || (abs(diff_dis) > 6.4 && moved < 20)){
      if (diff_dis > 6.4) {
        moveForwardCalibrate(1);
          md.setSpeeds(50, -50);
      } else {
        moveBackwardsCalibrate(1);
          md.setSpeeds(-50, 50);
      }
      delay(2);
      diff_dis = getMin(getFrontIR1(),getFrontIR2(),getFrontIR3());
      moved++;
    }
  double diff = getFrontIR1() - getFrontIR3();
  int rotated = 0;
  while (abs(diff) >= 0.1 && rotated < 20 && getFrontIR1_Block() == getFrontIR3_Block() && getFrontIR1_Block()< 3) {
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


void initializeLeftTurnEnd() {
  //  rotateRight(8);
  //  rotateLeft(6);
}


void initializeRightTurnEnd() {
  //  rotateLeft(8);
  //  rotateRight(6);
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
  md.setBrakes(0, 400);
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
