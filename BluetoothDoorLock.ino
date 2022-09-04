#include <stdlib.h>
#include "Arduino.h"
#include "Servo.h"
#include <SoftwareSerial.h>

//Constants
const int baud = 9600;
const int charsPerMAC = 12;
const int ledPin = 6;
const int servoPin = 4;
const int transferPinBT = 13;
const int receivePinBT = 8;
const int transferPinFP = 9;
const int receivePinFP = 12;

//Fields
bool verifiedUser = true;

//To verify the correct device is authenticating via their MAC Address
char* MAC;

//Circuit elements
Servo servo;
SoftwareSerial bluetooth(receivePinBT, transferPinBT);
SoftwareSerial fingerprint(transferPinFP, receivePinFP);

void setup() {
  servo.attach(servoPin);
  pinMode(transferPinBT, OUTPUT);
  pinMode(receivePinBT, INPUT);
  Serial.begin(baud);
  bluetooth.begin(baud);
  lock();
}

/**
 * Processes data transfer continuously. If an 's' is sent, person has been authenticated and servo will unlock. If an 'o' is sent, 
 * person has NOT been authenticated, and servo will lock. Arduino will send a message back indicating if the servo switched 
 * into either locked or unlocked position.
 */
void loop() {
  //Set MAC address once; remains on heap
  if (MAC == NULL) {
    while (bluetooth.available() == 0) {
      //Guaranteed to exit once MAC is sent
      //add a timer either here or on kotlin end?
    }
    MAC = readMACAddress(); //Cool now NEVER free() this
  }

  //Start reading text down UART
  if (bluetooth.available() > 0) {
    //Filter out new lines, terminal characters, and the like
    char c = bluetooth.read();
    //Valid character, keep going
    if (verifiedUser && c == 'o') {
      lock();
      bluetooth.println("Locked.");
    } else if (verifiedUser && c == 's') {
      unlock();
      bluetooth.println("Unlocked.");
    } else {
      //MAC address incoming
      char* newMAC = readMACAddress();
      if (strcmp(MAC, newMAC) != 0) {
        bluetooth.println("Invalid device. Locked.");
        lock();
        verifiedUser = false;
      } else {
        verifiedUser = true;
      }
      free(newMAC);
    }
    delay(1);
  }
}

char* readMACAddress() {
  char* newMAC = (char*) malloc(sizeof(char) * charsPerMAC);
  int numChars = 0;
  while (bluetooth.available() > 0) {
    //Check if it's a hex digit; then append to newMAC
    char c = bluetooth.read();
    if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
      *(newMAC + (numChars++)) = c;
    }
    delay(1); //Because serial comms need time to catch up
  }
  return newMAC;
}

void lock() {
  servo.write(10);
  digitalWrite(ledPin, LOW); //LED off
}

void unlock() {
  servo.write(100);
  digitalWrite(ledPin, HIGH); //LED on
}
