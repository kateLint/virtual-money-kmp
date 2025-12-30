# 💰 Virtual Money - AR Coin Hunter

An augmented reality coin collection game built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, demonstrating professional cross-platform development practices.

## 🎮 About The Game

Hunt Bank Hapoalim coins in augmented reality while avoiding penalty bank coins in an exciting 60-second challenge!

- 🏛️ **Bank Hapoalim** - Target coins (+10 points)
- 🏦 **Bank Leumi** - Penalty coins (-15 points)
- 💰 **Bank Mizrahi** - Penalty coins (-15 points)
- 💳 **Bank Discount** - Penalty coins (-15 points)

## ✨ Features

### AR & Gameplay
- 🎯 **AR Mode** - Hunt coins in real 3D space using your phone's camera and ARCore
- 📱 **2D Mode** - Classic screen-based gameplay for any device
- 🔄 **Automatic Fallback** - Seamlessly switches from ARCore to sensor fusion on non-AR devices
- ⏱️ **60 Second Challenge** - Fast-paced timed gameplay
- 🎚️ **Increasing Difficulty** - Coins shrink every 15 seconds for added challenge
- 🏆 **High Score Tracking** - Persistent high scores across sessions

### Technical Excellence
- ✅ **90%+ Shared Code** - All game logic and UI in commonMain
- ✅ **Haptic Feedback** - Vibration on every coin collection
- ✅ **Sound Effects** - Real-time audio feedback
- ✅ **Professional Architecture** - FSM, StateFlow, 3D math, clean separation of concerns
- ✅ **Comprehensive Testing** - Unit tests for core game logic, math, and projection

## 📋 Requirements

