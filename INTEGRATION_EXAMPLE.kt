// INTEGRATION EXAMPLE: How to Add Particle Effects & Coin Syncing to GameplayScreen

package com.keren.virtualmoney.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.keren.virtualmoney.ar.camera.CameraProvider
import com.keren.virtualmoney.backend.GameRepository
import com.keren.virtualmoney.game.GameMode
import com.keren.virtualmoney.game.GameState
import com.keren.virtualmoney.game.MultiplayerGameEngine
import com.keren.virtualmoney.multiplayer.MultiplayerStateManager
import com.keren.virtualmoney.ui.particles.ParticleEffectOverlay
import com.keren.virtualmoney.ui.particles.ParticleSystemManager

/**
 * BEFORE: GameplayScreen without particles or coin syncing
 */
@Composable
fun GameplayScreen_OLD(
    mode: GameMode,
    cameraProvider: CameraProvider,
    cameraBackground: @Composable () -> Unit,
    onGameOver: (GameState.Finished) -> Unit,
    onExit: () -> Unit
) {
    // Old implementation - no particles, no coin syncing
}

/**
 * AFTER: GameplayScreen WITH particles and coin syncing
 */
@Composable
fun GameplayScreen_NEW(
    mode: GameMode,
    gameId: String,  // NEW: Required for multiplayer coin syncing
    gameRepository: GameRepository,  // NEW: Required for coin syncing
    multiplayerManager: MultiplayerStateManager?,  // NEW: For multiplayer modes
    cameraProvider: CameraProvider,
    cameraBackground: @Composable () -> Unit,
    onGameOver: (GameState.Finished) -> Unit,
    onExit: () -> Unit
) {
    // ========================================
    // 1. CREATE PARTICLE MANAGER
    // ========================================
    val particleManager = remember { ParticleSystemManager() }

    // ========================================
    // 2. CREATE GAME ENGINE WITH NEW FEATURES
    // ========================================
    val gameEngine = remember(mode, gameId) {
        if (mode.isMultiplayer && multiplayerManager != null) {
            // Multiplayer with coin syncing + particles
            MultiplayerGameEngine(
                coroutineScope = rememberCoroutineScope(),
                multiplayerManager = multiplayerManager,
                gameMode = mode,
                gameRepository = gameRepository,  // NEW: For coin syncing
                gameId = gameId,                   // NEW: For coin syncing
                particleManager = particleManager, // NEW: For particle effects
                onPlaySound = { sound -> /* play sound */ },
                onHaptic = { type -> /* trigger haptic */ }
            )
        } else {
            // Single player (existing code - can add particles here too)
            // ... your existing single player engine
        }
    }

    // ========================================
    // 3. RENDER GAME WITH PARTICLE OVERLAY
    // ========================================
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera background
        cameraBackground()

        // Game UI (coins, power-ups, HUD)
        GameContent(
            gameEngine = gameEngine,
            onCoinTapped = { coin, tapPosition ->
                // NEW: Pass tap position for particle effect
                gameEngine.collectCoin(
                    coinId = coin.id,
                    screenPosition = tapPosition  // NEW: For particles
                )
            },
            onPowerUpTapped = { powerUp, tapPosition ->
                // NEW: Pass tap position for particle effect
                gameEngine.collectPowerUp(
                    powerUpId = powerUp.id,
                    screenPosition = tapPosition  // NEW: For particles
                )
            }
        )

        // ========================================
        // 4. PARTICLE EFFECTS OVERLAY (ON TOP)
        // ========================================
        ParticleEffectOverlay(
            particleManager = particleManager,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * COMPLETE EXAMPLE: MultiplayerGameScreen with all features
 */
@Composable
fun MultiplayerGameScreen(
    gameMode: GameMode,
    gameId: String,
    gameRepository: GameRepository,
    multiplayerManager: MultiplayerStateManager,
    onGameOver: () -> Unit,
    onExit: () -> Unit
) {
    // ========================================
    // SETUP
    // ========================================
    val particleManager = remember { ParticleSystemManager() }
    val coroutineScope = rememberCoroutineScope()

    // Create multiplayer game engine
    val gameEngine = remember {
        MultiplayerGameEngine(
            coroutineScope = coroutineScope,
            multiplayerManager = multiplayerManager,
            gameMode = gameMode,
            gameRepository = gameRepository,
            gameId = gameId,
            particleManager = particleManager,
            onPlaySound = { sound ->
                // Play sound effect
            },
            onHaptic = { type ->
                // Trigger haptic feedback
            }
        )
    }

    // Observe game state
    val localState by gameEngine.localState.collectAsState()

    // Start game
    LaunchedEffect(Unit) {
        gameEngine.startGame()
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            gameEngine.cleanup()
        }
    }

    // ========================================
    // RENDER
    // ========================================
    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = localState) {
            is com.keren.virtualmoney.game.MultiplayerLocalState.Playing -> {
                // Background
                GameBackground()

                // Coins (synchronized from server!)
                state.coins.forEach { coin ->
                    CoinButton(
                        coin = coin,
                        onClick = { tapPosition ->
                            // Collect with particle effect
                            gameEngine.collectCoin(
                                coinId = coin.id,
                                screenPosition = tapPosition
                            )
                        }
                    )
                }

                // Power-ups
                state.powerUps.forEach { powerUp ->
                    PowerUpButton(
                        powerUp = powerUp,
                        onClick = { tapPosition ->
                            // Collect with particle effect
                            gameEngine.collectPowerUp(
                                powerUpId = powerUp.id,
                                screenPosition = tapPosition
                            )
                        }
                    )
                }

                // HUD
                GameHUD(
                    score = state.score,
                    comboCount = state.comboCount,
                    activePowerUps = state.activePowerUps
                )

                // Particle effects overlay
                ParticleEffectOverlay(
                    particleManager = particleManager,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is com.keren.virtualmoney.game.MultiplayerLocalState.Finished -> {
                // Game over
                onGameOver()
            }

            else -> {
                // Loading or waiting
                LoadingScreen()
            }
        }
    }
}

/**
 * HELPER: CoinButton that reports tap position
 */
@Composable
fun CoinButton(
    coin: com.keren.virtualmoney.game.Coin,
    onClick: (Offset) -> Unit
) {
    // ... render coin ...
    // On click, report position:
    Box(
        modifier = Modifier
            .clickable {
                // Calculate screen position
                val position = Offset(x = /* coin x */, y = /* coin y */)
                onClick(position)
            }
    ) {
        // Coin image
    }
}

/**
 * MIGRATION CHECKLIST
 * 
 * To integrate these features into existing GameplayScreen:
 * 
 * ✅ 1. Add new parameters to GameplayScreen:
 *       - gameId: String
 *       - gameRepository: GameRepository
 *       - multiplayerManager: MultiplayerStateManager?
 * 
 * ✅ 2. Create ParticleSystemManager:
 *       val particleManager = remember { ParticleSystemManager() }
 * 
 * ✅ 3. Update MultiplayerGameEngine initialization:
 *       - Add gameRepository parameter
 *       - Add gameId parameter
 *       - Add particleManager parameter
 * 
 * ✅ 4. Update collectCoin() calls:
 *       - Add screenPosition parameter
 *       collectCoin(coinId, screenPosition)
 * 
 * ✅ 5. Update collectPowerUp() calls:
 *       - Add screenPosition parameter
 *       collectPowerUp(powerUpId, screenPosition)
 * 
 * ✅ 6. Add ParticleEffectOverlay to UI:
 *       ParticleEffectOverlay(
 *           particleManager = particleManager,
 *           modifier = Modifier.fillMaxSize()
 *       )
 * 
 * ✅ 7. Update App.kt navigation to pass new parameters
 * 
 * DONE! 🎉
 */

/**
 * BONUS: Trigger particles manually for other events
 */
@Composable
fun GameHUD(
    score: Int,
    comboCount: Int,
    activePowerUps: List<com.keren.virtualmoney.game.ActivePowerUp>,
    particleManager: ParticleSystemManager? = null
) {
    // Watch for combo milestones
    LaunchedEffect(comboCount) {
        if (comboCount > 0 && comboCount % 5 == 0) {
            // Spawn combo milestone particle
            particleManager?.spawnComboMilestone(
                position = Offset(x = screenWidth / 2, y = 100f),
                comboCount = comboCount
            )
        }
    }

    // ... rest of HUD ...
}

/**
 * TESTING EXAMPLE
 */
@Composable
fun ParticleTestScreen() {
    val particleManager = remember { ParticleSystemManager() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Buttons to test different effects
        Column {
            Button(onClick = {
                particleManager.spawnCoinCollect(Offset(200f, 300f))
            }) {
                Text("Test Coin Collect")
            }

            Button(onClick = {
                particleManager.spawnPenaltyHit(Offset(200f, 300f))
            }) {
                Text("Test Penalty Hit")
            }

            Button(onClick = {
                particleManager.spawnPowerUpCollect(Offset(200f, 300f))
            }) {
                Text("Test Power Up")
            }

            Button(onClick = {
                particleManager.spawnComboMilestone(Offset(200f, 300f), 10)
            }) {
                Text("Test Combo (10x)")
            }

            Button(onClick = {
                particleManager.spawnLevelUp(Offset(200f, 300f))
            }) {
                Text("Test Level Up")
            }

            Button(onClick = {
                particleManager.spawnAchievement(Offset(200f, 300f))
            }) {
                Text("Test Achievement")
            }
        }

        // Particle overlay
        ParticleEffectOverlay(
            particleManager = particleManager,
            modifier = Modifier.fillMaxSize()
        )
    }
}
