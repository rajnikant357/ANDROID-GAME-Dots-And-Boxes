# Dots and Boxes

<p align="center">
  <strong>A Prometrion product</strong>
</p>

<p align="center">
  <a href="https://github.com/rajnikant357/ANDROID-GAME-Dots-Boxes/releases/download/Dots_and_Boxes/Dots.and.boxes.apk">
    <img src="https://img.shields.io/badge/DOWNLOAD%20THIS%20APP-FF2424?style=for-the-badge&logo=android&logoColor=white" alt="Download this app" height="56">
  </a>
</p>


Dots and Boxes is an Android take on the classic pencil-and-paper game. This app is made by **Prometrion**.

## Set up the project

### Requirements

- [Android Studio](https://developer.android.com/studio)
- Android SDK 36.1 (install it through Android Studio's SDK Manager if it is not already installed)
- JDK 17 or newer
- Gradle 9.3.1 on your `PATH` (for command-line builds)

### Open and run in Android Studio

1. Clone or download this repository.
2. In Android Studio, choose **Open** and select the project folder.
3. Allow Gradle to sync and install any requested Android SDK components.
4. Choose an Android emulator or connect an Android device, then select **Run**.

### Build from the command line

From the project root, build a debug APK with Gradle:

```powershell
gradle :app:assembleDebug
```

The same command works on macOS and Linux if Gradle is on your `PATH`. To run the unit tests:

```powershell
gradle :app:testDebugUnitTest
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Tech stack

- Kotlin
- Jetpack Compose
- Android Gradle Plugin