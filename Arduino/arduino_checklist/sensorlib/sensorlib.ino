#include <RunningMedian.h>
// reading sensor values based on running median
const int MAX_SMALL_SENSOR = 40;
const int MAX_BIG_SENSOR = 150;
const int NUM_SAMPLES_MEDIAN = 15;

RunningMedian frontIR1_Median = RunningMedian(NUM_SAMPLES_MEDIAN);
RunningMedian frontIR2_Median = RunningMedian(NUM_SAMPLES_MEDIAN);
RunningMedian frontIR3_Median = RunningMedian(NUM_SAMPLES_MEDIAN);
RunningMedian rightIR1_Median = RunningMedian(NUM_SAMPLES_MEDIAN);
RunningMedian rightIR2_Median = RunningMedian(NUM_SAMPLES_MEDIAN);
RunningMedian leftIR_1_Median = RunningMedian(NUM_SAMPLES_MEDIAN);


double frontIR1_Diffs[] = {16.00, 26.00, 35.00};//copied array blocks from senior code for block detection
double frontIR2_Diffs[] = {12.00, 23.00, 30.00};//how it works is that for a certain range like 5-15 cm obstacle is considered as one block away
double frontIR3_Diffs[] = {17.00, 26.00, 35.00};//15-25cm is 2 blocks away etc

double rightIR1_Diffs[] = {16.00, 27.00 ,35.00};
double rightIR2_Diffs[] = {16.00, 25.00 ,35.00};

double leftIR1_Diffs[] = {21.50, 31.50, 41.50, 52.50, 62.50,71.50};

double frontIR1_Value = 0, frontIR2_Value = 0, frontIR3_Value = 0;
double rightIR1_Value = 0, rightIR2_Value = 0, leftIR1_Value = 0;
int  frontIR1_Block = 0, frontIR2_Block = 0, frontIR3_Block = 0;
int rightIR1_Block = 0, rightIR2_Block = 0, leftIR1_Block = 0;


void setupSensorInterrupt() {
  //  ADCSRA &= ~(bit (ADPS0) | bit (ADPS1) | bit (ADPS2)); // clear prescaler bits
  //  //  ADCSRA |= bit (ADPS0) | bit (ADPS2);// 32  prescaler
  //  ADCSRA |= bit (ADPS2); // 16  prescaler
  //    MsTimer2::set(35, readSensors);
  //    MsTimer2::start();
}

void readSensors() {
  readFrontSensor_1();
  readFrontSensor_2();
  readFrontSensor_3();
  readRightSensor_1();
  readRightSensor_2();
  readLeftSensor_1();
}

double getFrontIR1() {
  for (int n = 0; n < NUM_SAMPLES_MEDIAN; n++) {
    readFrontSensor_1();
  }
  return frontIR1_Value;
}
double getFrontIR2() {
  for (int n = 0; n < NUM_SAMPLES_MEDIAN; n++) {
    readFrontSensor_2();
  }
  return frontIR2_Value;
}
double getFrontIR3() {
  for (int n = 0; n < NUM_SAMPLES_MEDIAN; n++) {
    readFrontSensor_3();
  }
  return frontIR3_Value;
}
double getRightIR1() {
  for (int n = 0; n < NUM_SAMPLES_MEDIAN; n++) {
    readRightSensor_1();
  }
  return rightIR1_Value;
}
double getRightIR2() {
  for (int n = 0; n < NUM_SAMPLES_MEDIAN; n++) {
    readRightSensor_2();
  }
  return rightIR2_Value;
}
double getLeftIR1() {
  for (int n = 0; n < NUM_SAMPLES_MEDIAN; n++) {
    readLeftSensor_1();
  }
  return leftIR1_Value;
}

int getFrontIR1_Block() {
  return frontIR1_Block;
}
int getFrontIR2_Block() {
  return frontIR2_Block;
}
int getFrontIR3_Block() {
  return frontIR3_Block;
}
int getRightIR1_Block() {
  return rightIR1_Block;
}
int getRightIR2_Block() {
  return rightIR2_Block;
}
int getLeftIR1_Block() {
  return leftIR1_Block;
}