### For AR Mode
- Android device with ARCore support ([Check device compatibility](https://developers.google.com/ar/devices))
- Android 7.0 (API 24) or higher
- Google Play Services for AR installed
- Camera permission

### For 2D/Sensor Mode
- Any Android device
- Android 7.0 (API 24) or higher
- Gyroscope sensor recommended for sensor fallback mode

## 🎯 How To Play

### AR Mode
1. Tap "AR Mode" on the main menu
2. Grant camera permission when prompted
3. Move your phone to look around in 3D space
4. Tap blue Bank Hapoalim coins to collect them (+10 points)
5. Avoid tapping other bank coins (-15 points, disappear after 2 seconds)
6. Coins appear at different distances (0.5m - 3.5m from you)
7. Try to beat your high score in 60 seconds

**AR Tracking Modes:**
- **ARCore Mode** - On supported devices, uses ARCore for precise 6DoF tracking
- **Sensor Mode** - Automatic fallback using gyroscope + accelerometer on non-ARCore devices

### 2D Mode
1. Tap "2D Mode" on the main menu
2. Tap coins directly on screen to collect them
3. Same scoring rules and difficulty progression apply

## 🏗️ Architecture

### High-Level Overview
- **Kotlin Multiplatform** - Cross-platform code sharing
- **Compose Multiplatform** - Declarative UI framework
- **ARCore** - AR tracking on Android (with sensor fallback)
- **3D Math Library** - Custom Vector3D, Quaternion, projection system
- **StateFlow** - Reactive state management
- **Coroutines** - Async game loop, spawning, and cleanup

For detailed architecture documentation, see [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md)

### Code Sharing (KMP Best Practices)
* [/composeApp/src/commonMain](./composeApp/src/commonMain) - 90%+ shared code
  - [game/](./composeApp/src/commonMain/kotlin/com/keren/virtualmoney/game) - Game engine, FSM, and models
  - [ar/](./composeApp/src/commonMain/kotlin/com/keren/virtualmoney/ar) - AR math, projection, and interfaces
  - [ui/](./composeApp/src/commonMain/kotlin/com/keren/virtualmoney/ui) - Compose UI components
  - [platform/](./composeApp/src/commonMain/kotlin/com/keren/virtualmoney/platform) - Expect declarations

* [/composeApp/src/androidMain](./composeApp/src/androidMain) - Android-specific
  - ARCore implementation with sensor fallback
  - CameraX, Vibrator, SoundPool
* [/composeApp/src/iosMain](./composeApp/src/iosMain) - iOS-specific (ARKit stub, AVFoundation, UIKit, AudioToolbox)
* [/iosApp](./iosApp/iosApp) - iOS app entry point

## 🚀 Building & Running

### Android

**Quick Install:**
```bash
# Build and install on connected device
./gradlew installDebug
```

**Build APK:**
```bash
# Clean and build
./gradlew clean
./gradlew :composeApp:assembleDebug
```

APK location: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`

**Run Tests:**
```bash
# Run unit tests
./gradlew :composeApp:testDebugUnitTest

# Run specific test
./gradlew :composeApp:testDebugUnitTest --tests "com.keren.virtualmoney.ar.math.Vector3DTest"
```

### iOS
```bash
# Open in Xcode
open iosApp/iosApp.xcodeproj

# Press ▶ Run
```

**Important**: Add camera permission to `iosApp/iosApp/Info.plist`:
```xml
<key>NSCameraUsageDescription</key>
<string>Coin Hunter needs camera for AR gameplay</string>
```

**Note:** AR mode on iOS is currently a stub implementation. iOS uses standard 2D gameplay.

## 📚 Documentation

- [docs/AR_SETUP_GUIDE.md](./docs/AR_SETUP_GUIDE.md) - AR development environment setup and troubleshooting
- [docs/AR_TEST_CHECKLIST.md](./docs/AR_TEST_CHECKLIST.md) - Manual testing guide for AR features
- [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) - Detailed technical architecture
- [IMPLEMENTATION.md](./IMPLEMENTATION.md) - Full implementation guide
- [QUICK_START.md](./QUICK_START.md) - Quick start guide (Hebrew)
- [BANK_ICONS_UPDATE.md](./BANK_ICONS_UPDATE.md) - Bank icons update log

## 🛠️ Tech Stack

### Core Technologies
- **Kotlin Multiplatform (KMP)** - Cross-platform code sharing
- **Compose Multiplatform** - Declarative UI framework
- **Coroutines & Flow** - Reactive state management
- **StateFlow** - UI state management

### AR & 3D
- **ARCore 1.41.0** (Android) - Augmented reality tracking
- **Custom 3D Math** - Vector3D, Quaternion, matrix transformations
- **CoinProjector** - 3D world space to 2D screen projection
- **Sensor Fusion** - Gyroscope + accelerometer fallback

### Platform-Specific
- **Android**: ARCore, CameraX, Vibrator, SoundPool
- **iOS**: ARKit stub, AVFoundation, UIKit, AudioToolbox
- **Expect/Actual Pattern** - Platform abstractions

## 📦 Project Structure

```
composeApp/src/
├── commonMain/          # 90%+ shared code
│   ├── game/
│   │   ├── GameState.kt         # FSM states (Ready/Running/Finished)
│   │   ├── Coin.kt              # Coin model with 3D positioning
│   │   └── GameEngine.kt        # Core game logic
│   ├── ar/
│   │   ├── camera/
│   │   │   ├── CameraProvider.kt        # AR tracking interface (expect)
│   │   │   └── CameraProviderFactory.kt # Platform factory
│   │   ├── math/
│   │   │   ├── Vector3D.kt              # 3D vector operations
│   │   │   └── Quaternion.kt            # Rotation math
│   │   ├── data/
│   │   │   ├── Pose.kt                  # Camera pose (position + rotation)
│   │   │   └── ProjectedCoin.kt         # 2D screen projection result
│   │   └── projection/
│   │       └── CoinProjector.kt         # 3D to 2D projection
│   ├── ui/
│   │   ├── GameScreen.kt        # Main menu UI
│   │   ├── ARGameScreen.kt      # AR game UI
│   │   ├── ARCoinOverlay.kt     # Projected coins overlay
│   │   └── GameViewModel.kt     # ViewModel
│   └── platform/                # Expect declarations
│
├── androidMain/         # Android-specific
│   ├── ar/camera/
│   │   ├── CameraProvider.android.kt    # ARCore + sensor fallback
│   │   └── SensorPoseTracker.android.kt # Sensor fusion
│   └── platform/
│       ├── HapticFeedback.android.kt
│       ├── SoundPlayer.android.kt
│       ├── HighScoreStorage.android.kt
│       └── CameraView.android.kt
│
└── iosMain/             # iOS-specific
    ├── ar/camera/
    │   └── CameraProvider.ios.kt        # ARKit stub
    └── platform/
        ├── HapticFeedback.ios.kt
        ├── SoundPlayer.ios.kt
        ├── HighScoreStorage.ios.kt
        └── CameraView.ios.kt
```

## 🔑 Key Concepts

### Game Architecture
- **FSM (Finite State Machine)** - Clean state transitions (Ready → Running → Finished)
- **StateFlow** - Reactive UI updates from game state
- **Coroutine Jobs** - Background tasks (spawning, cleanup, maintenance)
- **Normalized Coordinates** - 0.0-1.0 positioning for screen-independent layout

### AR Architecture
- **3D World Space** - Coins positioned in meters (0.5m - 3.5m from camera)
- **Pose Tracking** - Camera position + rotation (Quaternion) updated at 60 FPS
- **Projection Pipeline** - 3D world coords → camera space → 2D screen projection
- **Automatic Fallback** - ARCore → Sensor fusion → 2D mode degradation path
- **Platform Abstraction** - Expect/Actual pattern for AR implementations

### Math & Projection
- **Vector3D** - Position, direction, and distance calculations
- **Quaternion** - Rotation representation and transformations
- **Perspective Projection** - FOV-based 3D to 2D conversion with depth scaling

## 📄 License

This project is for educational and portfolio purposes.

---

**Built with ❤️ using Kotlin Multiplatform**

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)