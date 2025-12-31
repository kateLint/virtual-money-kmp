package com.keren.virtualmoney.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keren.virtualmoney.ar.camera.CameraProvider
import com.keren.virtualmoney.game.GameEngine
import com.keren.virtualmoney.game.GameState

/**
 * Main game screen that displays different states based on the game FSM.
 * @param gameEngine The game engine managing state
 * @param cameraBackground Composable that renders the camera feed
 * @param cameraProvider CameraProvider for AR mode
 * @param isARMode Whether AR mode is enabled
 * @param onToggleARMode Callback to toggle AR mode
 */
@Composable
fun GameScreen(
    gameEngine: GameEngine,
    cameraBackground: @Composable () -> Unit,
    cameraProvider: CameraProvider,
    isARMode: Boolean,
    onToggleARMode: () -> Unit
) {
    val gameState by gameEngine.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = gameState) {
            is GameState.Ready -> ReadyScreen(
                onStartGame = { gameEngine.startGame() },
                isARMode = isARMode,
                onToggleARMode = onToggleARMode,
                isARAvailable = cameraProvider.isARAvailable()
            )
            is GameState.Running -> {
                if (isARMode) {
                    // AR Mode - use ARGameScreen
                    ARGameScreen(
                        gameState = state,
                        cameraProvider = cameraProvider,
                        onCoinTapped = { coinId -> gameEngine.collectCoin(coinId) },
                        onPause = { gameEngine.resetGame() }
                    )
                } else {
                    // 2D Mode - use original RunningScreen
                    RunningScreen(
                        state = state,
                        cameraBackground = cameraBackground,
                        onCoinTapped = { coinId -> gameEngine.collectCoin(coinId) }
                    )
                }
            }
            is GameState.Finished -> FinishedScreen(
                state = state,
                onTryAgain = { gameEngine.resetGame() }
            )
        }
    }
}

/**
 * Ready state - shows start button and high score.
 */
@Composable
private fun ReadyScreen(
    onStartGame: () -> Unit,
    isARMode: Boolean,
    onToggleARMode: () -> Unit,
    isARAvailable: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Coin Hunter",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700) // Gold
        )

        Spacer(modifier = Modifier.height(48.dp))

        // AR/2D Mode Toggle - only show 2D button if AR is NOT available
        if (isARAvailable) {
            // AR is available - only show AR mode (no toggle needed)
            Text(
                text = "AR Mode Ready",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4CAF50) // Green
            )
        } else {
            // AR not available - show mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onToggleARMode() },
                    modifier = Modifier.width(120.dp),
                    enabled = !isARMode
                ) {
                    Text("2D Mode")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { onToggleARMode() },
                    modifier = Modifier.width(120.dp),
                    enabled = isARMode
                ) {
                    Text("Sensor Mode")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStartGame,
            modifier = Modifier
                .size(width = 200.dp, height = 60.dp)
        ) {
            Text("Start Game", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isARAvailable) {
                "Move your phone to look around!\nTap coins to collect them.\n60 seconds to get the highest score."
            } else if (isARMode) {
                "Move your phone with sensors!\nTap coins to collect them.\n60 seconds to get the highest score."
            } else {
                "Tap coins to collect them!\n60 seconds to get the highest score."
            },
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * Running state - shows camera with coin overlay.
 */
@Composable
private fun RunningScreen(
    state: GameState.Running,
    cameraBackground: @Composable () -> Unit,
    onCoinTapped: (String) -> Unit
) {
    var screenSize = remember { IntSize(0, 0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { screenSize = it }
    ) {
        // Camera background
        cameraBackground()

        // HUD Overlay
        GameHUD(
            timeRemaining = state.timeRemaining,
            score = state.score
        )

        // Coins overlay
        CoinOverlay(
            coins = state.coins,
            screenSize = screenSize,
            onCoinTapped = onCoinTapped
        )
    }
}

/**
 * Finished state - shows final score and try again button.
 */
@Composable
private fun FinishedScreen(
    state: GameState.Finished,
    onTryAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isNewHighScore) {
            Text(
                text = "🎉 NEW HIGH SCORE! 🎉",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Final Score",
            fontSize = 24.sp,
            color = Color.White
        )

        Text(
            text = "${state.finalScore}",
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onTryAgain,
            modifier = Modifier.size(width = 200.dp, height = 60.dp)
        ) {
            Text("Try Again", fontSize = 20.sp)
        }
    }
}

/**
 * Heads-up display showing timer and score.
 */
@Composable
private fun GameHUD(
    timeRemaining: Int,
    score: Int
) {
    val timerColor = if (timeRemaining <= 10) Color.Red else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Timer
            Text(
                text = "⏱ $timeRemaining",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = timerColor,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Score
            Text(
                text = "💰 $score",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
