# 🎮 Coin Hunter - Quick Start

## TL;DR - להריץ עכשיו!

### Android
```bash
./gradlew installDebug
```

### iOS
```bash
open iosApp/iosApp.xcodeproj
# לחץ ▶ Run
```

---

## 📱 איך לראות את המשחק?

### 1. התקן על Android Device
```bash
# אם יש מכשיר מחובר
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk

# או דרך Gradle
./gradlew installDebug
```

### 2. פתח את האפליקציה
- שם האפליקציה: **VirtualMoney**
- אפליקציה תבקש הרשאת מצלמה ← **אשר!**

### 3. שחק!
1. **Ready Screen**: לחץ "Start Game"
2. **רקע המצלמה יופיע** עם מטבעות זהב 🪙
3. **לחץ על המטבעות** → תרגיש רטט וצליל!
4. **60 שניות** להשיג את הניקוד הכי גבוה

---

## 🔍 מה אני אמור לראות?

### מסך Ready
```
┌─────────────────────┐
│                     │
│   💰 Coin Hunter    │
│                     │
│   ┌─────────────┐   │
│   │ Start Game  │   │
│   └─────────────┘   │
│                     │
│   Tap coins to...   │
└─────────────────────┘
```

### מסך Running (עם מצלמה)
```
┌─────────────────────┐
│ ⏱ 45    💰 130     │ ← HUD
│                     │
│  🪙[רקע המצלמה]     │ ← 5 מטבעות זהב
│         🪙          │   על רקע המצלמה
│    🪙        🪙     │
│         🪙          │
│                     │
└─────────────────────┘
```

### מסך Finished
```
┌─────────────────────┐
│                     │
│ 🎉 NEW HIGH SCORE!  │
│                     │
│   Final Score       │
│       250           │
│                     │
│   ┌─────────────┐   │
│   │  Try Again  │   │
│   └─────────────┘   │
└─────────────────────┘
```

---

## ⚠️ בעיות נפוצות

### ❌ "Camera permission required"
→ **פתרון**: אשר הרשאת מצלמה בהגדרות המכשיר

### ❌ המטבעות לא מופיעים
→ **בדוק**:
1. המצלמה עובדת? (רקע חי?)
2. המשחק ב-Running state? (יש טיימר?)

### ❌ אין רטט בלחיצה
→ **פתרון**: חלק מהמכשירים/אמולטורים לא תומכים ברטט

### ❌ iOS: Black screen
→ **פתרון**: הוסף `NSCameraUsageDescription` ל-Info.plist

---

## 🎯 טיפים למשחק

### להשיג ניקוד גבוה:
1. **התחל מהר** - אל תבזבז שניות בהתחלה
2. **לחץ במרכז המטבע** - לחיצה מדויקת
3. **שים לב לזמן** - ב-15, 30, 45 שניות המטבעות מקטינים!
4. **השתמש בשתי ידיים** - יותר מהיר

### Difficulty Curve:
- **0-15 שניות**: מטבעות גדולים (100% size)
- **15-30 שניות**: מטבעות בינוניים (90% size)
- **30-45 שניות**: מטבעות קטנים (80% size)
- **45-60 שניות**: מטבעות קטנטנים (70% size)

---

## 🛠️ Debug Mode

### לראות logs:
```bash
# Android
adb logcat | grep VirtualMoney

# iOS
# ב-Xcode: View → Debug Area → Show Debug Area
```

### לבדוק State:
הוסף breakpoint ב-`GameEngine.kt`:
```kotlin
fun collectCoin(coinId: String) {
    println("🪙 Coin collected: $coinId, Score: ${_state.value.score}")
    // ...
}
```

---

## 📸 צילום מסך

למצגת / תיק עבודות:
```bash
# Android
adb shell screencap /sdcard/coin_hunter.png
adb pull /sdcard/coin_hunter.png

# iOS
Shift+Cmd+4 (בסימולטור)
```

---

**🎮 Good luck & have fun!**
