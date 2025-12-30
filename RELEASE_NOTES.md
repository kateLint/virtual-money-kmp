# Virtual Money - AR Coin Hunter - Release Notes

## Version 1.0.0 - AR Mode Launch

### New Features

**AR Mode**
- Hunt coins in real 3D space using ARCore
- Coins spawn at varying distances (0.5m - 3.5m)
- Automatic sensor fallback for non-ARCore devices
- 60 FPS tracking for smooth AR experience

**Bank Coin System**
- Collect Hapoalim coins (+10 points)
- Avoid penalty bank coins (-15 points)
- Penalty coins auto-disappear after 2 seconds

**Gameplay**
- 60-second challenge mode
- Increasing difficulty (coins shrink every 15 seconds)
- High score tracking
- AR/2D mode toggle

### Technical Implementation

- Kotlin Multiplatform architecture
- ARCore 1.41.0 integration
- Custom 3D math library (Vector3D, Quaternion)
- Perspective projection engine
- Sensor fusion fallback
- Compose Multiplatform UI

### Requirements

**AR Mode:**
- Android 7.0+ (API 24)
- ARCore-supported device
- Camera permission

**2D/Sensor Mode:**
- Android 7.0+ (API 24)
- Gyroscope sensor

### Known Limitations

- Camera background is black placeholder (actual feed not implemented)
- iOS ARKit support not implemented (iOS uses stub)
- No multiplayer support
- No sound effects or haptic feedback

### Testing

- Comprehensive unit test coverage
- Manual AR test checklist provided
- Build tested on Android 14

### Documentation

- Complete README with features and setup
- AR Setup Guide for developers
- Architecture documentation
- Troubleshooting guide

### Future Enhancements

1. Real camera background feed
2. iOS ARKit support
3. Haptic feedback on coin collection
4. Sound effects
5. Multiplayer mode
6. Achievement system
7. Power-ups and bonuses

---

For setup instructions, see [AR Setup Guide](docs/AR_SETUP_GUIDE.md)
