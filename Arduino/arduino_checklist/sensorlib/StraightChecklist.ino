#include <MsTimer2.h>
#include <PID_v1.h>
#include <PID_AutoTune_v0.h>
#include <EnableInterrupt.h>
#include <DualVNH5019MotorShield.h>

const int LEFT_PULSE = 3; // LEFT M1 Pulse
const int RIGHT_PULSE = 11; // RIGHT M2 Pulse
const int MOVE_FAST_SPEED = 370;
const int MOVE_MAX_SPEED = 310;
const int MOVE_MIN_SPEED = 200;
const int TURN_MAX_SPEED = 260;
const int ROTATE_MAX_SPEED = 150;
const int TURN_TICKS_L = 395;
const int TURN_TICKS_R = 389;
const int TICKS[10] = {545, 1155, 1760, 2380, 2985, 3615, 4195, 4775, 5370};
const double DIST_WALL_CENTER_BOX = 1.58;
const double kp = 0.02, ki = 0, kd = 0.0124; // Arena 1
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
  double currentSpeed = 0;
  if (distance < 60) {
    currentSpeed = MOVE_MIN_SPEED;
  } else {
    currentSpeed = MOVE_MAX_SPEED;
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
        md.setSpeeds(currentSpeed + speed_O, currentSpeed - speed_O);
      else
        md.setSpeeds(offset * (currentSpeed + speed_O), offset * (currentSpeed - speed_O));
    }
  }
  initializeMotor_End();
}

void moveBackwards(int distance) {
  initializeTick();
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
        md.setSpeeds(-(currentSpeed + speed_O), -(currentSpeed - speed_O));
      else
        md.setSpeeds(-(offset * (currentSpeed + speed_O)), -(offset * (currentSpeed - speed_O)));
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

void moveForwardCalibrate(int distance) {
  initializeTick();
  initializeMotor_Start();
  distance = cmToTicksCalibrate(distance);
  if (distance < 3)
    return;
  double currentSpeed = 0;
  //  Serial.print("HELLO: ");
  //  Serial.println(distance);
  if (distance < 60) {
    currentSpeed = MOVE_MIN_SPEED;
  } else {
    currentSpeed = MOVE_MAX_SPEED;
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
        md.setSpeeds(-(currentSpeed + speed_O), -(currentSpeed - speed_O));
      else
        md.setSpeeds(-1* offset * (currentSpeed + speed_O),-1* offset * (currentSpeed - speed_O));
    }
  }
  initializeMotor_End();
}


void moveBackwardsCalibrate(int distance) {
  initializeTick();
  initializeMotor_Start();
  distance = cmToTicksCalibrate(distance);
  if (distance < 3)
    return;
  double currentSpeed = 0;
  //  Serial.print("HELLO: ");
  //  Serial.println(distance);
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
      if (offset >= 1 || distance < 110)
        md.setSpeeds((currentSpeed + speed_O), (currentSpeed - speed_O));
      else
        md.setSpeeds((offset * (currentSpeed + speed_O)), (offset * (currentSpeed - speed_O)));
    }
  }
  initializeMotor_End();
}

void turnRight() {
  initializeTick();
  initializeMotor_Start();
  double currentSpeed = TURN_MAX_SPEED;
  double offset = 0;

  while (tick_L < TURN_TICKS_L || tick_R < TURN_TICKS_L) {
    //    offset = computePID();
    if (myPID.Compute())
      md.setSpeeds(-(currentSpeed + speed_O), currentSpeed - speed_O);
  }
  initializeMotor_End();
  initializeLeftTurnEnd();
}

