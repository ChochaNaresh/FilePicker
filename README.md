# 📁 File Picker Library for Android

A customizable, modern media/document picker for Android with support for image and video capture, file selection, and Jetpack's ActivityResult API integration.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.chochanaresh/filepicker.svg)](https://search.maven.org/artifact/io.github.chochanaresh/filepicker)
![Build](https://github.com/ChochaNaresh/FilePicker/actions/workflows/ci.yml/badge.svg)
[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23)
![Language](https://img.shields.io/badge/language-Kotlin-orange.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-blue)

[![Get it on Google Play](https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png)](https://play.google.com/store/apps/details?id=com.nareshchocha.filepicker)
---

## 🚀 Features

- 📂 Document Picker
- 📷 Image Capture
- 🎥 Video Capture
- 🖼️ Pick Images & Videos from Gallery
- ⚙️ Fully customizable popups
- 🧩 Built-in ActivityResultContracts for Kotlin & Java

---

## 📦 Installation

### Gradle (Groovy)
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.github.chochanaresh:filepicker:<latest-version>'
}
```

### Gradle (Kotlin DSL)
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.chochanaresh:filepicker:<latest-version>")
}
```
## Version
`latest-version` = [![libVersion](https://img.shields.io/maven-central/v/io.github.chochanaresh/filepicker.svg)](https://central.sonatype.com/artifact/io.github.chochanaresh/filepicker/versions)
---

## 🎯 FilePickerResultContracts (Jetpack ActivityResult APIs)

Use the built-in contracts for simplified integration using Jetpack's `registerForActivityResult`.

### ✅ Setup Example
```kotlin
private val launcher = registerForActivityResult(FilePickerResultContracts.ImageCapture()) { result ->
    if (result.errorMessage != null) {
        Log.e("Picker", result.errorMessage ?: "")
    } else {
        val uri = result.selectedFileUri
        val filePath = result.selectedFilePath
    }
}
```

---

### 📋 Available Contracts

| Contract | Description |
|---------|-------------|
| `ImageCapture()` | Launch camera and capture image |
| `VideoCapture()` | Record video using camera |
| `PickMedia()` | Select image/video from gallery |
| `PickDocumentFile()` | Choose document(s) |
| `AllFilePicker()` | Show a popup to choose between multiple |
| `AnyFilePicker()` | Automatically selects contract based on config |

---

### 🧠 Usage Examples

#### 📷 Image Capture
```kotlin
val launcher = registerForActivityResult(FilePickerResultContracts.ImageCapture()) { result -> }
launcher.launch(ImageCaptureConfig())
```

#### 🎬 Video Capture
```kotlin
val launcher = registerForActivityResult(FilePickerResultContracts.VideoCapture()) { result -> }
launcher.launch(VideoCaptureConfig())
```

#### 🖼️ Pick Image/Video
```kotlin
val launcher = registerForActivityResult(FilePickerResultContracts.PickMedia()) { result -> }
launcher.launch(PickMediaConfig(allowMultiple = true))
```

#### 📄 Pick Documents
```kotlin
val launcher = registerForActivityResult(FilePickerResultContracts.PickDocumentFile()) { result -> }
launcher.launch(DocumentFilePickerConfig(allowMultiple = true))
```

#### 🔄 Popup UI for All Options
```kotlin
val launcher = registerForActivityResult(FilePickerResultContracts.AllFilePicker()) { result -> }
launcher.launch(PickerData())
```

#### 🧠 Dynamic Picker (AnyFilePicker)
```kotlin
val launcher = registerForActivityResult(FilePickerResultContracts.AnyFilePicker()) { result -> }
launcher.launch(ImageCaptureConfig()) // Or any config subclass
```

---

### 📘 FilePickerResult

```kotlin
data class FilePickerResult(
    val selectedFileUri: Uri? = null,
    val selectedFileUris: List<Uri>? = null,
    val selectedFilePath: String? = null,
    val selectedFilePaths: List<String>? = null,
    val errorMessage: String? = null
)
```

---

# ⚙️ Config Options

### 📄 DocumentFilePickerConfig
```kotlin
DocumentFilePickerConfig(
    popUpIcon = R.drawable.ic_file,
    popUpText = "File Media",
    allowMultiple = true,
    maxFiles = 10,
    mMimeTypes = listOf("application/pdf", "image/*"),
    resolveRealPath = true // set false to skip path resolution and receive only Uri
)
```

---

### 📷 ImageCaptureConfig
```kotlin
ImageCaptureConfig(
    popUpIcon = R.drawable.ic_camera,
    popUpText = "Camera",
    mFolder = File(cacheDir, "Images"),
    fileName = "image_${System.currentTimeMillis()}.jpg",
    isUseRearCamera = true,
    resolveRealPath = true // set false to skip path resolution and receive only Uri
)
```

---

### 🎥 VideoCaptureConfig
```kotlin
VideoCaptureConfig(
    popUpIcon = R.drawable.ic_video,
    popUpText = "Video",
    mFolder = File(cacheDir, "Videos"),
    fileName = "video_${System.currentTimeMillis()}.mp4",
    maxSeconds = 60,
    maxSizeLimit = 20L * 1024 * 1024,
    isHighQuality = true,
    resolveRealPath = true // set false to skip path resolution and receive only Uri
)
```

---

### 🖼️ PickMediaConfig
```kotlin
PickMediaConfig(
    popUpIcon = R.drawable.ic_media,
    popUpText = "Pick Media",
    allowMultiple = true,
    maxFiles = 5,
    mPickMediaType = PickMediaType.ImageAndVideo,
    resolveRealPath = true // set false to skip path resolution and receive only Uri
)
```

**PickMediaType Options**
```kotlin
PickMediaType.ImageOnly
PickMediaType.VideoOnly
PickMediaType.ImageAndVideo
```

---

### 🔗 resolveRealPath

Each picker config has its own `resolveRealPath: Boolean` property (default `true`). It is **not** on the base class — set it directly on whichever config you use.

| Value | Behaviour |
|---|---|
| `true` *(default)* | The library copies/queries the file and populates `selectedFilePath`/`selectedFilePaths` in the result. |
| `false` | Path resolution is skipped. Only `selectedFileUri`/`selectedFileUris` are returned. Use this when you only need the `Uri` and want to avoid the extra I/O cost. |

**Supported on all four configs:**
```kotlin
ImageCaptureConfig(resolveRealPath = false)
VideoCaptureConfig(resolveRealPath = false)
PickMediaConfig(resolveRealPath = false)
DocumentFilePickerConfig(resolveRealPath = false)
```

**Example — Uri-only with `PickDocumentFile`:**
```kotlin
val launcher = registerForActivityResult(FilePickerResultContracts.PickDocumentFile()) { result ->
    val uri = result.selectedFileUri   // always available
    val path = result.selectedFilePath // null when resolveRealPath = false
}
launcher.launch(DocumentFilePickerConfig(resolveRealPath = false))
```

**Example — Uri-only with `AnyFilePicker`:**
```kotlin
val launcher = registerForActivityResult(FilePickerResultContracts.AnyFilePicker()) { result ->
    val uri = result.selectedFileUri   // always available
    val path = result.selectedFilePath // null when resolveRealPath = false
}
launcher.launch(DocumentFilePickerConfig(resolveRealPath = false))
// or: launcher.launch(PickMediaConfig(resolveRealPath = false))
```

---

### 🎛️ PickerData & PopUpConfig
```kotlin
val pickerData = PickerData(
    mPopUpConfig = PopUpConfig(
        chooserTitle = "Choose Option",
        mPopUpType = PopUpType.BOTTOM_SHEET,
        mOrientation = Orientation.VERTICAL,
        cornerSize = 12f
    ),
    listIntents = listOf(
        ImageCaptureConfig(),
        VideoCaptureConfig(),
        PickMediaConfig(),
        DocumentFilePickerConfig()
    )
)
```
# Migration Guide: FilePicker Library

## Overview

This guide outlines the changes from the old code to the new `FilePicker` implementation, focusing on the transition from the Builder pattern to `ActivityResultContract`.

## Key Changes

1. **Package and Class Changes**:  
   The package has changed from `com.nareshchocha.filepickerlibrary.ui` to `com.nareshchocha.filepickerlibrary`.

2. **Removal of `Builder` Class**:  
   The `Builder` class is no longer needed. The new code utilizes `ActivityResultContract` for handling file picker actions.

3. **Introduction of `ActivityResultContracts`**:  
   File picker operations are now handled by specific `ActivityResultContract` classes, such as `ImageCapture`, `VideoCapture`, and `PickMedia`.

4. **Logging Support**:  
   A new `isLoggingEnabled` flag allows enabling logging in the contracts for debugging.

## Migration Steps

### 1. **Remove Builder Pattern**

The `Builder` class is no longer needed. You should transition to using `ActivityResultContracts` instead.

### 2. **Use `ActivityResultContracts`**

You can now handle file picker actions with specific contracts. For example:

- **Old Code**:
   ```kotlin
   fun imageCaptureBuild(mImageCaptureConfig: ImageCaptureConfig?): Intent =
       ImageCaptureActivity.getInstance(context, mImageCaptureConfig)
    ```
- **New Code**:
    ```kotlin
    val imageCaptureResult = registerForActivityResult(FilePickerResultContracts.ImageCapture()) { result ->
    // Handle result
    }
    ```

---

## 📱 Compatibility

- Android 6.0 (API 23) and above
- Supports Kotlin & Java

---

## 🙌 Contributing

Contributions are always welcome!  
Please fork and submit PRs with clear commit history.

---

## ☕ Support

- 📧 chochanaresh0@gmail.com
- 💬 Join our Slack
- [Buy me a Coffee](https://www.buymeacoffee.com/chochanaresh)

---

## 📄 License

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
