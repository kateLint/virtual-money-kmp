# AR Coin Hunter - Design Document

**Date:** 2025-12-30
**Feature:** Augmented Reality Coin Hunting with Motion Tracking
**Platforms:** Android (ARCore) + iOS (ARKit) via Kotlin Multiplatform

---

## Overview

Transform Virtual Money from a 2D tap game to an immersive AR experience where players move their phone through physical space to hunt for bank logo coins floating in augmented reality.

**Key Requirements:**
- Full-screen AR camera view with coin overlay
- Real AR (ARCore/ARKit) with automatic sensor fallback
- Coins positioned in 3D space at mixed distances (0.5m - 3.5m)
- Realistic perspective scaling (distant coins appear smaller)
- Maintain all existing animations (rotation, pulse, collect effects)
- Cross-platform (Android + iOS) using KMP

---

## Architecture

### Three-Layer System

#### 1. Common Layer (Shared KMP Code)

**Data Models:**
```kotlin
data class Coin(
    val id: String,
    val position3D: Vector3D,  // 3D position in meters
    val scale: Float = 1.0f,
    val type: CoinType,
    val spawnTime: Long = System.currentTimeMillis()
)

data class Vector3D(
    val x: Float,  // Left/Right (-3m to +3m)
    val y: Float,  // Up/Down (-2m to +2m)
    val z: Float   // Forward/Back (-3m to +3m)
)

data class Pose(
    val position: Vector3D,
    val rotation: Quaternion
)

data class ProjectedCoin(
    val coin: Coin,
    val screenX: Float,
    val screenY: Float,
    val apparentScale: Float,
    val distance: Float
)
```

**Core Components:**
- `ARCoinManager` - Manages 3D coin positioning and spawning
- `CoinProjector` - Projects 3D coins to 2D screen coordinates
- `GameEngine` - Extended to spawn coins in 3D space
- Sensor-based fallback positioning logic

#### 2. Platform Layer (Android/iOS Specific)

**Android (`androidMain`):**
- ARCore integration via `ArCameraSession`
- Camera permission handling
- Gyroscope/accelerometer sensor manager (fallback)

**iOS (`iosMain`):**
- ARKit integration via `ArCameraSession`
- Camera permission handling (Info.plist)
- CoreMotion sensor manager (fallback)

**Common Interface:**
```kotlin
expect class CameraProvider {
    fun startSession()
    fun stopSession()
    fun getCurrentPose(): Pose
    fun isARAvailable(): Boolean
    fun isARActive(): Boolean
}
```

#### 3. UI Layer (Compose Multiplatform)

- `ARCameraView` - Platform-specific camera view wrapper
- `CoinOverlay` - Modified to render projected 3D coins
- `ARGameScreen` - Full-screen camera with UI overlay

---

## Coin Positioning & Distribution

### Virtual Sphere Strategy

Coins spawn in a spherical volume around the user's initial position.

