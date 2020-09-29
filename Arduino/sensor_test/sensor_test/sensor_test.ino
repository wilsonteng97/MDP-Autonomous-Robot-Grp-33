

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  Serial.setTimeout(0);
  pinMode(A3, INPUT);
 int raw = analogRead(A3);
 pinMode(A0, INPUT);
 int raw1 = analogRead(A0);
 pinMode(A1, INPUT);
 int raw2 = analogRead(A1);
 pinMode(A2, INPUT);
 int raw3 = analogRead(A2);
 pinMode(A4, INPUT);
 int raw4 = analogRead(A4);
 pinMode(A5, INPUT);
 int raw5 = analogRead(A5);
    Serial.print(" A3: ");
    Serial.println(raw);
}

void loop() {
  // put your main code here, to run repeatedly:
 int raw = analogRead(A3);//Front left
 int raw1 = analogRead(A0);//Front right
 int raw2 = analogRead(A1);//Middle
 int raw3 = analogRead(A2);//right back
 int raw4 = analogRead(A4);//Long Range
 int raw5 = analogRead(A5);//right front
    Serial.print(" A2: ");
    Serial.println(raw3);
}
