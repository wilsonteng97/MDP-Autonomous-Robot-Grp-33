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


double frontIR1_Diffs[] = {5.50, 14.75, 24.30, 35.00};//copied array blocks from senior code for block detection
double frontIR2_Diffs[] = {5.70, 15.65, 24.75, 39.00};//how it works is that for a certain range like 5-15 cm obstacle is considered as one block away
double frontIR3_Diffs[] = {5.15, 14.55, 23.50, 36.00};//15-25cm is 2 blocks away etc

double rightIR1_Diffs[] = {6.90, 17.05, 26.75, 41.00};
double rightIR2_Diffs[] = {7.5, 17.75, 28.75, 47.00};

double leftIR1_Diffs[] = {20.25, 24.55, 33.2, 42.25, 51.2, 57.20};

double frontIR1_Value = 0, frontIR2_Value = 0, frontIR3_Value = 0;
double rightIR1_Value = 0, rightIR2_Value = 0, leftIR1_Value = 0;
int  frontIR1_Block = 0, frontIR2_Block = 0, frontIR3_Block = 0;
int rightIR1_Block = 0, rightIR2_Block = 0, leftIR1_Block = 0;



void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  Serial.setTimeout(0);
  pinMode(A3, INPUT);
  pinMode(A0, INPUT);
  pinMode(A1, INPUT);
  pinMode(A2, INPUT);
  pinMode(A4, INPUT);
  pinMode(A5, INPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  // FrontIR1 for 30-50cm if LeftIR1 not accurate enough
  // LeftIR1 for 30-70cm
    Serial.print("Front left cm: ");
    getLeftIR1();
    Serial.println(getFrontIR1());
    getFrontIR2();
    getFrontIR3();
    getRightIR1();
    getRightIR2();
    delay(1000);
   
}

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
  double irDistance = 6340.4/analogRead(A3) - 4.4509;//Front left
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
  double irDistance = 6044.1/analogRead(A1) - 2.8095;//Middle
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
  double irDistance = 7236.7/analogRead(A0) - 7.2009;//Front right
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
  double irDistance = 6776.4/analogRead(A2) - 8.3595;//right back
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
  double irDistance = 5456.6/analogRead(A5)- 3.3411;//right front
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
  double irDistance=0.0;
  if (analogRead(A4)>300){
     irDistance = 12855/analogRead(A4)- 2.1442;//Long Range
  }
  else {
   irDistance = 12855/analogRead(A4)- 4.1442;//Long Range 
  }
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