void readFrontSensor_1() {
  double irDistance = 6114.6/analogRead(A3) - 3.9535;//Front left S5
  frontIR1_Median.add(irDistance);
  if (frontIR1_Median.getCount() >= NUM_SAMPLES_MEDIAN) {
    if (abs(frontIR1_Median.getHighest() - frontIR1_Median.getLowest()) > 40) {
      frontIR1_Value = MAX_SMALL_SENSOR;
    } else {
      frontIR1_Value = frontIR1_Median.getMedian();
    }
  }
    for (int m = 0; m < 3; m++) {
    if (frontIR1_Value <= frontIR1_Diffs[m]) {
      frontIR1_Block = m + 1;
      return;
    }
  }
  frontIR1_Block = 9;
}

void readFrontSensor_2() {
  double irDistance = 0;//Middle S1
  if (analogRead(A1)>210)
   irDistance=6149.8/analogRead(A1)-3.7914;
  else
    irDistance=4252.5/analogRead(A1) + 4.5429; 
  frontIR2_Median.add(irDistance);
  if (frontIR2_Median.getCount() >= NUM_SAMPLES_MEDIAN) {
    if (abs(frontIR2_Median.getHighest() - frontIR2_Median.getLowest()) > 40) {
      frontIR2_Value = MAX_SMALL_SENSOR;
    } else {
      frontIR2_Value = frontIR2_Median.getMedian();
    }
  }
  for (int m = 0; m < 3; m++) {
    if (frontIR2_Value <= frontIR2_Diffs[m]) {
      frontIR2_Block = m + 1;
      return;
    }
  }
  frontIR2_Block = 9;  
}

void readFrontSensor_3() {
  double irDistance = 7211.6/analogRead(A0) - 4.9214;//Front right S4
  frontIR3_Median.add(irDistance);
  if (frontIR3_Median.getCount() >= NUM_SAMPLES_MEDIAN) {
    if (abs(frontIR3_Median.getHighest() - frontIR3_Median.getLowest()) > 40) {
      frontIR3_Value = MAX_SMALL_SENSOR;
    } else {
      frontIR3_Value = frontIR3_Median.getMedian();
    }
  }
    for (int m = 0; m < 3; m++) {
    if (frontIR3_Value <= frontIR3_Diffs[m]) {
      frontIR3_Block = m + 1;
      return;
    }
  }
  frontIR3_Block = 9;
}

void readRightSensor_1() {
  double irDistance = 6047.8/analogRead(A2) - 3.868;//right back s2
  rightIR1_Median.add(irDistance);
  if (rightIR1_Median.getCount() >= NUM_SAMPLES_MEDIAN) {
    if (abs(rightIR1_Median.getHighest() - rightIR1_Median.getLowest()) > 40) {
      rightIR1_Value = MAX_SMALL_SENSOR;
    } else {
      rightIR1_Value = rightIR1_Median.getMedian();
    }
  }
    for (int m = 0; m < 3; m++) {
    if (rightIR1_Value <= rightIR1_Diffs[m]) {
      rightIR1_Block = m + 1;
      return;
    }
  }
  rightIR1_Block = 9;
}

void readRightSensor_2() {
  double irDistance = 6268.4/analogRead(A5)- 3.9301;//right front s3
  rightIR2_Median.add(irDistance);
  if (rightIR2_Median.getCount() >= NUM_SAMPLES_MEDIAN) {
    if (abs(rightIR2_Median.getHighest() - rightIR2_Median.getLowest()) > 40) {
      rightIR2_Value = MAX_SMALL_SENSOR;
    } else {
      rightIR2_Value = rightIR2_Median.getMedian();
    }
  }
    for (int m = 0; m < 3; m++) {
    if (rightIR2_Value <= rightIR2_Diffs[m]) {
      rightIR2_Block = m + 1;
      return;
    }
  }
  rightIR2_Block = 9;
}

void readLeftSensor_1() {
  double irDistance=13604/analogRead(A4) - 5.5009; //Long range sensor left A4
  leftIR_1_Median.add(irDistance);
  if (leftIR_1_Median.getCount() >= NUM_SAMPLES_MEDIAN) {
    if (abs(leftIR_1_Median.getHighest() - leftIR_1_Median.getLowest()) > 40) {
      leftIR1_Value = MAX_BIG_SENSOR;
    } else {
      leftIR1_Value = leftIR_1_Median.getMedian();
    }
  }
  for (int m = 0; m < 6; m++) {
    if (leftIR1_Value <= leftIR1_Diffs[m]) {
      leftIR1_Block = m + 1;
      return;
    }
  }
  leftIR1_Block = 9;
}
