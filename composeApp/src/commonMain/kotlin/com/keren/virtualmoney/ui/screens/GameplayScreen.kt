package com.keren.virtualmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keren.virtualmoney.ar.camera.CameraProvider
import com.keren.virtualmoney.backend.ServiceLocator
import com.keren.virtualmoney.game.*
import com.keren.virtualmoney.theme.CoinSkin
import com.keren.virtualmoney.ui.ARGameScreen
import com.keren.virtualmoney.ui.CoinOverlay
import com.keren.virtualmoney.ui.components.ComboDisplay
import com.keren.virtualmoney.ui.components.PowerUpHUD

/** Main gameplay screen that uses the EnhancedGameEngine. */
@Composable
fun GameplayScreen(
        mode: GameMode,
        cameraProvider: CameraProvider,
        cameraBackground: @Composable () -> Unit,
        onGameOver: (GameState.Finished) -> Unit,
        onExit: () -> Unit,
        modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Create game config based on mode
    val config =
            remember(mode) {
                when (mode) {
                    GameMode.CLASSIC -> GameConfig.classic()
                    GameMode.BLITZ -> GameConfig.blitz()
                    GameMode.SURVIVAL -> GameConfig.survival()
                    GameMode.QUICK_MATCH -> GameConfig.quickMatch()
                    GameMode.BATTLE_ROYALE -> GameConfig.battleRoyale()
                    GameMode.TEAM_BATTLE -> GameConfig.teamBattle()
                    GameMode.KING_OF_HILL -> GameConfig.kingOfHill()
                }
            }

    // Create game engine
    val gameEngine =
            remember(mode) {
                EnhancedGameEngine(
                        coroutineScope = coroutineScope,
                        config = config,
                        cameraProvider = cameraProvider,
                        onPlaySound = { sound -> ServiceLocator.soundManager.play(sound) },
                        onHaptic = { type -> ServiceLocator.hapticManager.vibrate(type) },
                        getHighScore = { ServiceLocator.progressionManager.stats.value.highScore },
                        saveHighScore = { score ->
                            // High score is saved via progression system
                        }
                )
            }

    val gameState by gameEngine.state.collectAsState()
    val comboState by gameEngine.comboState.collectAsState()

    // Theme
    val currentSkin by ServiceLocator.themeManager.currentSkin.collectAsState()

    // Check for AR availability
    val isARAvailable = remember { cameraProvider.isARAvailable() }

    // Start AR session if available
    LaunchedEffect(isARAvailable) {
        if (isARAvailable) {
            cameraProvider.startSession()
        }
    }

    // Handle game over
    LaunchedEffect(gameState) {
        if (gameState is GameState.Finished) {
            val result = gameState as GameState.Finished
            // Process game result for progression
            val gameResult =
                    com.keren.virtualmoney.progression.GameResult(
                            score = result.finalScore,
                            coinsCollected = result.coinsCollected,
                            bestCombo = result.bestCombo,
                            powerUpsCollected = result.powerUpsCollected,
                            wasPerfectRun = result.wasPerfectRun,
                            playTimeMs = result.playTimeMs,
                            gameMode = result.gameMode.name
                    )
            ServiceLocator.progressionManager.processGameResult(gameResult)
            onGameOver(result)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = gameState) {
            is GameState.Ready -> {
                // Show countdown and start game
                CountdownScreen(mode = mode, onStart = { gameEngine.startGame() }, onExit = onExit)
            }
            is GameState.Running -> {
                if (isARAvailable) {
                    // AR Mode gameplay
                    ARGameplayContent(
                            state = state,
                            comboState = comboState,
                            skin = currentSkin,
                            cameraProvider = cameraProvider,
                            onCoinTapped = { coinId -> gameEngine.collectCoin(coinId) },
                            onPowerUpTapped = { powerUpId -> gameEngine.collectPowerUp(powerUpId) },
                            onExit = {
                                gameEngine.resetGame()
                                onExit()
                            }
                    )
                } else {
                    // 2D Mode gameplay
                    GameplayContent2D(
                            state = state,
                            comboState = comboState,
                            skin = currentSkin,
                            cameraBackground = cameraBackground,
                            onCoinTapped = { coinId -> gameEngine.collectCoin(coinId) },
                            onPowerUpTapped = { powerUpId -> gameEngine.collectPowerUp(powerUpId) },
                            onExit = {
                                gameEngine.resetGame()
                                onExit()
                            }
                    )
                }
            }
            is GameState.Finished -> {
                // Handled by LaunchedEffect above
            }
        }
    }
}

