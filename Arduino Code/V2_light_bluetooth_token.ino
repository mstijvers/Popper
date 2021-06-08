
// This code is made by Marijn Stijvers for the Final Bachelor Project; Popper. It is developed 
// at the Technical University of Eindhoven 2021. The Code makes use of 
// the Adafruit NeoPixel library and SoftwareSerial libary to communicate with an Android phone
// The code allows for a bluetooth connection after which the LED's will light up, indicating that
// there is a phone connected. With the Button the Phone can receive a signal that tha application 
// should be in an 'inactive' state, To identify this state the LED's also have another colour. 


#include <Adafruit_NeoPixel.h>
#ifdef __AVR__
#include <avr/power.h> // Required for 16 MHz Adafruit Trinket
#endif

//NeoPixels
#define PIN        6 //breadboard
//#define PIN        5//soldering
#define NUMPIXELS 12 // NeoPixel ring size

//initiate bluethooth
#include <SoftwareSerial.h>
#define bluetoothRX 2
#define bluetoothTX 3

// bluethooth state
const byte BTpin = 4;
//bluetooth connection made first time
boolean BTcon = 1;

//Button Stable Unstable state
const byte ActiveBtn = 5; //breadboard
//const byte ActiveBtn =6; //soldering
//switching between active and inactive state (send bluetooth signal
boolean ActiveBtnNow = 0;
boolean ActiveBtnPrev = 0;

//light and timing
long timer = millis();
int sampleInterval = 200;
int LEDS = 0;

SoftwareSerial myBluetooth(bluetoothRX, bluetoothTX);

// When setting up the NeoPixel library, we tell it how many pixels,
// and which pin to use to send signals.
Adafruit_NeoPixel pixels(NUMPIXELS, PIN, NEO_GRB + NEO_KHZ800);


void setup() {
  //declare inputs etc.
  pinMode(BTpin, INPUT);
  pinMode(ActiveBtn, INPUT_PULLUP);
  digitalWrite(ActiveBtn, LOW);
  digitalWrite(BTpin, LOW);


  // Open serial communications and wait for port to open:
  Serial.begin(9600);
  while (!Serial) {
    ;
  }

  // Open serial communication for the Bluetooth Serial Port
  myBluetooth.begin(9600);

  // INITIALIZE NeoPixel strip object (REQUIRED)
  pixels.begin();
  pixels.clear(); // Set all pixel colors to 'off'

}


void loop() {
  //read if there is a bluetooth connection.
  if (digitalRead(BTpin) == LOW) {
    for (int i = 0; i < NUMPIXELS; i++) { // For each pixel...
      pixels.setPixelColor(i, pixels.Color(0, 0, 0));
      pixels.show();   // Send the updated pixel colors to the hardware
    }
    Serial.println("Disconnected");
    BTcon = 1 ;
    //if there is a bluetooth connection light up.
  } else if (digitalRead(BTpin) == HIGH) {
    Serial.println("connected");
    BTconnected();
    //read state for activity button LOW == Token is active
    if (digitalRead(ActiveBtn) == LOW) {
      for (int i = 0; i < NUMPIXELS; i++) { // For each pixel...
        pixels.setPixelColor(i, pixels.Color(255, 255, 255));
        pixels.setBrightness(20);
        pixels.show();   // Send the updated pixel colors to the hardware

        //Tokenlist is active
        ActiveBtnNow = 1;
      }
      //read state for activity button HIGH == Token is not active
    } else {
      for (int i = 0; i < NUMPIXELS; i++) { // For each pixel...
        pixels.setPixelColor(i, pixels.Color(0, 0, 45));
        pixels.show();   // Send the updated pixel colors to the hardware
      }

      //Tokenlist is not active
      ActiveBtnNow = 0;
    }
  }
  //notify the bluetooth device whenever the state is shifted.
  ActiveSwitch();

}


void ActiveSwitch() {
  if (ActiveBtnNow == 0 && ActiveBtnPrev == 1) {
    myBluetooth.println("0");
    ActiveBtnPrev = ActiveBtnNow;
  } else if (ActiveBtnNow == 1 && ActiveBtnPrev == 0) {
    myBluetooth.println("1");
    ActiveBtnPrev = ActiveBtnNow;
  }
}

void LightCircle() {
  if (millis() - timer >= sampleInterval) {   //Timer: send sensor data
    timer = millis();
    pixels.setPixelColor(LEDS, pixels.Color(45, 45, 45));
    pixels.show();   // Send the updated pixel colors to the hardware
    LEDS++;
    if (LEDS == 12) {
      LEDS = 0;
      for (int i = 0; i < NUMPIXELS; i++) { // For each pixel...
        pixels.setPixelColor(i, pixels.Color(0, 0, 0));
        pixels.show();   // Send the updated pixel colors to the hardware
      }
      //data = analogRead(A0);                    //get the analog reading
      //sendDataToProcessing('A', data);          //Put the data into buffer to sent it out later.
    }
  }
}

void BTconnected() {
  if (BTcon == 1) {
    pixels.clear();
    for (int i = 0; i < NUMPIXELS; i++) { // For each pixel...
      pixels.setPixelColor(i, pixels.Color(15, 15, 15));
      pixels.show();   // Send the updated pixel colors to the hardware
      delay(50);
      BTcon = 0;
    }   
  }
}
