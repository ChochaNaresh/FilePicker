
## File Picker Library for Android
This library is designed to simplify the process of selecting and retrieving media files from an Android device, and supports media capture for images and videos.

[![](https://jitpack.io/v/ChochaNaresh/FilePicker.svg)](https://jitpack.io/#ChochaNaresh/FilePicker)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
![Language](https://img.shields.io/badge/language-Kotlin-orange.svg)
![Language](https://img.shields.io/badge/Kotlin-1.8.22-blue)

### How to use
**How to add dependencies**

**Groovy**
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

**kts**
```kotlin
allprojects {
    repositories {
        mavenCentral() // For FilePicker library, this line is enough. Although, it has been published on jitpack as well
        maven { setUrl("https://jitpack.io") }  //Make sure to add this in your project
    }
}
```

```kotlin
dependencies {
    // ...
    implementation("com.github.ChochaNaresh:FilePicker:$libVersion")
    // ...
}
```
## Version
Where `$libVersion` = [![libVersion](https://img.shields.io/github/release/ChochaNaresh/FilePicker/all.svg?style=flat-square)](https://github.com/ChochaNaresh/FilePicker/releases)

## How to get result

##### Kotlin
```kotlin
private val launcher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            // Use the uri to load the image
            val uri = it.data?.data!!
            // Use the file path to set image or upload 
            val filePath= it.data.getStringExtra(Const.BundleExtras.FILE_PATH)
            //... 

            // for Multiple picks 
            // first item 
            val first = it.data?.data!!
            // other items 
            val  clipData = it.data?.clipData
            // Multiple file paths list 
            val filePaths = result.data?.getStringArrayListExtra(Const.BundleExtras.FILE_PATH_LIST) 
            //... 
        }
    }
```


##### Java
```java
private ActivityResultLauncher launcher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // Use the uri to load the image
                            Uri uri = result.getData().getData();
                            // Use the file path to set image or upload
                            String filePath = result.getData().getStringExtra(Const.BundleExtras.FILE_PATH);
                            //...

                            // for Multiple picks
                            // first item
                            Uri first = result.getData().getData();
                            // other items
                            ClipData clipData = result.getData().getClipData();
                            // Multiple file paths list
                            ArrayList<String> filePaths = result.getData().getStringArrayListExtra(Const.BundleExtras.FILE_PATH_LIST);
                            //...
                        }
                    }
                });
```
## Customization

**Multiple option with BottomSheet Or Dialog**

##### Kotlin
```kotlin
FilePicker.Builder(this)
    .setPopUpConfig()
    .addPickDocumentFile()
    .addImageCapture()
    .addVideoCapture()
    .addPickMedia()
    .build()
```
##### Java
```java
new FilePicker.Builder(this)
    .setPopUpConfig(null)
    .addPickDocumentFile(null)
    .addImageCapture(null)
    .addVideoCapture(null)
    .addPickMedia(null)
    .build();
```
**Customize popup**

#####  Kotlin
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

#####  Java
```java
//..
setPopUpConfig(
    new PopUpConfig(
        "Choose Profile",
        null,// custom layout 
        PopUpType.BOTTOM_SHEET, // PopUpType.BOTTOM_SHEET Or PopUpType.DIALOG
        RecyclerView.VERTICAL // RecyclerView.VERTICAL or RecyclerView.HORIZONTAL
    )
)
//..
.build()
```
### Pick Document Config
#####  Kotlin
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

#####  Java
```java
//..
addPickDocumentFile(
    new DocumentFilePickerConfig(
        null, // DrawableRes Id 
        null,// Title for pop item 
        true, // set Multiple pick file 
        null, // max files working only in android latest version
        mMimeTypesList, // added Multiple MimeTypes
        null,  // set Permission ask Title
        null, // set Permission ask Message
        null, // set Permission setting Title
        null // set Permission setting Messag
    )
)
//..
.build()
```

### Image Capture Config

##### kotlin
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

##### Java
```java
//..
addImageCapture(
    new ImageCaptureConfig(
            R.drawable.ic_camera, // DrawableRes Id 
            null, // Title for pop item 
            null, // set custom folder with write file permission
            null,  // set custom File name
            askPermissionTitle = null, // set Permission ask Title
            askPermissionMessage = null,// set Permission ask Message
            settingPermissionTitle = null,// set Permission setting Title
            settingPermissionMessage = null,// set Permission setting Messag
    )
)
//..
.build()
```

### Video Capture Config

##### kotlin
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

##### Java
```java
//..
addVideoCapture(
    new VideoCaptureConfig(
            R.drawable.ic_video,// DrawableRes Id 
            null, // Title for pop item 
            null, // set custom folder with write file permission
            null, // set custom File name
            null, // set video duration in seconds
            null, // set size limit 
            null, // set isHighQuality true/false
            null, // set Permission ask Title
            null, // set Permission ask Message
            null, // set Permission setting Title
            null, // set Permission setting Messag
    )
)
//..
.build()
```

### Pick Media Config
##### kotlin
```kotlin
//..
addPickMedia(
    PickMediaConfig(
        popUpIcon = R.drawable.ic_media,// DrawableRes Id 
        popUpText = "Video",
        allowMultiple = false,// set Multiple pick file 
        maxFiles = 0,// max files working only in android latest version
        mPickMediaType = ImageAndVideo,
        askPermissionTitle = null, // set Permission ask Title
        askPermissionMessage = null,// set Permission ask Message
        settingPermissionTitle = null,// set Permission setting Title
        settingPermissionMessage = null,// set Permission setting Messag
    ),
)
//..
.build()
```

##### Java
```java
//..
addPickMedia(
    new PickMediaConfig(
            R.drawable.ic_media,// DrawableRes Id 
            null, // Title for pop item 
            null, // set Multiple pick file 
            null, // max files working only in android latest version
            null, // set PickMediaTypes 
            null, // set Permission ask Title
            null, // set Permission ask Message
            null, // set Permission setting Title
            null, // set Permission setting Messag
    )
)
//..
.build()
```

### Pick Media Types

##### kotlin
```kotlin
import com.nareshchocha.filepickerlibrary.models.PickMediaType
```

##### Java
```java
import com.nareshchocha.filepickerlibrary.models.PickMediaType;
```

* PickMediaType.ImageOnly
* PickMediaType.VideoOnly
* PickMediaType.ImageAndVideo
## Use directly
### Pick Document

##### Kotlin
```kotlin
    FilePicker.Builder(this)
        .pickDocumentFileBuild(DocumentFilePickerConfig())
```
Customization
[**DocumentFilePickerConfig**](#kotlin-3)

##### Java
```java
    new FilePicker.Builder(this)
        .pickDocumentFileBuild(new DocumentFilePickerConfig(null));
```
Customization
[**DocumentFilePickerConfig**](#java-3)

### Image Capture

##### Kotlin
```kotlin
    FilePicker.Builder(this)
        .imageCaptureBuild(ImageCaptureConfig())
```
Customization
[**ImageCaptureConfig**](#kotlin-4)

##### Java
```java
    new FilePicker.Builder(this)
        .imageCaptureBuild(new ImageCaptureConfig(null));
```
Customization
[**ImageCaptureConfig**](#java-4)

### Video Capture

##### Kotlin
```kotlin
    FilePicker.Builder(this)
        .videoCaptureBuild(VideoCaptureConfig())
```
Customization
[**VideoCaptureConfig**](#kotlin-5)

##### Java
```java
    new FilePicker.Builder(this)
        .videoCaptureBuild(new VideoCaptureConfig(null));
```
Customization
[**VideoCaptureConfig**](#java-5)

### Pick Media

##### Kotlin
```kotlin
    FilePicker.Builder(this)
        .pickMediaBuild(PickMediaConfig())
```
Customization
[**PickMediaConfig**](#kotlin-6)

##### Java
```java
    new FilePicker.Builder(this)
        .pickMediaBuild(new PickMediaConfig(null));
```
Customization
[**PickMediaConfig**](#java-6)
## Proguard rules
```text
-keepclasseswithmembernames class com.nareshchocha.filepickerlibrary.models.**{
    *;
}
-keepclassmembers class * extends androidx.appcompat.app.AppCompatActivity {
    *;
}
```
## Compatibility
* Library - Android Lollipop 5.0+ (API 21)
* Sample - Android Lollipop 5.0+ (API 21)
## Contributing

Contributions are always welcome!
## Support

For support, email  chochanaresh0@gmail.com or join our Slack channel.

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/chochanaresh)

## License
```text
Copyright 2023 Naresh chocha

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    
http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
