# AR Mode Setup Guide

## Prerequisites

- **Android Studio** Arctic Fox (2020.3.1) or newer
- **Android SDK 24+** (Android 7.0 Nougat or higher)
- **Physical Android device** with ARCore support
- **USB cable** for device connection
- **Google Play Services for AR** installed on device

## Development Environment Setup

### 1. Install ARCore Dependency

ARCore is automatically included via Gradle dependency in `composeApp/build.gradle.kts`:

```kotlin
androidMain.dependencies {
    // ARCore for augmented reality
    implementation("com.google.ar:core:1.41.0")
}
```

No additional SDK installation required - Gradle handles everything.

### 2. Enable Developer Options on Device

1. Go to **Settings** → **About Phone**
2. Tap **Build Number** 7 times to enable Developer Mode
3. Go back to **Settings** → **Developer Options**
4. Enable **USB Debugging**

### 3. Install Google Play Services for AR

On your Android device:

1. Open **Google Play Store**
2. Search for **"Google Play Services for AR"**
3. Install or update to the latest version

Alternatively, check if already installed:
```bash
adb shell pm list packages | grep arcore
```

Expected output: `package:com.google.ar.core`

### 4. Connect Device via USB

Connect your device and verify connection:

```bash
# List connected devices
adb devices

# Expected output:
# List of devices attached
# ABC123XYZ    device
```

If device shows as "unauthorized", check your phone for USB debugging authorization prompt.

### 5. Build and Install

**Quick install:**
```bash
./gradlew installDebug
```

**Manual APK install:**
```bash
# Build APK
./gradlew :composeApp:assembleDebug

# Install APK
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

**Reinstall (if already installed):**
```bash
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

## Camera Permission

The app will request camera permission on first launch to the AR mode screen.

**Manually grant permission via ADB:**
```bash
adb shell pm grant com.keren.virtualmoney android.permission.CAMERA
```

**Check permission status:**
```bash
adb shell dumpsys package com.keren.virtualmoney | grep CAMERA
```

**Reset all app permissions:**
```bash
adb shell pm clear com.keren.virtualmoney
```

## Testing AR Mode

### On ARCore-Supported Device

When running on a device with ARCore support:

- AR mode uses **ARCore** for precise 6DoF (6 degrees of freedom) tracking
- Bottom-left indicator shows **"AR Mode"**
- Best experience with good lighting conditions
- More stable and accurate tracking

