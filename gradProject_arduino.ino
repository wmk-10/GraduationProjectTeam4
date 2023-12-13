int echoPin = 2;
int trigPin = 3;
int buzzerPin = 4;
int i=0;
int distanceArr[500]={0,};

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  pinMode(trigPin,OUTPUT);
  pinMode(echoPin,INPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  digitalWrite(trigPin,LOW);
  digitalWrite(echoPin,LOW);
  delay(150);

  digitalWrite(trigPin,HIGH);
  delay(500);
  digitalWrite(trigPin,LOW);

  unsigned long time = pulseIn(echoPin,HIGH); 
  float distance = ((float)(340*time)/10000)/2;

  i++;
  distanceArr[i]=distance;

  //if(distance<=200,distance>=50){
  //  tone(buzzerPin,196);
  //  delay(500);
  //  noTone(buzzerPin);
  //}

  if(distanceArr[i-1]-distanceArr[i]>=600){
    tone(buzzerPin,196);
    delay(500);
    tone(buzzerPin,262);
    delay(500);
    noTone(buzzerPin);
  }
  else if(distanceArr[i-1]-distanceArr[i]>=100){
    tone(buzzerPin,196);
    delay(1000);
    tone(buzzerPin,262);
    delay(1000);
    noTone(buzzerPin);
  }

  Serial.print("Array Number [");
  Serial.print(i);
  Serial.print("] ");
  Serial.print(distance);
  Serial.println("cm");
}