
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

**Pick Document Config**
```kotlin
    //..
    addPickDocumentFile(
        DocumentFilePickerConfig(
            popUpIcon = R.drawable.ic_file,// DrawableRes Id 
            popUpText = "File Media", 
            allowMultiple = false,// set Multiple pick file 
            maxFiles = 0,// max files working only in android latest version
            mMimeTypes = listOf("*/*"),// added Multiple MimeTypes
            askPermissionTitle = null, // set Permission ask Title
            askPermissionMessage = null,// set Permission ask Message
            settingPermissionTitle = null,// set Permission setting Title
            settingPermissionMessage = null,// set Permission setting Messag
            ),
    )
    //..
    .build()

```

**Image Capture Config**
```kotlin
    //..
    addImageCapture(
        ImageCaptureConfig(
            popUpIcon = R.drawable.ic_camera,// DrawableRes Id 
            popUpText = "Camera", 
            mFolder = File(),// set custom folder with write file permission
            fileName = "image.jpg",
            askPermissionTitle = null, // set Permission ask Title
            askPermissionMessage = null,// set Permission ask Message
            settingPermissionTitle = null,// set Permission setting Title
            settingPermissionMessage = null,// set Permission setting Messag
            ),
    )
    //..
    .build()

```

**Video Capture Config**
```kotlin
    //..
    addVideoCapture(
        VideoCaptureConfig(
            popUpIcon = R.drawable.ic_video,// DrawableRes Id 
            popUpText = "Video", 
            mFolder=File(),// set custom folder with write file permission
            fileName = "video.mp4",
            maxSeconds = null,// set video duration in seconds
            maxSizeLimit = null,// set size limit 
            isHighQuality = null,// set isHighQuality true/false
            askPermissionTitle = null, // set Permission ask Title
            askPermissionMessage = null,// set Permission ask Message
            settingPermissionTitle = null,// set Permission setting Title
            settingPermissionMessage = null,// set Permission setting Messag
            ),
    )
    //..
    .build()

```


**Pick Media Config**
```kotlin
    //..
    addPickMedia(
        PickMediaConfig(
            popUpIcon = R.drawable.ic_video,// DrawableRes Id 
            popUpText = "Video", 
            allowMultiple = false,// set Multiple pick file 
            maxFiles = 0,// max files working only in android latest version
            mPickMediaType = ImageAndVideo
            askPermissionTitle = null, // set Permission ask Title
            askPermissionMessage = null,// set Permission ask Message
            settingPermissionTitle = null,// set Permission setting Title
            settingPermissionMessage = null,// set Permission setting Messag
            ),
    )
    //..
    .build()
```

**Pick Media Types**

* ImageOnly
* VideoOnly
* ImageAndVideo


# If you want to use only one
**Pick Document**
```kotlin
    FilePicker.Builder(this)
        .pickDocumentFileBuild(DocumentFilePickerConfig()) // Customization check Pick Document Config
```
**Image Capture**
```kotlin
    FilePicker.Builder(this)
        .imageCaptureBuild(ImageCaptureConfig()) // Customization check Image Capture Config
```

**Video Capture**
```kotlin
    FilePicker.Builder(this)
        .videoCaptureBuild(VideoCaptureConfig()) // Customization check Video Capture Config
```

**Pick Media**
```kotlin
    FilePicker.Builder(this)
        .pickMediaBuild(PickMediaConfig()) // Customization check Pick Media Config
```



# Compatibility
* Library - Android Lollipop 5.0+ (API 21)
* Sample - Android Lollipop 5.0+ (API 21)