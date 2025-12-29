# Coin Hunter KMP - Implementation Guide

## 🎮 מה זה?

אפליקציית משחק AR-Lite multiplatform שמדגימה את הכוח של **Kotlin Multiplatform (KMP)** ו-**Compose Multiplatform**.

### Features מרכזיות:
- ✅ **90%+ Shared Code** - כל הלוגיקה וה-UI ב-commonMain
- ✅ **Camera Background** - רקע AR עם מצלמה אמיתית
- ✅ **Haptic Feedback** - רטט על כל מטבע שנאסף
- ✅ **Sound Effects** - צלילים בזמן אמת
- ✅ **High Score** - שמירה מקומית persistent
- ✅ **Dynamic Difficulty** - מטבעות מקטינים במהלך המשחק

---

## 📁 מבנה הפרויקט

```
composeApp/src/
├── commonMain/          # 🟢 Shared code (90%+)
│   ├── game/
│   │   ├── GameState.kt         # FSM: Ready → Running → Finished
│   │   ├── Coin.kt              # Normalized coordinates (0.0-1.0)
│   │   └── GameEngine.kt        # Core logic + StateFlow
│   ├── ui/
│   │   ├── GameScreen.kt        # Main UI composable
│   │   ├── CoinOverlay.kt       # Coin animations
│   │   └── GameViewModel.kt     # ViewModel
│   └── platform/                # Expect declarations
│       ├── HapticFeedback.kt
│       ├── SoundPlayer.kt
│       ├── HighScoreStorage.kt
│       └── CameraView.kt
│
├── androidMain/         # 🤖 Android-specific (10%)
│   └── platform/        # Actual implementations
│       ├── HapticFeedback.android.kt   # Vibrator API
│       ├── SoundPlayer.android.kt      # SoundPool
│       ├── HighScoreStorage.android.kt # SharedPreferences
│       └── CameraView.android.kt       # CameraX
│
└── iosMain/             # 🍎 iOS-specific (10%)
    └── platform/        # Actual implementations
        ├── HapticFeedback.ios.kt       # UIImpactFeedbackGenerator
        ├── SoundPlayer.ios.kt          # AudioToolbox
        ├── HighScoreStorage.ios.kt     # UserDefaults
        └── CameraView.ios.kt           # AVFoundation
```

---

## 🚀 איך להריץ?

### Android

```bash
# Build APK
./gradlew assembleDebug

# Install on device/emulator
./gradlew installDebug

# Or run directly
./gradlew :composeApp:runDebugExecutable
```

APK נמצא ב: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`

### iOS

```bash
# Open Xcode project
open iosApp/iosApp.xcodeproj

# Or build from command line
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug
```

**חשוב**: הוסף ל-`iosApp/iosApp/Info.plist`:
```xml
<key>NSCameraUsageDescription</key>
<string>Coin Hunter needs camera for AR gameplay</string>
```

---

## 🎯 איך המשחק עובד?

1. **מסך Ready**: לחץ "Start Game"
2. **משחק (60 שניות)**:
   - 5 מטבעות זהב מופיעים על המסך
   - לחץ על מטבע → +10 נקודות + רטט + צליל
   - מטבע חדש נוצר מיד במקום אחר
   - כל 15 שניות: המטבעות מקטינים ב-10% (קושי ↑)
3. **סיום**: הצגת ניקוד סופי + השוואה ל-High Score

---

## 🏗️ ארכיטקטורה - KMP Best Practices

### 1. **FSM (Finite State Machine)**
```kotlin
sealed class GameState {
    data object Ready
    data class Running(timeRemaining: Int, score: Int, coins: List<Coin>)
    data class Finished(finalScore: Int, isNewHighScore: Boolean)
}
```

### 2. **Normalized Coordinates**
```kotlin
data class Coin(
    val id: String,
    val x: Float,  // 0.0 = left, 1.0 = right
    val y: Float,  // 0.0 = top, 1.0 = bottom
    val scale: Float = 1.0f
)
```
→ עובד על כל רזולוציה אוטומטית!

### 3. **Expect/Actual Pattern**
```kotlin
// commonMain
interface HapticFeedback {
    fun performLight()
}
expect fun createHapticFeedback(): HapticFeedback

// androidMain
actual fun createHapticFeedback() = AndroidHapticFeedback(context)

// iosMain
actual fun createHapticFeedback() = IOSHapticFeedback()
```

### 4. **StateFlow for Reactive UI**
```kotlin
class GameEngine {
    private val _state = MutableStateFlow<GameState>(Ready)
    val state: StateFlow<GameState> = _state.asStateFlow()
}
```

---

## 📦 Dependencies

### commonMain
- Compose Multiplatform (UI)
- Kotlin Coroutines (async)
- Lifecycle ViewModel

### androidMain
- CameraX (camera-camera2, camera-lifecycle, camera-view)

### iosMain
- AVFoundation (מצלמה)
- AudioToolbox (צלילים)

---

## 🐛 Troubleshooting

### Build נכשל?
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

### CameraX לא עובד?
בדוק ש-`AndroidManifest.xml` כולל:
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.VIBRATE" />
```

### iOS Camera לא מופיעה?
הוסף `NSCameraUsageDescription` ל-Info.plist!

---

## 💡 למה זה מושלם לתיק עבודות?

✨ **KMP Best Practices**:
- ✅ 90%+ קוד משותף (logic + UI)
- ✅ Platform-specific רק שם שצריך
- ✅ Expect/Actual נכון ונקי

🎯 **Professional Architecture**:
- ✅ FSM ברור ומתועד
- ✅ Reactive State Management (StateFlow)
- ✅ Separation of Concerns מושלם
- ✅ Clean Code + Documentation

🚀 **Demo-Ready**:
- ✅ משחק שעובד ומהנה
- ✅ AR-Lite experience
- ✅ Native feel בכל פלטפורמה

---

## 🔮 שיפורים עתידיים (אופציונלי)

- [ ] הוספת קבצי MP3 אמיתיים במקום system sounds
- [ ] Leaderboard עם Firebase/Supabase
- [ ] Power-ups ו-bonus coins
- [ ] Multiplayer mode
- [ ] שיפור אנימציות (particles, explosions)
- [ ] Desktop support (macOS/Windows/Linux)

---

## 📞 Support

נתקלת בבעיה? בדוק:
1. Build logs: `./gradlew build --stacktrace`
2. IDE diagnostics (Android Studio / Xcode)
3. GitHub Issues (אם זה open source)

---

**Built with ❤️ using Kotlin Multiplatform**
