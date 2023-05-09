
# File Picker Library for Android


[![](https://jitpack.io/v/ChochaNaresh/FilePicker.svg)](https://jitpack.io/#ChochaNaresh/FilePicker)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
![Language](https://img.shields.io/badge/language-Kotlin-orange.svg)
![Language](https://img.shields.io/badge/Kotlin-1.8.21-blue)

This library is designed to simplify the process of selecting and retrieving media files from an Android device, and supports media capture for images and videos.

# Features
* Handle Runtime Permissions
* Retrieve Result as Uri, File Path as String
* Custom Popup Or BottomSheet with custom text and icon and custom layout file for items
* Capture Image / Video
* Pick Image / Video
* Pick Any File with Mimetype
* All multiple file to pick

# Usage
```groovy
	allprojects {
	   repositories {
	      	mavenCentral() // For FilePicker library, this line is enough. Although, it has been published on jitpack as well
           	maven { url "https://jitpack.io" }  //Make sure to add this in your project
	   }
	}
```

```groovy
   dependencies {
             // ...
	        implementation 'com.github.ChochaNaresh:FilePicker:$libVersion'
            // ...
	}
```
Where `$libVersion` = [![libVersion](https://img.shields.io/github/release/ChochaNaresh/FilePicker/all.svg?style=flat-square)](https://github.com/ChochaNaresh/FilePicker/releases)


# Customization
**If you want to Multiple option with BottomSheet Or Dialog:**
```kotlin
    FilePicker.Builder(this)
                    .setPopUpConfig()
                    .addPickDocumentFile()
                    .addImageCapture()
                    .addVideoCapture()
                    .addPickMedia()
                    .build()
```
**with custom PopUp Config**
```kotlin
    //..
    setPopUpConfig(
        PopUpConfig(
            chooserTitle = "Choose Profile",
            // layoutId = 0, custom layout 
            mPopUpType = PopUpType.BOTTOM_SHEET,// PopUpType.BOTTOM_SHEET Or PopUpType.DIALOG
            mOrientation = RecyclerView.VERTICAL // RecyclerView.VERTICAL or RecyclerView.HORIZONTAL
        )
    )
    //..
    .build()

```


# Compatibility
* Library - Android Lollipop 5.0+ (API 21)
* Sample - Android Lollipop 5.0+ (API 21)