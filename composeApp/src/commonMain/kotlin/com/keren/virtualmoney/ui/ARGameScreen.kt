package com.keren.virtualmoney.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keren.virtualmoney.ar.camera.CameraProvider
import com.keren.virtualmoney.ar.data.ProjectedCoin
import com.keren.virtualmoney.ar.projection.CoinProjector
import com.keren.virtualmoney.game.GameState
import com.keren.virtualmoney.theme.CoinSkin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * AR Game Screen - Full-screen camera view with AR coin overlay
 *
 * @param gameState Current game state (Running)
 * @param skin Current selected coin skin
 * @param cameraProvider Provides real-time camera pose
 * @param onCoinTapped Callback when user taps a coin
 * @param onPause Callback when user pauses the game
 */
@Composable
fun ARGameScreen(
        gameState: GameState,
        skin: CoinSkin = CoinSkin.CLASSIC,
        cameraProvider: CameraProvider,
        onCoinTapped: (String) -> Unit,
        onPause: () -> Unit
) {
        if (gameState !is GameState.Running) return

        // Collect camera pose
        val cameraPose by cameraProvider.poseFlow.collectAsState()

        // 60 FPS update loop - trigger recomposition and update pose
        var frameCount by remember { mutableStateOf(0) }
        LaunchedEffect(Unit) {
                while (isActive) {
                        cameraProvider.updatePose() // Update pose from AR/sensors
                        delay(16) // ~60 FPS
                        frameCount++
                }
        }

        // Hint visibility (fade after 3 seconds)
        var showHint by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
                delay(3000)
                showHint = false
        }

        // Get screen size
        val density = LocalDensity.current
        var screenSize by remember { mutableStateOf(IntSize(1080, 1920)) }

        // Project 3D coins to 2D
        val projector = remember { CoinProjector() }
        val projectedCoins: List<ProjectedCoin> =
                gameState.coins.mapNotNull { coin ->
                        projector.project3DTo2D(coin, cameraPose, screenSize)
                }

        Box(modifier = Modifier.fillMaxSize()) {
                // Real camera preview background
                CameraPreview(modifier = Modifier.fillMaxSize())

                // Measure screen size
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        screenSize =
                                IntSize(
                                        width = with(density) { maxWidth.toPx().toInt() },
                                        height = with(density) { maxHeight.toPx().toInt() }
                                )
                }

                // AR Coin Overlay (projected coins)
                ARCoinOverlay(
                        projectedCoins = projectedCoins,
                        skin = skin,
                        screenSize = screenSize,
                        onCoinTapped = onCoinTapped
                )

                // Top overlay - Timer, Score, Pause
                Row(
                        modifier =
                                Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Timer
                        Text(
                                text = "Time: ${gameState.timeRemaining}",
                                fontSize = 20.sp,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                        )

                        // Score
                        Text(
                                text = "Score: ${gameState.score}",
                                fontSize = 20.sp,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                        )

                        // Pause button
                        Button(onClick = onPause) { Text("Pause") }
                }

                // AR/Sensor mode indicator (bottom left)
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                        Text(
                                text =
                                        if (cameraProvider.isARActive()) "AR Mode: Active"
                                        else "Sensor Mode: Active",
                                fontSize = 14.sp,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                        )
                        // DEBUG: Show Pose Data
                        Text(
                                text =
                                        "Pos: ${cameraPose.position.x.toString().take(5)}, ${cameraPose.position.y.toString().take(5)}, ${cameraPose.position.z.toString().take(5)}",
                                fontSize = 12.sp,
                                color = Color.Yellow
                        )
                        Text(
                                text =
                                        "Rot: ${cameraPose.rotation.x.toString().take(5)}, ${cameraPose.rotation.y.toString().take(5)}, ${cameraPose.rotation.z.toString().take(5)}, ${cameraPose.rotation.w.toString().take(5)}",
                                fontSize = 12.sp,
                                color = Color.Yellow
                        )
                }

                // Initial hint (fades after 3 seconds)
                AnimatedVisibility(
                        visible = showHint,
                        exit = fadeOut(),
                        modifier = Modifier.align(Alignment.Center)
                ) {
                        Text(
                                text = "Move your phone to look around!",
                                fontSize = 18.sp,
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                modifier =
                                        Modifier.background(Color.Black.copy(alpha = 0.6f))
                                                .padding(16.dp)
                        )
                }
        }
}
