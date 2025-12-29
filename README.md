# 💰 Virtual Money - Coin Hunter KMP

An AR-lite mobile game built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, demonstrating professional cross-platform development practices.

## 🎮 About The Game

Collect Bank Hapoalim coins while avoiding other bank coins in a 60-second AR-lite experience with real camera background!

- 🏛️ **Bank Hapoalim** - Good coins (+10 points)
- 🏦 **Bank Leumi** - Penalty coins (-15 points)
- 💰 **Bank Mizrahi** - Penalty coins (-15 points)
- 💳 **Bank Discount** - Penalty coins (-15 points)

## ✨ Features

- ✅ **90%+ Shared Code** - All game logic and UI in commonMain
- ✅ **AR-Lite Experience** - Real camera background with coin overlay
- ✅ **Haptic Feedback** - Vibration on every coin collection
- ✅ **Sound Effects** - Real-time audio feedback
- ✅ **Persistent High Score** - Local storage across sessions
- ✅ **Dynamic Difficulty** - Coins shrink as time progresses
- ✅ **Professional Architecture** - FSM, StateFlow, Clean Code

## 🏗️ Architecture

### Code Sharing (KMP Best Practices)
* [/composeApp/src/commonMain](./composeApp/src/commonMain) - 90%+ shared code
  - [game/](./composeApp/src/commonMain/kotlin/com/keren/virtualmoney/game) - Game engine, FSM, and models
  - [ui/](./composeApp/src/commonMain/kotlin/com/keren/virtualmoney/ui) - Compose UI components
  - [platform/](./composeApp/src/commonMain/kotlin/com/keren/virtualmoney/platform) - Expect declarations

* [/composeApp/src/androidMain](./composeApp/src/androidMain) - Android-specific (CameraX, Vibrator, SoundPool)
* [/composeApp/src/iosMain](./composeApp/src/iosMain) - iOS-specific (AVFoundation, UIKit, AudioToolbox)
* [/iosApp](./iosApp/iosApp) - iOS app entry point

## 🚀 Quick Start

### Android
```bash
# Build and install on connected device/emulator
./gradlew installDebug

# Or build APK only
./gradlew assembleDebug
```

APK location: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`

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

## 🎯 How To Play

1. **Ready Screen** - Tap "Start Game"
2. **Running (60 seconds)**:
   - 4 Bank Hapoalim coins appear on screen
   - 3 penalty bank coins appear randomly and disappear after 2 seconds
   - Tap Hapoalim → +10 points, new coin respawns in 1 second
   - Tap penalty banks → -15 points
   - Coins shrink every 15 seconds (difficulty increases)
3. **Finished** - View final score and high score

## 📚 Documentation

- [IMPLEMENTATION.md](./IMPLEMENTATION.md) - Full implementation guide
- [QUICK_START.md](./QUICK_START.md) - Quick start guide (Hebrew)
- [BANK_ICONS_UPDATE.md](./BANK_ICONS_UPDATE.md) - Bank icons update log

## 🛠️ Tech Stack

- **Kotlin Multiplatform** - Cross-platform code sharing
- **Compose Multiplatform** - Declarative UI
- **Coroutines & Flow** - Reactive state management
- **CameraX** (Android) - Camera integration
- **AVFoundation** (iOS) - Camera integration
- **Expect/Actual Pattern** - Platform-specific implementations

## 📦 Project Structure

```
composeApp/src/
├── commonMain/          # 90%+ shared code
│   ├── game/
│   │   ├── GameState.kt         # FSM states
│   │   ├── Coin.kt              # Coin model
│   │   └── GameEngine.kt        # Core game logic
│   ├── ui/
│   │   ├── GameScreen.kt        # Main UI
│   │   ├── CoinOverlay.kt       # Coin rendering
│   │   └── GameViewModel.kt     # ViewModel
│   └── platform/                # Expect declarations
│
├── androidMain/         # Android-specific
│   └── platform/
│       ├── HapticFeedback.android.kt
│       ├── SoundPlayer.android.kt
│       ├── HighScoreStorage.android.kt
│       └── CameraView.android.kt
│
└── iosMain/             # iOS-specific
    └── platform/
        ├── HapticFeedback.ios.kt
        ├── SoundPlayer.ios.kt
        ├── HighScoreStorage.ios.kt
        └── CameraView.ios.kt
```

## 🔑 Key Concepts

- **FSM (Finite State Machine)** - Clean state transitions (Ready → Running → Finished)
- **Normalized Coordinates** - 0.0-1.0 positioning for any screen size
- **StateFlow** - Reactive UI updates
- **Coroutine Jobs** - Background tasks (spawning, cleanup, maintenance)
- **Platform-Specific Abstractions** - Expect/Actual for native APIs

## 📄 License

This project is for educational and portfolio purposes.

---

**Built with ❤️ using Kotlin Multiplatform**

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)