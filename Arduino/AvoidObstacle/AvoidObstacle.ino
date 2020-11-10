void setup() {
  Serial.begin(9600);
  setupMotorEncoder();
  delay(3000);
}

void loop() {

  Serial.println("Moving forward...");
  moveForward(50);
  Serial.println("Stopped!");
  Serial.println("Moving backward...");
  moveBackward(50);
  Serial.println("Stopped!");
  
}
