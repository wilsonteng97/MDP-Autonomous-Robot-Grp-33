#include "DualVNH5019MotorShield.h"
#include "PinChangeInterrupt.h"

#define LEFT_ENCODER  3
#define RIGHT_ENCODER 11

// Motor timing
unsigned long currTime = 0;       // updated on every loop
unsigned long startTimeL = 0;    // start timing A interrupts
unsigned long startTimeR = 0;    // start timing B interrupts
unsigned long tickCountL = 0;     // count the A interrupts
unsigned long tickCountR = 0;     // count the B interrupts
unsigned long distanceTicksL = 0;          //distance L
unsigned long distanceTicksR = 0;          //distance R
unsigned long timeWidthL = 0;              // motor A period
unsigned long timeWidthR = 0;              // motor B period                  
unsigned long printTime = 0;

float setDistance = 150;
float setTickDistance = 0;

const unsigned long SAMPLE_COUNT = 20;

// PID   
const unsigned long SAMPLE_TIME = 1000;  // time between PID updates  
const unsigned long INT_COUNT = 20;     // sufficient interrupts for accurate timing  
double setpointInit;
double setpointL = 150;         // setpoint is rotational speed in Hz  
double inputL = 0;              // input is PWM to motors 
double prevOutputL = 0; 
double outputL = 0;             // output is rotational speed in Hz  
double setpointR = 150;         // setpoint is rotational speed in Hz  
double inputR = 0;              // input is PWM to motors  
double prevOutputR = 0;
double outputR = 0;             // output is rotational speed in Hz  
double outputSpeedL = 0;
double outputSpeedR = 0;
double KpL = 2.5, KiL = 0.1, KdL = 0.1;  
double KpR = 4, KiR = 0.1, KdR = 0.07; 
double kL1 = KpL + KiL + KdL;
double kL2 = -KpL - 2 * KdL;
double kL3 = KdL; 
double kR1 = KpR + KiR + KdR;
double kR2 = -KpR - 2 * KdR;
double kR3 = KdR; 
double errorL1 = 0;
double errorL2 = 0;
double errorL3 = 0;
double errorR1 = 0;
double errorR2 = 0;
double errorR3 = 0;
double currRPML;
double currRPMR;

/*double Kp = 0, Ki = 0, Kd = 0;
double k1 = Kp + Ki + Kd;
double k2 = -Kp - 2 * Kd;
double k3 = Kd;*/

DualVNH5019MotorShield md;


void setup() {

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

}

void loop() {
  
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
    //Serial.print("Current left time width: ");
    //Serial.print(timeWidthL); 
    //Serial.print("  Current left motor RPM: ");
    //Serial.println(currRPML);
    //Serial.print("Current right time width: ");
    //Serial.print(timeWidthR); 
    //Serial.print("  Current right motor RPM: ");
    //Serial.println(currRPMR);  
    pidController();
    Serial.print("Errors in RPM: ");
    Serial.print(errorL1); Serial.print(", "); Serial.println(errorR1);
    printTime = currTime;
    stopIfFault();
  }
  
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
  double rpmL = 0.3019 * speedL - 7.3771;
  return rpmL;
}

double calcRPMfromSpeedR(double speedR) {
  double rpmR = 0.2876 * speedR - 7.1982;
  return rpmR;
}

double calculateSpeedL(double rpm) {
  double speedL = 3.3079 * rpm + 24.723 - 5;
  return speedL;
}

double calculateSpeedR(double rpm) {
  double speedR = 3.4714 * rpm + 25.354;
  return speedR;
}

void pidController() {
  
  Serial.println("PID Controller is running...");
  Serial.print("Setpoint RPM = ");
  Serial.println(setpointInit);

  errorL1 = setpointInit - currRPML;
  errorR1 = setpointInit - currRPMR;
  
  //PID control law
  
  
  outputL = prevOutputL + kL1 * errorL1 + kL2 * errorL2 + kL3 * errorL3;
  outputR = prevOutputR + kR1 * errorR1 + kR2 * errorR2 + kR3 * errorR3;

  outputSpeedL = calculateSpeedL(outputL);
  outputSpeedR = calculateSpeedR(outputR);

  md.setSpeeds(-outputL, -outputR);

  prevOutputL = outputL;
  prevOutputR = outputR;
  
  errorL3 = errorL2;
  errorL2 = errorL1;
  errorR3 = errorR2;
  errorR2 = errorR1;
  
}

float distToTicks(float distance) {
  float ticks = distance / 18.85 * 562.25 * 2;
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
