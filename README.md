![Logo of the project](https://github.com/mstijvers/phone-block/blob/master/app/src/main/res/drawable/ic_launcher.png?raw=true)
# Popper Final Bachelor Project
> Android Application aiming to organzine and control social media behaviour.

An Android application designed to work with physical tokens. 
The tokens connect to the application by use of bluetooth where the application allows for 
access to specific applications based on this bluetooth connection. 

## Installing / Getting started

```
git clone https://github.com/robmcelhinney/PhoneBlock.git
cd PhoneBlock
```
You can now open the project with Android Studio.
To build the app from the command line
```
./gradlew assembleDebug
```
The app will be found at `/build/outputs/apk/`

Run the emulator and drag app-debug.apk in, then you can open PhoneBlock.

![PhoneBlock homescreen](https://github.com/robmcelhinney/PhoneBlock/raw/master/readme_assets/homescreen.png)

## Features

Using Google’s Activity Recognition API, it is possible to detect a user’s activity,
such as when they are on their feet. Then the application records the device’s
accelerometer data and compares it to the model, predicting a match with
entering a car. If the match is deemed sufficiently similar, when the device is
detected to be in a vehicle, through the activity recognition API, the block is
activated. There is also an option to activate the block once the user is assumed
to be in a vehicle and is connected to a Bluetooth headset.

What's all the bells and whistles this project can perform?
* Neural Network for Activity Recognition
* Detecting When Sitting Into Car
* Blocking Other Applications
* Bluetooth Detection

![PhoneBlock notification](https://github.com/robmcelhinney/PhoneBlock/raw/master/readme_assets/notification.png)
![LSTM](https://github.com/robmcelhinney/PhoneBlock/raw/master/readme_assets/lstm.png)

## Help
- https://github.com/ricvalerio/foregroundappchecker 
- https://aqibsaeed.github.io/2016-11-04-human-activity-recognition-cnn/
- http://curiousily.com/data-science/2017/06/03/tensorflow-for-hackers-part-
6.html

## Licensing
"The code in this project is licensed under MIT license."
