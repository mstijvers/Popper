![Logo of the project](https://github.com/mstijvers/popper/blob/master/app/src/main/res/drawable/ic_launcher.png?raw=true)
# Popper Final Bachelor Project
> Android Application aiming to organzine and control social media behaviour.

An Android application designed to work with physical tokens. 
The tokens connect to the application by use of bluetooth where the application allows for 
access to specific applications based on this bluetooth connection. 

## Installing / Getting started

```
git clone hhttps://github.com/mstijvers/phone-block.git
cd Popper
```
You can now open the project with Android Studio.
To build the app from the command line
```
./gradlew assembleDebug
```
The app will be found at `/build/outputs/apk/`

Run the emulator and drag app-debug.apk in, then you can open PhoneBlock.


## Features

With this app and the physical tokens, one can seperate his/her digita social media spaces and connect them to a physical location. This allows for more controlled and intentional use of social media. The application can connect to two different tokens, one for social spaces and one for desk (work) spaces. 

What's all the bells and whistles this project can perform?
* Connect to a physical product through use of bluetooth
* respond on changes in the bluetooth defice
* Blocking Other Applications
* Setting applications to block for different spaces.

![Home screen](https://github.com/mstijvers/Popper/blob/master/imagesReadMe/home%20screen.PNG?raw=true)

![Premissions screen](https://github.com/mstijvers/Popper/blob/master/imagesReadMe/premissions.PNG?raw=true)

## Licensing
This code is inspired on the repository of robmcelhinney/phone-block