void turnLeft() {
  initializeTick();
  initializeMotor_Start();
  double currentSpeed = TURN_MAX_SPEED;
  double offset = 0;

  while (tick_L < (TURN_TICKS_R + 10) || tick_R < (TURN_TICKS_R + 10)) {
    //    offset = computePID();
    if (myPID.Compute())
      md.setSpeeds((currentSpeed + speed_O), -(currentSpeed - speed_O));
  }
  initializeMotor_End();
  initializeRightTurnEnd();
}

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
  while (abs(diff) >= 0.4 && getRightIR1_Block() == getRightIR2_Block() && rotated < 15) {
    rotated++;
    if (diff > 0) {
      rotateLeft(abs(diff * 5));
      diff = getRightIR1() - getRightIR2();
      if (getRightIR1_Block() != getRightIR2_Block()) {
        rotateRight(abs(diff * 4));
        diff = getRightIR1() - getRightIR2();
      }
    } else {
      rotateRight(abs(diff * 5));
      diff = getRightIR1() - getRightIR2();
      if (getRightIR1_Block() != getRightIR2_Block()) {
        rotateLeft(abs(diff * 4));
        diff = getRightIR1() - getRightIR2();
      }
    }
    delay(1);
  }
  delay(2);
}

void alignFront() {
  delay(2);
  double diff_dis;
  int moved = 0;
  double previous_turn = 0;
  if (getFrontIR1_Block() != 1 || getFrontIR3_Block() != 1 ) {
    do {
      diff_dis = getMin(getFrontIR1(), getFrontIR2(), getFrontIR3()) - DIST_WALL_CENTER_BOX;
      if (diff_dis > 0) {
        moveForwardCalibrate(abs(diff_dis));
      } else {
        moveBackwardsCalibrate(abs(diff_dis));
      }
      delay(2);
      diff_dis = getMin(getFrontIR1(), getFrontIR2(), getFrontIR3()) - DIST_WALL_CENTER_BOX;
      moved++;
    } while (abs(diff_dis) > 0.2 && moved < 15);
    return;
  }
  delay(2);
  moved = 0;
  double diff = getFrontIR1() - getFrontIR3();
  while (abs(diff) >= 0.4 && moved < 15) {
    moved++;
    previous_turn = abs(diff * 5);
    if (diff > 0) {
      rotateLeft(previous_turn);
      diff = getFrontIR1() - getFrontIR3();
      if (getFrontIR1_Block() != getFrontIR3_Block()) {
        rotateRight(previous_turn);
        break;
      }
    } else {
      rotateRight(previous_turn);
      diff = getFrontIR1() - getFrontIR3();
      if (getFrontIR1_Block() != getFrontIR3_Block()) {
        rotateRight(previous_turn);
        break;
      }
    }
    delay(2);
  }
  delay(2);
  moved = 0;
  do {
    diff_dis = getMin(getFrontIR1(), getFrontIR2(), getFrontIR3()) - DIST_WALL_CENTER_BOX;
    if (diff_dis > 0) {
      moveForwardCalibrate(abs(diff_dis));
    } else {
      moveBackwardsCalibrate(abs(diff_dis));
    }
    delay(2);
    diff_dis = getMin(getFrontIR1(), getFrontIR2(), getFrontIR3()) - DIST_WALL_CENTER_BOX;
    moved++;
  } while (abs(diff_dis) > 0.2 && moved < 20);
  moved = 0;
  delay(2);
  diff = getFrontIR1() - getFrontIR3();
  while (abs(diff) >= 0.4 && moved < 15) {
    moved++;
    previous_turn = abs(diff * 5);
    if (diff > 0) {
      rotateLeft(previous_turn);
      diff = getFrontIR1() - getFrontIR3();
      if (getFrontIR1_Block() != getFrontIR3_Block())
        rotateRight(previous_turn);
    } else {
      rotateRight(previous_turn);
      diff = getFrontIR1() - getFrontIR3();
      if (getFrontIR1_Block() != getFrontIR3_Block())
        rotateRight(previous_turn);
    }
    delay(2);
  }
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
  md.setSpeeds(0, 0);
  md.setBrakes(400, 400);
}

void initializeMotor_End() {
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
