# AR Mode Test Checklist

## Pre-Test Setup
- [ ] Android device with ARCore support (Pixel, Galaxy S20+, etc.)
- [ ] Google Play Services for AR installed
- [ ] Camera permission granted
- [ ] Good lighting conditions
- [ ] Clear space to move around

## Test Cases

### 1. App Launch
- [ ] App launches without crash
- [ ] Main menu displays correctly
- [ ] AR/2D mode toggle buttons visible

### 2. Permission Handling
- [ ] Camera permission requested on first launch
- [ ] Permission denial shows toast message
- [ ] App continues to work in 2D mode if denied

### 3. AR Mode Initialization
- [ ] Tap "AR Mode" button
- [ ] ARCore initializes successfully (or falls back to sensors)
- [ ] Game starts with timer and score at 0
- [ ] Hint text "Move your phone to look around!" appears

### 4. AR Tracking
- [ ] Moving phone updates coin positions
- [ ] Coins appear at different distances (close/medium/far)
- [ ] Coins scale correctly based on distance
- [ ] Rotation tracking works smoothly
- [ ] No jittering or jumping

### 5. Gameplay
- [ ] Hapoalim coins (blue) are tappable
- [ ] Tapping Hapoalim coin adds +10 points
- [ ] Penalty coins (other banks) appear
- [ ] Tapping penalty coin subtracts -15 points
- [ ] Penalty coins disappear after 2 seconds
- [ ] New coins spawn to maintain minimum counts
- [ ] Timer counts down correctly
- [ ] Score updates correctly

### 6. Difficulty Progression
- [ ] Coins get smaller every 15 seconds
- [ ] Minimum scale is 0.5x

### 7. Game End
- [ ] Game ends when timer reaches 0
- [ ] Final score displayed correctly
- [ ] High score updated if beaten
- [ ] "Play Again" works

### 8. Sensor Fallback
- [ ] On non-ARCore device, sensors are used
- [ ] "Sensor Mode" indicator shows
- [ ] Gyroscope tracking works
- [ ] Gameplay still functional

### 9. Performance
- [ ] Smooth 60 FPS
- [ ] No lag or stuttering
- [ ] Battery usage acceptable
- [ ] No memory leaks

### 10. Edge Cases
- [ ] Pause and resume work correctly
- [ ] App handles low memory
- [ ] App handles losing/regaining camera permission
- [ ] App handles ARCore crash gracefully

## Known Limitations
- Camera background is black placeholder (not actual camera feed)
- iOS AR mode not implemented (uses stub)

## Issues Found
(Document any issues discovered during testing)
