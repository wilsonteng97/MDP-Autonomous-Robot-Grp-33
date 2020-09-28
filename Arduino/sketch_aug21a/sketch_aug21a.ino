#include "DualVNH5019MotorShield.h"
#include "PinChangeInterrupt.h"

#define LEFT_ENCODER 3
#define RIGHT_ENCODER 11

volatile int leftEncoderCount = 0;
volatile int rightEncoderCount = 0;

DualVNH5019MotorShield md;

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

void incLeftEncoder() {
  leftEncoderCount++;
  //Serial.print("Left encoder count: ");
  //Serial.println(leftEncoderCount);
}

void incRightEncoder() {
  rightEncoderCount++;
  //Serial.print("Right encoder count: ");
  //Serial.println(rightEncoderCount);
}

void setup() {
  Serial.begin(115200);
  md.init();
  pinMode (LEFT_ENCODER, INPUT); //set digital pin 11 as input
  pinMode (RIGHT_ENCODER, INPUT); //set digital pin 3 as input
  attachPCINT(digitalPinToPCINT(LEFT_ENCODER), incLeftEncoder, HIGH);
  attachPCINT(digitalPinToPCINT(RIGHT_ENCODER), incRightEncoder, HIGH);
}

void loop() {
  // put your main code here, to run repeatedly:
  for (int i = 50; i <= 400; i += 50)
  {
    Serial.print("Left motor speed: ");
    Serial.println(i);
    md.setM1Speed(i);
    stopIfFault();
    if (i == 400)
    {
      Serial.println("BRAKE!");
    }
    delay(2000);
    Serial.print("Left encoder count: ");
    Serial.println(leftEncoderCount);
    leftEncoderCount = 0;
  }

  md.setM1Brake(200);
  delay(1000);

  for (int i = 50; i <= 400; i += 50)
  {
    Serial.print("Right motor speed: ");
    Serial.println(i);
    md.setM2Speed(i);
    stopIfFault();
    if (i == 400)
    {
      Serial.println("BRAKE!");
    }
    delay(2000);
    Serial.print("Right encoder count: ");
    Serial.println(rightEncoderCount);
    rightEncoderCount = 0;
  }

  md.setM2Brake(200);
  delay(1000);

  for (int i = 0; i >= -400; i -= 50)
  {
    md.setSpeeds(i, i);
    stopIfFault();
    if (i == -400)
    {
      Serial.println("BRAKE!");
    }
    delay(2);
  }

  delay(2000);
  md.setBrakes(200, 200);
}
