# Virtual Money - Technical Architecture

## Overview

Virtual Money is an AR coin hunting game built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**. The architecture emphasizes code sharing, clean separation of concerns, and platform-specific optimizations for AR tracking.

### Key Architectural Principles

1. **90%+ Code Sharing** - Game logic, UI, and AR math in commonMain
2. **Platform Abstraction** - Expect/actual pattern for AR implementations
3. **Reactive State** - StateFlow for UI updates and game state
4. **Graceful Degradation** - ARCore → Sensors → 2D fallback chain
5. **Testability** - Unit tests for game logic, math, and projection

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                     │
│  (Compose Multiplatform UI - commonMain)                 │
│                                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  GameScreen  │  │ ARGameScreen │  │ ARCoinOverlay│   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
└─────────────────────────────────────────────────────────┘
                          ↓ StateFlow
┌─────────────────────────────────────────────────────────┐
│                     Business Logic                        │
│  (Game Engine & State Machine - commonMain)              │
│                                                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │              GameEngine                           │   │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐ │   │
│  │  │   Ready    │→ │  Running   │→ │  Finished  │ │   │
│  │  └────────────┘  └────────────┘  └────────────┘ │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                      AR & 3D Layer                        │
│  (Math, Projection, Tracking - commonMain)               │
│                                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Vector3D    │  │  Quaternion  │  │CoinProjector │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│                                                           │
│  ┌───────────────────────────────────────────────────┐   │
│  │        CameraProvider (expect)                    │   │
│  │  - startSession() / stopSession()                 │   │
│  │  - updatePose()                                   │   │
│  │  - poseFlow: StateFlow<Pose>                      │   │
│  └───────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          ↓ expect/actual
┌───────────────────────────┬─────────────────────────────┐
│    Android (actual)       │      iOS (actual - stub)    │
│                           │                             │
│  ┌─────────────────────┐  │  ┌─────────────────────┐   │
│  │  ARCore Provider    │  │  │  ARKit Stub         │   │
│  │  - ARCore Session   │  │  │  - Not implemented  │   │
│  │  - Sensor Fallback  │  │  │  - Returns identity │   │
│  └─────────────────────┘  │  └─────────────────────┘   │
│                           │                             │
│  ┌─────────────────────┐  │  ┌─────────────────────┐   │
│  │ Platform Services   │  │  │ Platform Services   │   │
│  │ - Haptic            │  │  │ - Haptic            │   │
│  │ - Sound             │  │  │ - Sound             │   │
│  │ - Storage           │  │  │ - Storage           │   │
│  └─────────────────────┘  │  └─────────────────────┘   │
└───────────────────────────┴─────────────────────────────┘
```

## Layer Details

### 1. Presentation Layer (UI)

**Technology:** Compose Multiplatform

**Components:**

#### GameScreen.kt (Main Menu)
- Displays game mode selection (AR Mode / 2D Mode)
- Shows high score
- Handles navigation to game screens

#### ARGameScreen.kt (AR Game UI)
- Manages AR game session
- Integrates CameraProvider for pose tracking
- Renders ARCoinOverlay with projected coins
- Displays game state (timer, score)
- Shows tracking mode indicator (AR/Sensor)

#### ARCoinOverlay.kt (Coin Rendering)
- Receives projected coins from CoinProjector
- Renders coins at 2D screen positions
- Handles hit testing for tap detection
- Scales coins based on distance

**State Flow:**
```kotlin
GameState.Ready → Start Game
    ↓
GameState.Running(timer, score, coins)
    ↓ (collect coins, timer ticks)
GameState.Running(updated state)
    ↓ (timer = 0)