@Composable
private fun CountdownScreen(mode: GameMode, onStart: () -> Unit, onExit: () -> Unit) {
    var countdown by remember { mutableStateOf(3) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            kotlinx.coroutines.delay(1000)
            countdown--
        }
        onStart()
    }

    Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
    ) {
        // Exit button
        Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Exit",
                tint = Color.White,
                modifier =
                        Modifier.align(Alignment.TopEnd).padding(16.dp).size(32.dp).clickable {
                            onExit()
                        }
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                    text = mode.displayName.uppercase(),
                    color = Color(0xFFFFD700),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                    text = if (countdown > 0) countdown.toString() else "GO!",
                    color = Color.White,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = mode.description, color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
        }
    }
}

@Composable
private fun ARGameplayContent(
        state: GameState.Running,
        comboState: ComboState,
        skin: CoinSkin,
        cameraProvider: CameraProvider,
        onCoinTapped: (String) -> Unit,
        onPowerUpTapped: (String) -> Unit,
        onExit: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // AR Game Screen
        ARGameScreen(
                gameState = state,
                skin = skin,
                cameraProvider = cameraProvider,
                onCoinTapped = onCoinTapped,
                onPause = onExit
        )

        // Enhanced HUD overlay
        EnhancedGameHUD(state = state, comboState = comboState, onExit = onExit)
    }
}

@Composable
private fun GameplayContent2D(
        state: GameState.Running,
        comboState: ComboState,
        skin: CoinSkin,
        cameraBackground: @Composable () -> Unit,
        onCoinTapped: (String) -> Unit,
        onPowerUpTapped: (String) -> Unit,
        onExit: () -> Unit
) {
    var screenSize by remember { mutableStateOf(IntSize(0, 0)) }

    Box(modifier = Modifier.fillMaxSize().onSizeChanged { screenSize = it }) {
        // Camera background
        cameraBackground()

        // Coins overlay
        CoinOverlay(
                coins = state.coins,
                skin = skin,
                screenSize = screenSize,
                onCoinTapped = onCoinTapped
        )

        // Enhanced HUD
        EnhancedGameHUD(state = state, comboState = comboState, onExit = onExit)
    }
}

@Composable
private fun EnhancedGameHUD(state: GameState.Running, comboState: ComboState, onExit: () -> Unit) {
    val timerColor = if (state.timeRemaining <= 10) Color.Red else Color.White

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Top bar
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Exit button
            Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit",
                    tint = Color.White,
                    modifier =
                            Modifier.size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable { onExit() }
                                    .padding(4.dp)
            )

            // Timer
            Text(
                    text = formatTime(state.timeRemaining),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = timerColor,
                    modifier =
                            Modifier.clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Score
            Text(
                    text = "${state.score}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    modifier =
                            Modifier.clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Lives (for survival mode)
        if (state.gameMode == GameMode.SURVIVAL) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                    modifier =
                            Modifier.clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                repeat(state.lives) {
                    Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom HUD - Combo and Power-ups
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
        ) {
            // Combo display
            ComboDisplay(comboState = comboState, modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.width(16.dp))

            // Active power-ups
            PowerUpHUD(activePowerUps = state.activePowerUps, modifier = Modifier.weight(1f))
        }
    }
}

private fun formatTime(seconds: Int): String {
    return if (seconds == Int.MAX_VALUE || seconds > 3600) {
        // Survival mode - show elapsed time would be better, but for now show infinity
        "∞"
    } else {
        val mins = seconds / 60
        val secs = seconds % 60
        if (mins > 0) {
            "${mins}:${secs.toString().padStart(2, '0')}"
        } else {
            "$secs"
        }
    }
}