**Distance Ranges:**
- **Close range**: 0.5m - 1.5m (easy, within arm's reach)
- **Medium range**: 1.5m - 2.5m (moderate difficulty)
- **Far range**: 2.5m - 3.5m (challenging, requires walking)

**Distribution per Game:**
```
4 Hapoalim coins (good):
  - 2 close (0.5-1.5m)
  - 1 medium (1.5-2.5m)
  - 1 far (2.5-3.5m)

3 Penalty coins (Leumi/Mizrahi/Discount):
  - 1 close
  - 2 medium/far
```

**Height Distribution:**
- 50% at eye level (-0.3m to +0.3m)
- 30% higher (+0.3m to +1.5m)
- 20% lower (-0.5m to ground level)

**Spawning Algorithm:**
```kotlin
fun spawnCoinAtDistance(
    type: CoinType,
    distanceRange: ClosedFloatingPointRange<Float>
): Coin {
    val distance = Random.nextFloat() * (distanceRange.endInclusive - distanceRange.start) + distanceRange.start
    val azimuth = Random.nextFloat() * 360f  // 0-360 degrees around user
    val elevation = when {
        Random.nextFloat() < 0.5f -> Random.nextFloat() * 0.6f - 0.3f  // Eye level
        Random.nextFloat() < 0.6f -> Random.nextFloat() * 1.2f + 0.3f  // Higher
        else -> Random.nextFloat() * -0.5f  // Lower
    }

    return Coin(
        id = generateId(),
        position3D = sphericalToCartesian(distance, azimuth, elevation),
        type = type
    )
}
```

---

## 3D to 2D Projection

### Projection Math

**Core Algorithm (CommonMain):**
```kotlin
class CoinProjector(private val fov: Float = 60f) {

    fun project3DTo2D(
        coin: Coin,
        cameraPose: Pose,
        screenSize: IntSize
    ): ProjectedCoin? {

        // Transform coin from world space to camera space
        val cameraSpace = transformToCamera(coin.position3D, cameraPose)

        // Coin behind camera? Don't render
        if (cameraSpace.z <= 0) return null

        // Calculate focal length from FOV
        val focalLength = (screenSize.width / 2f) / tan(fov.toRadians() / 2f)

        // Perspective projection
        val screenX = (cameraSpace.x / cameraSpace.z) * focalLength + screenSize.width / 2f
        val screenY = (-cameraSpace.y / cameraSpace.z) * focalLength + screenSize.height / 2f

        // Off-screen? Don't render
        if (screenX !in 0f..screenSize.width.toFloat()) return null
        if (screenY !in 0f..screenSize.height.toFloat()) return null

        // Distance-based scaling (closer = bigger)
        val distance = cameraSpace.length()
        val apparentScale = (coin.scale * BASE_COIN_SIZE) / distance

        // Distance-based alpha (slight fade for far coins)
        val distanceAlpha = (1f - (distance - 0.5f) / 3f).coerceIn(0.6f, 1f)

        return ProjectedCoin(
            coin = coin,
            screenX = screenX,
            screenY = screenY,
            apparentScale = apparentScale.coerceIn(0.3f, 2.0f),
            distance = distance
        )
    }

    private fun transformToCamera(worldPos: Vector3D, cameraPose: Pose): Vector3D {
        // Translate to camera origin
        val relative = worldPos - cameraPose.position

        // Rotate by inverse camera rotation
        return cameraPose.rotation.inverse() * relative
    }
}
```

### Rendering Pipeline

**Every Frame:**
1. Get current camera pose (from AR or sensors)
2. Project all 3D coins to 2D screen coordinates
3. Filter out coins behind camera or off-screen
4. Sort by distance (far to near) for proper Z-ordering
5. Render each visible coin with existing animations

**Updated CoinOverlay:**
- Replace normalized 2D coordinates with projected positions
- Add distance-based alpha blending
- Maintain rotation, pulse, collect animations
- Render in distance order (painter's algorithm)

---

## Platform Integration

### Android (ARCore)

**Dependencies (`build.gradle.kts`):**
```kotlin
androidMain.dependencies {
    implementation("com.google.ar:core:1.41.0")
    implementation("io.github.sceneview:arsceneview:2.0.3")
}
```

**Implementation:**
```kotlin
// androidMain/kotlin/.../ar/CameraProvider.android.kt
actual class CameraProvider(private val context: Context) {
    private var arSession: Session? = null
    private var sensorManager: SensorManager? = null
    private var isUsingAR = false

    actual fun startSession() {
        try {
            if (ArCoreApk.getInstance().checkAvailability(context) == Availability.SUPPORTED_INSTALLED) {
                arSession = Session(context)
                isUsingAR = true
            } else {
                startSensorFallback()
            }
        } catch (e: Exception) {
            startSensorFallback()
        }
    }

    actual fun getCurrentPose(): Pose {
        return if (isUsingAR) {
            arSession?.update()?.camera?.pose?.toPose() ?: Pose.IDENTITY
        } else {
            getSensorPose()
        }
    }

    private fun startSensorFallback() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // Register gyroscope + accelerometer listeners
    }

    actual fun isARAvailable(): Boolean =
        ArCoreApk.getInstance().checkAvailability(context) == Availability.SUPPORTED_INSTALLED

    actual fun isARActive(): Boolean = isUsingAR
}
```

**Permissions (`AndroidManifest.xml`):**
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera.ar" android:required="false" />
```

### iOS (ARKit)

**Implementation:**
```kotlin
// iosMain/kotlin/.../ar/CameraProvider.ios.kt
actual class CameraProvider {
    private var arSession: ARSession? = null
    private var motionManager: CMMotionManager? = null
    private var isUsingAR = false

    actual fun startSession() {
        if (ARWorldTrackingConfiguration.isSupported) {
            arSession = ARSession()
            val config = ARWorldTrackingConfiguration()
            arSession?.run(config)
            isUsingAR = true
        } else {
            startSensorFallback()
        }
    }

    actual fun getCurrentPose(): Pose {
        return if (isUsingAR) {
            arSession?.currentFrame?.camera?.transform?.toPose() ?: Pose.IDENTITY
        } else {
            getSensorPose()
        }
    }

    private fun startSensorFallback() {
        motionManager = CMMotionManager()
        motionManager?.startDeviceMotionUpdates()
    }

    actual fun isARAvailable(): Boolean = ARWorldTrackingConfiguration.isSupported
    actual fun isARActive(): Boolean = isUsingAR
}
```

**Permissions (`Info.plist`):**
```xml
<key>NSCameraUsageDescription</key>
<string>Camera is used to display bank logos in augmented reality</string>
```

### Sensor Fallback (Both Platforms)

**When AR Unavailable:**
- Use device gyroscope for rotation tracking
- Use accelerometer for tilt detection
- Simulate camera pose from sensor fusion
- Coins positioned in virtual sphere (same as AR)
- User rotates phone to look around

**Sensor Fusion Algorithm:**
```kotlin
class SensorPoseTracker {
    private var currentRotation = Quaternion.IDENTITY

    fun updateFromSensors(
        gyroData: Vector3D,
        accelData: Vector3D,
        deltaTime: Float
    ): Pose {
        // Integrate gyroscope for rotation
        val gyroQuat = Quaternion.fromEuler(gyroData * deltaTime)
        currentRotation = currentRotation * gyroQuat

        // Use accelerometer to correct drift (gravity vector)
        val gravityCorrection = calculateGravityCorrection(accelData)
        currentRotation = currentRotation.slerp(gravityCorrection, 0.02f)

        return Pose(
            position = Vector3D.ZERO,  // User stays at origin in sensor mode
            rotation = currentRotation
        )
    }
}
```

---

## UI Design

### Full-Screen AR Layout

```
┌─────────────────────────────────┐
│ ⏱ 45s        SCORE: 120      ⏸ │ ← Semi-transparent top bar
├─────────────────────────────────┤
│                                 │
│     [Live Camera Feed]          │
│                                 │
│    🏛️  (Floating bank logos)    │
│         💰                      │
│                  🏦             │
│                                 │
│  "Move phone to find coins!"    │ ← Hint (fades after 3s)
│                                 │
└─────────────────────────────────┘
```

**Components:**
- **Camera Background**: Full-screen camera view or black background (sensor mode)
- **Top Bar**: Semi-transparent (80% opacity), timer left, score center, pause right
- **Coins**: Rendered with all animations over camera
- **Hint Text**: Initial instruction, fades after 3 seconds
- **Mode Indicator**: Small badge showing "AR Mode" or "Sensor Mode"

### ARGameScreen Implementation

```kotlin
@Composable
fun ARGameScreen(
    gameState: GameState.Running,
    cameraProvider: CameraProvider,
    onCoinTapped: (String) -> Unit,
    onPause: () -> Unit
) {
    val cameraPose by cameraProvider.poseFlow.collectAsState()
    val screenSize = LocalConfiguration.current.let {
        IntSize(it.screenWidthDp.dp.toPx(), it.screenHeightDp.dp.toPx())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera view (platform-specific)
        if (cameraProvider.isARActive()) {
            ARCameraView(cameraProvider)
        } else {
            // Black background for sensor mode
            Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        }

        // Project and render coins
        val projector = remember { CoinProjector() }
        val projectedCoins = remember(gameState.coins, cameraPose) {
            gameState.coins
                .mapNotNull { projector.project3DTo2D(it, cameraPose, screenSize) }
                .sortedByDescending { it.distance }  // Far to near
        }

        CoinOverlay(
            projectedCoins = projectedCoins,
            onCoinTapped = onCoinTapped
        )

        // UI Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("⏱ ${gameState.timeRemaining}s", color = Color.White)
                Text("SCORE: ${gameState.score}", color = Color.White)
                IconButton(onClick = onPause) {
                    Icon(Icons.Default.Pause, tint = Color.White)
                }
            }
        }

        // Initial hint
        var showHint by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            delay(3000)
            showHint = false
        }

        if (showHint) {
            Text(
                text = "Move your phone to look around!",
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp),
                color = Color.White,
                fontSize = 18.sp
            )
        }

        // Mode indicator
        Text(
            text = if (cameraProvider.isARActive()) "AR Mode" else "Sensor Mode",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White,
            fontSize = 12.sp
        )
    }
}
```

---

## Permission Handling

### First Launch Flow

```
App Start
  ↓