GameState.Finished(finalScore, isHighScore)
```

### 2. Business Logic Layer (Game Engine)

**Technology:** Kotlin (commonMain)

#### GameEngine.kt

**Responsibilities:**
- FSM state transitions (Ready → Running → Finished)
- Game loop (60 second countdown)
- Coin spawning and management
- Score calculation
- Difficulty progression (coin shrinking)
- High score persistence

**Key Methods:**
```kotlin
fun startGame()                          // Ready → Running
fun collectCoin(coinId: String)          // Update score, respawn
fun endGame()                            // Running → Finished
fun reset()                              // Finished → Ready
```

**Coroutine Jobs:**
- `gameLoopJob` - Timer countdown (1s ticks)
- `coinMaintenanceJob` - Ensure minimum coin counts (200ms)
- `coinCleanupJob` - Remove expired penalty coins (100ms)

**State Management:**
```kotlin
private val _state = MutableStateFlow<GameState>(GameState.Ready)
val state: StateFlow<GameState> = _state.asStateFlow()
```

#### Coin.kt (Data Model)

```kotlin
data class Coin(
    val id: String,
    val bank: Bank,                      // Hapoalim, Leumi, Mizrahi, Discount
    val position: Vector3D,              // 3D world position (meters)
    val normalizedX: Float,              // 2D fallback (0.0-1.0)
    val normalizedY: Float,              // 2D fallback (0.0-1.0)
    val spawnTimeMs: Long,               // For penalty coin expiration
    val scale: Float                     // Size multiplier (difficulty)
)
```

### 3. AR & 3D Layer

#### 3D Math (commonMain)

##### Vector3D.kt
- 3D position representation
- Vector operations: add, subtract, scale, normalize
- Distance and dot product calculations
- Cross product for perpendiculars

```kotlin
data class Vector3D(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vector3D): Vector3D
    operator fun minus(other: Vector3D): Vector3D
    operator fun times(scalar: Float): Vector3D
    fun length(): Float
    fun normalized(): Vector3D
    fun dot(other: Vector3D): Float
    fun cross(other: Vector3D): Vector3D
}
```

##### Quaternion.kt
- Rotation representation (x, y, z, w)
- Euler angle conversion
- Quaternion multiplication
- Vector rotation
- Conjugate and inverse

```kotlin
data class Quaternion(val x: Float, val y: Float, val z: Float, val w: Float) {
    operator fun times(other: Quaternion): Quaternion
    fun conjugate(): Quaternion
    fun rotateVector(v: Vector3D): Vector3D

    companion object {
        fun fromEuler(pitch: Float, yaw: Float, roll: Float): Quaternion
        fun identity(): Quaternion
    }
}
```

#### Projection (commonMain)

##### CoinProjector.kt

**Purpose:** Convert 3D world positions to 2D screen coordinates

**Algorithm:**
1. **Camera Space Transform** - Apply camera rotation to coin position
2. **Perspective Projection** - Apply FOV and depth scaling
3. **Screen Normalization** - Convert to 0.0-1.0 coordinates
4. **Visibility Check** - Filter coins behind camera (z < 0)

```kotlin
object CoinProjector {
    fun projectCoins(
        coins: List<Coin>,
        cameraPose: Pose,
        screenWidth: Int,
        screenHeight: Int,
        fovDegrees: Float = 60f
    ): List<ProjectedCoin>
}
```

**Projection Math:**
```
1. To Camera Space:
   relativePos = coin.position - camera.position
   cameraSpace = camera.rotation.rotateVector(relativePos)

2. Check Visibility:
   if (cameraSpace.z >= 0) return null  // Behind camera

3. Perspective Projection:
   depth = abs(cameraSpace.z)
   fovRadians = fovDegrees * PI / 180
   scale = 1.0 / tan(fovRadians / 2)

   screenX = (cameraSpace.x / depth) * scale
   screenY = (cameraSpace.y / depth) * scale

4. Normalize to Screen:
   normalizedX = (screenX + 1.0) / 2.0
   normalizedY = (1.0 - screenY) / 2.0  // Flip Y

5. Scale by Distance:
   baseScale = coin.scale
   depthScale = 1.0 / (1.0 + depth * 0.5)
   finalScale = baseScale * depthScale
