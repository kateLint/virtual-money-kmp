# 🔧 UI/UX Issues to Fix

**Priority**: HIGH  
**Date**: January 16, 2026

---

## 📋 Issues Reported

### 1. **Settings Screen Not Scrollable** (HIGH PRIORITY)
**Problem**: Cannot see all settings data - content is cut off  
**Solution**: Wrap SettingsScreen content in `Column` with `Modifier.verticalScroll()`  
**File**: `SettingsScreen.kt`  
**Estimated Time**: 2 minutes

---

### 2. **Theme Selection - Remove Level Restrictions** (MEDIUM PRIORITY)
**Problem**: Users cannot choose themes regardless of level (should be all unlocked)  
**Solution**: 
- Option A: Set all theme unlock levels to 0
- Option B: Bypass level check in ThemeManager
- Option C: Add "unlock all" debug mode  
**Files**: `ThemeManager.kt`, `GameTheme.kt`  
**Estimated Time**: 5 minutes

---

### 3. **"Coin Hunter" Title Animation Poor** (MEDIUM PRIORITY)
**Problem**: Title animation on main screen looks basic/ugly  
**Solution**: 
- Add gradient text effect
- Add subtle pulse/glow animation
- Add particle effects around text
- Better typography with custom font  
**File**: `MainMenuScreen.kt`  
**Estimated Time**: 10 minutes

---

### 4. **Game Exit Navigation Issue** (HIGH PRIORITY)
**Problem**: Pressing X to exit Classic mode goes to Main Menu, should go to Single Player Menu  
**Solution**: Update `onExit` callback to navigate to Single Player Menu instead of Main Menu  
**Files**: `App.kt`, `GameplayScreen.kt`  
**Estimated Time**: 3 minutes

---

### 5. **Coins Clustered in One Spot** (CRITICAL GAMEPLAY ISSUE)
**Problem**: All coins spawn in the same position (clustered in one dot)  
**Solution**: Fix coin spawning algorithm to properly randomize positions
- Check `Coin.createRandom3D()` and `Coin.createRandom2D()`
- Ensure proper position distribution
- Test in AR and 2D modes  
**Files**: `Coin.kt`, `GameEngine.kt`  
**Estimated Time**: 15 minutes

---

## 🎯 Implementation Priority

1. **Coins clustered** (CRITICAL - breaks gameplay)
2. **Settings not scrollable** (blocks access to features)
3. **Game exit navigation** (UX annoyance)
4. **Title animation** (visual polish)
5. **Theme unlocking** (nice-to-have)

---

## ✅ Action Plan

### Step 1: Fix Coin Positions (CRITICAL)
```kotlin
// Coin.kt - Ensure proper randomization
fun createRandom3D() {
    // Check: distance range is reasonable
    // Check: angles are properly distributed
    // Check: no hardcoded positions
}
```

### Step 2: Make Settings Scrollable
```kotlin
// SettingsScreen.kt
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()) // ADD THIS
) {
    // ... settings content
}
```

### Step 3: Fix Exit Navigation
```kotlin
// App.kt - Update navigation callback
onExit = { 
    currentScreen = Screen.SINGLE_PLAYER_MENU // Not MAIN_MENU
}
```

### Step 4: Improve Title Animation
```kotlin
// MainMenuScreen.kt
Text(
    text = "COIN HUNTER",
    modifier = Modifier
        .glow() // Add glow effect
        .pulse() // Add pulse animation
    style = TextStyle(
        brush = Brush.linearGradient(...) // Gradient text
    )
)
```

### Step 5: Unlock All Themes
```kotlin
// ThemeManager.kt or GameTheme.kt
fun isThemeUnlocked(): Boolean {
    return true // Temporarily unlock all
    // OR set all unlockLevel = 0
}
```

---

## 📊 Testing Checklist

After fixes:
- [ ] Coins appear in different positions (2D mode)
- [ ] Coins appear in different positions (AR mode)
- [ ] Settings screen scrolls to bottom
- [ ] All settings visible
- [ ] Exit from Classic → Single Player Menu
- [ ] Exit from Blitz → Single Player Menu
- [ ] Exit from Survival → Single Player Menu
- [ ] Title looks professional
- [ ] All themes selectable
- [ ] Theme changes apply correctly

---

## 🚀 Next Steps

1. Check build completion (`installDebug`)
2. Test new icon on device
3. Fix issues in priority order
4. Test each fix
5. Final build and validation

---

**Status**: Ready to implement  
**Blocking**: None  
**Risk**: Low (all straightforward fixes)