Request Camera Permission
  ↓
  ├─ Granted → Check AR Capability
  │             ├─ Available → Initialize ARCore/ARKit
  │             └─ Unavailable → Initialize Sensor Mode
  │
  └─ Denied → Show "Camera needed for AR" Dialog
                ↓
                User Choice:
                ├─ "Use Sensor Mode" → Start with sensors
                ├─ "Open Settings" → Navigate to app settings
                └─ "Exit" → Close app
```

### Permission Request Implementation

**Android:**
```kotlin
class MainActivity : ComponentActivity() {
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            initializeAR()
        } else {
            showPermissionDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
}
```

**iOS:**
```kotlin
// Handled automatically by Info.plist
// System shows permission dialog on first camera access
```

---

## Error Handling

### Error Scenarios & Responses

| Error | Detection | Response |
|-------|-----------|----------|
| No camera permission | Permission denied | Show dialog → Fallback to sensor mode |
| ARCore/ARKit unavailable | Capability check fails | Auto-fallback to sensor mode |
| Camera hardware failure | Session initialization error | Show error → Restart or fallback |
| No sensors available | Sensor check fails | Show "Device not supported" error |
| AR session lost | Tracking state = STOPPED | Attempt to restart session |
| Low light conditions | AR tracking quality low | Show "Need better lighting" hint |

### Graceful Degradation

**Priority Cascade:**
1. **Best**: ARCore/ARKit with camera
2. **Good**: Sensor mode (gyro + accel)
3. **Fallback**: 2D mode (original game)

**Auto-fallback triggers:**
- AR initialization fails 3 times
- Tracking quality stays "Poor" for 10+ seconds
- User manually switches in settings

---

## Performance Considerations

### Target Frame Rates
- **AR Mode**: 30 FPS minimum (60 FPS target)
- **Sensor Mode**: 60 FPS (less overhead)

### Optimization Strategies
- **Coin culling**: Don't project coins behind camera
- **Off-screen culling**: Skip rendering coins outside viewport
- **Distance culling**: Don't render coins >5m away
- **Animation throttling**: Reduce animation updates when >10 coins visible
- **Lazy projection**: Only recalculate when camera pose changes significantly

### Memory Management
- Reuse projection matrices
- Pool coin instances
- Limit max concurrent coins to 10
- Release AR session when game paused

---

## Testing Strategy

### Unit Tests
- `CoinProjector` projection math accuracy
- Spherical to Cartesian coordinate conversion
- Distance calculation correctness
- Camera pose transformations

### Integration Tests
- AR session lifecycle (start/stop/restart)
- Sensor fallback activation
- Permission flow completion
- Coin respawn in 3D space

### Platform Tests
- **Android**: ARCore on various devices (Pixel, Samsung)
- **iOS**: ARKit on iPhone 12+
- **Sensor Mode**: Non-AR devices (emulators, older phones)

### Manual QA Checklist
- [ ] Coins appear at correct distances (measure with ruler)
- [ ] Camera tracking is smooth (no jitter)
- [ ] Sensor fallback works without AR
- [ ] All animations visible in AR
- [ ] Coins tappable at all distances
- [ ] UI overlay readable over camera
- [ ] Performance smooth (check FPS)
- [ ] Battery drain acceptable (<20%/hour)

---

## Future Enhancements (Out of Scope)

These features are intentionally excluded from initial implementation:

- ❌ Multiplayer AR (complex networking)
- ❌ Persistent AR anchors (cloud anchors)
- ❌ Occlusion/depth sensing (requires LiDAR)
- ❌ Hand tracking for grabbing coins
- ❌ Custom 3D coin models (using 2D logos)
- ❌ AR world mapping/saving
- ❌ Social features (sharing AR sessions)

---

## Implementation Phases

### Phase 1: Foundation (Week 1)
- Create 3D data models (Vector3D, Pose, ProjectedCoin)
- Implement CoinProjector with projection math
- Add unit tests for projection accuracy
- Update Coin data class with position3D

### Phase 2: Sensor Fallback (Week 1-2)
- Implement SensorPoseTracker
- Add gyroscope/accelerometer listeners
- Test sensor fusion algorithm
- Validate rotation tracking accuracy

### Phase 3: Platform AR (Week 2-3)
- Android: Integrate ARCore session management
- iOS: Integrate ARKit session management
- Implement expect/actual CameraProvider
- Add permission handling

### Phase 4: UI Integration (Week 3)
- Create ARGameScreen composable
- Update CoinOverlay for projected coins
- Add camera view platform wrappers
- Implement UI overlay components

### Phase 5: Polish & Testing (Week 4)
- Performance optimization
- Error handling
- Platform testing (real devices)
- Bug fixes and refinement

---

## Success Criteria

✅ **Must Have:**
- Coins visible in AR camera view
- Sensor fallback works on non-AR devices
- All existing animations preserved
- Smooth camera tracking (>30 FPS)
- Works on both Android and iOS
- Proper permission handling

✅ **Should Have:**
- 60 FPS in AR mode
- Automatic AR/sensor detection
- Visual indicator of current mode
- Graceful error recovery

✅ **Nice to Have:**
- Distance indicator for coins
- AR quality hints ("move to well-lit area")
- Settings to force sensor mode

---

## Dependencies

**New Android Dependencies:**
```kotlin
implementation("com.google.ar:core:1.41.0")
implementation("io.github.sceneview:arsceneview:2.0.3")
```

**New iOS Dependencies:**
```kotlin
// Via Kotlin/Native platform APIs
platform.ARKit.*
platform.CoreMotion.*
```

**No new common dependencies required** - all math done in commonMain

---

## Documentation Updates Needed

- [ ] Update README with AR feature description
- [ ] Add AR setup instructions (ARCore/ARKit)
- [ ] Document sensor fallback behavior
- [ ] Add troubleshooting guide for AR issues
- [ ] Update screenshots with AR gameplay

---

## Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| ARCore/ARKit version incompatibility | Medium | High | Use latest stable versions, test thoroughly |
| Poor sensor accuracy | High | Medium | Implement drift correction, offer calibration |
| Performance issues on older devices | High | Medium | Optimize rendering, reduce coin count dynamically |
| Permission denial by users | Medium | Medium | Clear explanation, sensor fallback available |
| Complex platform-specific bugs | High | High | Extensive testing on real devices, good logging |

---

**End of Design Document**

Ready for implementation planning and development.