```

#### Camera Tracking (expect/actual)

##### CameraProvider.kt (commonMain - expect)

**Interface:**
```kotlin
expect class CameraProvider {
    fun startSession()
    fun stopSession()
    fun updatePose()
    val poseFlow: StateFlow<Pose>
    val trackingMode: StateFlow<String>  // "AR Mode" / "Sensor Mode"
}
```

##### CameraProvider.android.kt (Android - actual)

**Implementation Strategy:**
1. Try to initialize ARCore
2. If ARCore unavailable → fall back to SensorPoseTracker
3. Update pose every frame (60 FPS)
4. Emit tracking mode for UI indicator

**ARCore Integration:**
```kotlin
private var arSession: Session? = null

try {
    arSession = Session(context)
    arSession.configure(config)
    _trackingMode.value = "AR Mode"
} catch (e: Exception) {
    fallbackToSensors()
}
```

**updatePose() with ARCore:**
```kotlin
val frame = arSession?.update()
val arPose = frame?.camera?.pose

val position = Vector3D(
    arPose.tx(),
    arPose.ty(),
    arPose.tz()
)

val rotation = Quaternion(
    arPose.qx(),
    arPose.qy(),
    arPose.qz(),
    arPose.qw()
)

_poseFlow.value = Pose(position, rotation)
```

##### SensorPoseTracker.android.kt (Sensor Fallback)

**Sensors Used:**
- **Gyroscope** - Rotation rate
- **Accelerometer** - Linear acceleration (gravity reference)
- **Magnetometer** - Compass heading (optional)

**Algorithm:**
1. Integrate gyroscope for rotation changes
2. Use accelerometer for gravity-based pitch/roll correction
3. Combine into orientation quaternion
4. Update at sensor frequency (~100 Hz)

```kotlin
private fun onSensorChanged(event: SensorEvent) {
    when (event.sensor.type) {
        Sensor.TYPE_GYROSCOPE -> {
            // Integrate rotation rate
            val dt = (event.timestamp - lastTimestamp) / 1_000_000_000f
            val deltaQuat = Quaternion.fromEuler(
                event.values[0] * dt,  // pitch rate
                event.values[1] * dt,  // yaw rate
                event.values[2] * dt   // roll rate
            )
            currentRotation = currentRotation * deltaQuat
        }
        Sensor.TYPE_ACCELEROMETER -> {
            // Apply gravity correction
            applyGravityCorrection(event.values)
        }
    }
}
```

##### CameraProvider.ios.kt (iOS - stub)

Currently a stub that returns identity pose. Future implementation should use ARKit.

### 4. Platform Layer (Expect/Actual)

#### HapticFeedback
- Android: `Vibrator.vibrate()`
- iOS: `UIImpactFeedbackGenerator`

#### SoundPlayer
- Android: `SoundPool`
- iOS: `AVAudioPlayer`

#### HighScoreStorage
- Android: `SharedPreferences`
- iOS: `UserDefaults`

## Data Flow Example: Coin Collection in AR Mode

```
1. User taps screen at (x, y)
   ↓
2. ARCoinOverlay.onTap(x, y)
   - Hit test against projected coin bounds
   - Find coin at tap location
   ↓
3. GameEngine.collectCoin(coinId)
   - if (coin.bank == Hapoalim) score += 10
   - else score -= 15
   - Remove coin from state
   - Schedule respawn (for Hapoalim)
   ↓
4. GameEngine updates state
   - _state.value = Running(newScore, newCoins, timer)
   ↓
5. UI observes StateFlow
   - ARGameScreen recomposes
   - Score updates
   - Coins update
   ↓
6. CoinProjector.projectCoins()
   - Projects new coin list with updated positions
   ↓