**Check ARCore compatibility:** [ARCore supported devices](https://developers.google.com/ar/devices)

### On Non-ARCore Device

When running on a device without ARCore:

- Automatic fallback to **sensor fusion** (gyroscope + accelerometer)
- Bottom-left indicator shows **"Sensor Mode"**
- Uses device motion sensors for tracking
- Less precise than ARCore but still functional

**Check sensor availability:**
```bash
adb shell dumpsys sensorservice
```

Look for: `Gyroscope`, `Accelerometer`, `Magnetometer`

## Troubleshooting

### ARCore Not Working

**Problem:** AR mode doesn't start or crashes

**Solutions:**

1. **Check ARCore installation:**
```bash
adb shell pm list packages | grep arcore
```

Expected: `package:com.google.ar.core`

2. **Update Google Play Services for AR:**
   - Open Play Store on device
   - Update "Google Play Services for AR"

3. **Install ARCore APK manually:**
   - Download ARCore APK from [APKMirror](https://www.apkmirror.com/apk/google-inc/arcore/)
   - Install via ADB:
```bash
adb install google-ar-core.apk
```

4. **Check device compatibility:**
   - Visit [ARCore supported devices](https://developers.google.com/ar/devices)
   - Verify your device is listed

### Camera Permission Denied

**Problem:** App doesn't request camera permission or shows "Permission denied"

**Solutions:**

1. **Grant permission manually:**
```bash
adb shell pm grant com.keren.virtualmoney android.permission.CAMERA
```

2. **Reset app data:**
```bash
adb shell pm clear com.keren.virtualmoney
```

3. **Check permission in Settings:**
   - Settings → Apps → Virtual Money → Permissions → Camera → Allow

### Sensor Fallback Not Working

**Problem:** On non-ARCore device, sensor mode doesn't track movement

**Solutions:**

1. **Check sensor availability:**
```bash
adb shell dumpsys sensorservice
```

2. **Calibrate sensors:**
   - Move device in figure-8 pattern
   - Place on flat surface for 10 seconds

3. **Check sensor permissions:**
```bash
adb shell dumpsys package com.keren.virtualmoney | grep SENSOR
```

### Performance Issues

**Problem:** Lag, stuttering, or low frame rate

**Solutions:**

1. **Close background apps** to free memory
2. **Ensure good lighting** - ARCore needs light to track
3. **Reduce screen brightness** to save battery
4. **Disable battery saver mode** - can throttle AR tracking
5. **Clear app cache:**
```bash
adb shell pm clear com.keren.virtualmoney
```

### App Crashes on Launch

**Problem:** App crashes immediately or on AR mode selection

**Solutions:**

1. **Check logcat for errors:**
```bash
adb logcat | grep "VirtualMoney"
```

2. **Verify Android version:**
```bash
adb shell getprop ro.build.version.sdk
```

Expected: 24 or higher (Android 7.0+)

3. **Reinstall app:**
```bash
adb uninstall com.keren.virtualmoney
./gradlew installDebug
```

## Development Tips

### Real-Time Debugging

**Monitor app logs:**
```bash
adb logcat | grep -E "VirtualMoney|ARCore|Sensor"
```

**Monitor frame rate:**
```bash
adb shell dumpsys gfxinfo com.keren.virtualmoney
```

### Testing on Emulator

**Important:** ARCore **does not work** on Android Emulator. You must use a physical device.

The app will automatically fall back to 2D mode on emulator.

### Testing Sensor Fallback

To test sensor fallback mode on an ARCore-supported device:

1. Temporarily disable ARCore:
```bash
adb shell pm disable-user com.google.ar.core
```

2. Run app - should use sensor mode

3. Re-enable ARCore:
```bash
adb shell pm enable com.google.ar.core
```

## Architecture Overview

### AR Layer Structure

```
CameraProvider (expect/actual)
├── Android: ARCore implementation
│   ├── ARCore session management
│   ├── ARCore frame updates
│   └── Sensor fusion fallback (SensorPoseTracker)
└── iOS: ARKit stub (not implemented)

Pose Tracking (60 FPS)
├── Position: Vector3D(x, y, z)
└── Rotation: Quaternion(x, y, z, w)

Coin Projection Pipeline
├── 3D World Space (coins at 0.5m - 3.5m)
├── Camera Space Transform (apply camera rotation)
├── Perspective Projection (apply FOV)
└── 2D Screen Coordinates (normalized 0.0-1.0)

UI Layer
├── ARGameScreen (composable with camera + overlay)
└── ARCoinOverlay (renders projected coins)
```

### Key Files

**Common (Shared):**
- `ar/camera/CameraProvider.kt` - AR tracking interface (expect declaration)
- `ar/math/Vector3D.kt` - 3D vector math operations
- `ar/math/Quaternion.kt` - Rotation math and transformations
- `ar/projection/CoinProjector.kt` - 3D to 2D projection logic
- `ar/data/Pose.kt` - Camera pose data class
- `ar/data/ProjectedCoin.kt` - Projected coin result
- `ui/ARGameScreen.kt` - AR game UI
- `ui/ARCoinOverlay.kt` - Coin rendering overlay
- `game/GameEngine.kt` - Game logic (mode-agnostic)

**Android (Actual):**
- `ar/camera/CameraProvider.android.kt` - ARCore + sensor implementation
- `ar/camera/SensorPoseTracker.android.kt` - Sensor fusion fallback
- `ar/camera/CameraProviderFactory.android.kt` - Platform factory

**iOS (Stub):**
- `ar/camera/CameraProvider.ios.kt` - Stub implementation
- `ar/camera/CameraProviderFactory.ios.kt` - Platform factory

### Data Flow

```
1. Camera/Sensors
   ↓
2. CameraProvider.updatePose()
   ↓
3. poseFlow emits new Pose
   ↓
4. GameEngine observes pose
   ↓
5. CoinProjector.projectCoins(coins, pose, screenSize)
   ↓
6. ARCoinOverlay renders projected coins
   ↓
7. User taps coin
   ↓
8. Hit test against projected bounds
   ↓
9. GameEngine.collectCoin()
```

## Next Steps for Development

### Immediate Improvements

1. **Add Camera Background** - Show actual camera feed instead of black background
   - Use `AndroidView` with `TextureView` for ARCore camera feed
   - Render camera frames as background layer

2. **Haptic Feedback** - Add vibration on coin collect (platform implemented, needs integration)

3. **Sound Effects** - Add audio feedback (platform implemented, needs integration)

### Future Enhancements

4. **iOS ARKit Support** - Implement ARKit tracking for iOS
   - Replace stub with real ARKit session
   - Use ARSCNView or ARView for rendering

5. **Multiplayer** - Add competitive AR gameplay
   - Share coin positions across devices
   - Real-time score synchronization

6. **AR Anchors** - Persist coin positions in real world
   - Use ARCore Cloud Anchors
   - Share anchors between sessions

7. **Occlusion** - Hide coins behind real objects
   - Use ARCore Depth API
   - Requires device with depth sensor

## Resources

### Official Documentation
- [ARCore Documentation](https://developers.google.com/ar)
- [ARCore Supported Devices](https://developers.google.com/ar/devices)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)

### Tutorials
- [ARCore Quickstart (Android)](https://developers.google.com/ar/develop/java/quickstart)
- [ARCore Best Practices](https://developers.google.com/ar/develop/best-practices)
- [Sensor Fusion Basics](https://developer.android.com/guide/topics/sensors/sensors_motion)

### Tools
- [Android Debug Bridge (ADB)](https://developer.android.com/studio/command-line/adb)
- [Logcat](https://developer.android.com/studio/debug/am-logcat)
- [ARCore Scene Viewer](https://developers.google.com/ar/develop/scene-viewer)

## Support

For issues or questions:
1. Check the [AR Test Checklist](./AR_TEST_CHECKLIST.md)
2. Review troubleshooting section above
3. Check logcat for error messages
4. Verify device compatibility with ARCore