7. ARCoinOverlay renders updated coins
```

## Performance Optimizations

### 1. Projection Optimization
- Project coins once per frame, not per recomposition
- Filter invisible coins early (behind camera)
- Cache projection results in `remember` block

### 2. State Flow Optimization
- Use `StateFlow` instead of `State` for better control
- Emit only when state actually changes
- Use `distinctUntilChanged()` for derived flows

### 3. Coroutine Management
- All background jobs use same CoroutineScope
- Jobs are cancelable and cleaned up on stopGame()
- Use `delay()` instead of tight loops

### 4. AR Tracking
- Update pose at 60 FPS (16.67ms per frame)
- ARCore runs on separate thread
- Sensor fusion runs at sensor frequency (~100 Hz)

### 5. Compose Recomposition
- `key()` blocks for stable coin identity
- `remember` for projection results
- `derivedStateOf` for computed values

## Testing Strategy

### Unit Tests (commonTest)

#### GameEngineTest.kt
- FSM state transitions
- Score calculations
- Coin spawning logic
- Timer functionality

#### Vector3DTest.kt
- Vector operations
- Distance calculations
- Normalization

#### QuaternionTest.kt
- Rotation composition
- Euler conversion
- Vector rotation

#### CoinProjectorTest.kt
- 3D to 2D projection
- Visibility filtering
- Depth scaling
- Screen normalization

### Integration Tests (androidTest)

#### ARGameScreenTest.kt
- UI rendering with projected coins
- Tap detection
- State updates

#### CameraProviderTest.kt
- ARCore initialization
- Sensor fallback
- Pose updates

## Build Configuration

### Gradle (composeApp/build.gradle.kts)

```kotlin
kotlin {
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64())

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        }

        androidMain.dependencies {
            implementation("com.google.ar:core:1.41.0")
            implementation("androidx.camera:camera-camera2:1.4.1")
        }
    }
}

android {
    compileSdk = 34
    defaultConfig {
        minSdk = 24  // ARCore minimum
        targetSdk = 34
    }
}
```

### AndroidManifest.xml

```xml
<!-- ARCore requirement -->
<uses-feature
    android:name="android.hardware.camera.ar"
    android:required="false" />

<!-- Camera permission -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- ARCore metadata -->
<meta-data
    android:name="com.google.ar.core"
    android:value="optional" />
```

## Future Architecture Improvements

### 1. Camera Background Rendering
Add actual camera feed background instead of black screen:

```kotlin
@Composable
fun ARGameScreen() {
    Box {
        // Camera feed background
        AndroidView(factory = { context ->
            TextureView(context).apply {
                surfaceTextureListener = arCameraRenderer
            }
        })

        // Coin overlay on top
        ARCoinOverlay(projectedCoins)
    }
}
```

### 2. iOS ARKit Implementation

Replace stub with ARKit:

```swift
// CameraProvider.ios.kt (using Kotlin/Native interop)
actual class CameraProvider {
    private val arSession = ARSession()

    actual fun startSession() {
        val config = ARWorldTrackingConfiguration()
        arSession.run(config)
    }

    actual fun updatePose() {
        val frame = arSession.currentFrame
        val transform = frame?.camera.transform

        // Convert to Pose
        val position = Vector3D(transform.columns.3)
        val rotation = Quaternion(transform.columns.0-3)

        _poseFlow.value = Pose(position, rotation)
    }
}
```

### 3. Multiplayer Architecture

Add networking layer:

```
Client A (AR)     Server          Client B (AR)
    ↓                ↓                  ↓
CoinSpawner → Sync Coins → CoinSpawner
    ↓                ↓                  ↓
Collect Coin → Update Score → Update Score
    ↓                                   ↓
  Win!              ←→               Lose
```

### 4. AR Persistence (Cloud Anchors)

Enable coin positions to persist across sessions:

```kotlin
// Create cloud anchor
val anchor = arSession.hostCloudAnchor(localAnchor)

// Share anchor ID with other players
val anchorId = anchor.cloudAnchorId

// Resolve anchor on other device
val resolvedAnchor = arSession.resolveCloudAnchor(anchorId)
```

## Resources

### Code Documentation
- All public APIs have KDoc comments
- Complex algorithms have inline comments
- Math formulas referenced in comments

### External Resources
- [ARCore Documentation](https://developers.google.com/ar)
- [Kotlin Multiplatform Guide](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Quaternion Math](https://en.wikipedia.org/wiki/Quaternion)

### Related Docs
- [AR Setup Guide](./AR_SETUP_GUIDE.md) - Development setup
- [AR Test Checklist](./AR_TEST_CHECKLIST.md) - Testing guide
- [README.md](../README.md) - Project overview
