package com.keren.virtualmoney.game

/** Enhanced game engine with power-ups, combos, and multiple game modes. */
import com.keren.virtualmoney.ar.camera.CameraProvider
import com.keren.virtualmoney.ar.core.Transform
import com.keren.virtualmoney.ar.logic.CoinManager
import com.keren.virtualmoney.audio.GameSound
import com.keren.virtualmoney.audio.HapticType
import com.keren.virtualmoney.platform.getCurrentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Enhanced game engine with power-ups, combos, and multiple game modes. */
class EnhancedGameEngine(
        private val coroutineScope: CoroutineScope,
        private val config: GameConfig = GameConfig.classic(),
        private val cameraProvider: CameraProvider, // Added CameraProvider dependency
        private val onPlaySound: (GameSound) -> Unit = {},
        private val onHaptic: (HapticType) -> Unit = {},
        private val getHighScore: () -> Int,
        private val saveHighScore: (Int) -> Unit
) {
    private val _state = MutableStateFlow<GameState>(GameState.Ready)
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val comboTracker = ComboTracker()
    val comboState: StateFlow<ComboState> = comboTracker.state

    // NEW: Coin Manager
    // NEW: Coin Manager
    private val coinManager = CoinManager(config)

    // Game session tracking
    private var gameStartTime: Long = 0
    private var powerUpsCollectedThisGame: Int = 0
    private var bestComboThisGame: Int = 0

    // Jobs
    private var gameLoopJob: Job? = null
    private var coinMaintenanceJob: Job? = null
    private var coinCleanupJob: Job? = null
    private var powerUpSpawnJob: Job? = null
    private var powerUpCleanupJob: Job? = null
    private var comboTickJob: Job? = null

    companion object {
        private const val TICK_INTERVAL_MS = 1000L
        private const val COIN_CLEANUP_INTERVAL_MS = 100L
        private const val COIN_MAINTENANCE_INTERVAL_MS = 100L // 60fps update target ideally
        private const val POWERUP_CLEANUP_INTERVAL_MS = 500L
        private const val COMBO_TICK_INTERVAL_MS = 100L
        private const val DIFFICULTY_INCREASE_INTERVAL = 15
        private const val SCALE_REDUCTION_PER_INTERVAL = 0.1f
        private const val MIN_COIN_SCALE = 0.5f
    }

    // AR Gameplay tracking
    private var totalCoinsSpawned: Int = 0
    private var powerUpsSpawned: Int = 0
    private var lastCoinCollectTime: Long = 0
    private var isTrackingStable: Boolean = false

    /** Start a new game with the configured mode. */
    fun startGame() {
        if (_state.value !is GameState.Ready) return

        gameStartTime = getCurrentTimeMillis()
        powerUpsCollectedThisGame = 0
        bestComboThisGame = 0
        comboTracker.reset()

        // Reset AR gameplay tracking
        totalCoinsSpawned = 0
        powerUpsSpawned = 0
        lastCoinCollectTime = gameStartTime
        isTrackingStable = false

        // Start with empty coins - will spawn after tracking stabilizes
        _state.value =
                GameState.Running(
                        timeRemaining = config.duration,
                        score = 0,
                        coins = emptyList(),
                        powerUps = emptyList(),
                        activePowerUps = emptyList(),
                        comboCount = 0,
                        lives = config.startingLives,
                        coinsCollected = 0,
                        penaltiesHit = 0,
                        gameMode = config.mode
                )

        onPlaySound(GameSound.GAME_START)
        onHaptic(HapticType.MEDIUM)

        startGameLoop()
        startCoinCleanup()
        startCoinMaintenance() // This now updates CoinManager
        startComboTick()
        startAntiStallCheck()

        if (config.powerUpsEnabled) {
            startPowerUpSpawn()
            startPowerUpCleanup()
        }

        // Start tracking stability check and initial spawn
        startTrackingStabilityAndSpawn()
    }

    /** Collect a coin. */
    fun collectCoin(coinId: String) {
        val currentState = _state.value as? GameState.Running ?: return
        val coin = currentState.coins.find { it.id == coinId } ?: return

        // Notify CoinManager
        val collected = coinManager.onCoinCollected(coinId)
        if (!collected) return // Already collected or invalid

        val isPenalty = Coin.isPenaltyCoin(coin.type)

        // Track last collect time for anti-stall
        lastCoinCollectTime = getCurrentTimeMillis()

        // Check shield for penalties
        if (isPenalty && currentState.hasShield()) {
            // Shield blocks penalty - just remove coin, no point loss
            onPlaySound(GameSound.SHIELD_BLOCK)
            onHaptic(HapticType.LIGHT)

            // CoinManager already removed it from its internal list
            updateCoinsFromManager(currentState)
            return
        }

        // Calculate points
        var basePoints = Coin.getValue(coin.type)

        // Apply combo multiplier for good coins
        val comboMultiplier =
                if (!isPenalty) {
                    val mult = comboTracker.onCoinCollected()
                    bestComboThisGame = maxOf(bestComboThisGame, comboTracker.state.value.count)
                    mult
                } else {
                    comboTracker.onPenaltyHit()
                    1.0f
                }

        // Apply power-up multiplier
        val powerUpMultiplier = currentState.getScoreMultiplier()

        val finalPoints = (basePoints * comboMultiplier * powerUpMultiplier).toInt()
        val newScore = (currentState.score + finalPoints).coerceAtLeast(0)

        // Update coins collected / penalties
        val newCoinsCollected =
                if (!isPenalty) currentState.coinsCollected + 1 else currentState.coinsCollected
        val newPenaltiesHit =
                if (isPenalty) currentState.penaltiesHit + 1 else currentState.penaltiesHit

        // Update lives for survival mode
        var newLives = currentState.lives
        var penaltyScoreReduction = 0
        var penaltyCoinsReduction = 0

        if (isPenalty && config.mode == GameMode.SURVIVAL) {
            newLives--
            // Lose 1/3 of coins/score on hit
            penaltyCoinsReduction = currentState.coinsCollected / 3
            penaltyScoreReduction = currentState.score / 3
        }

        // Apply reductions
        val adjustedScore = (newScore - penaltyScoreReduction).coerceAtLeast(0)
        val adjustedCoinsCollected = (newCoinsCollected - penaltyCoinsReduction).coerceAtLeast(0)

        // Play appropriate sound/haptic
        if (isPenalty) {
            onPlaySound(GameSound.PENALTY_HIT)
            onHaptic(HapticType.ERROR)
        } else {
            onPlaySound(GameSound.COIN_COLLECT)
            onHaptic(HapticType.MEDIUM)

            // Combo milestone sound
            val combo = comboTracker.state.value
            if (combo.isMilestone()) {
                onPlaySound(GameSound.COMBO_MILESTONE)
                onHaptic(HapticType.SUCCESS)
            }
        }

        // Update state
        _state.value =
                currentState.copy(
                        score = adjustedScore,
                        coins =
                                currentState.coins.filterNot {
                                    it.id == coinId
                                }, // OR updateCoinsFromManager(currentState)
                        comboCount = comboTracker.state.value.count,
                        lives = newLives,
                        coinsCollected = adjustedCoinsCollected,
                        penaltiesHit = newPenaltiesHit
                )

        // Ensure state is synced with manager
        updateCoinsFromManager(_state.value as GameState.Running)

        // Check for game over in survival mode
        if (config.mode == GameMode.SURVIVAL && newLives <= 0) {
            endGame(newScore)
        }
    }

    /** Collect a power-up. */
    fun collectPowerUp(powerUpId: String) {
        val currentState = _state.value as? GameState.Running ?: return
        val powerUp = currentState.powerUps.find { it.id == powerUpId } ?: return

        powerUpsCollectedThisGame++

        // Create active power-up effect
        val activePowerUp = ActivePowerUp(type = powerUp.type)

        // Remove from world, add to active
        val newPowerUps = currentState.powerUps.filterNot { it.id == powerUpId }
        val newActivePowerUps =
                currentState.activePowerUps.filterNot { it.type == powerUp.type } +
                        activePowerUp // Replace existing of same type

        onPlaySound(GameSound.POWERUP_COLLECT)
        onHaptic(HapticType.SUCCESS)

        // Start power-up specific sounds
        when (powerUp.type) {
            PowerUpType.MAGNET -> onPlaySound(GameSound.MAGNET_ACTIVE)
            PowerUpType.SHIELD -> onPlaySound(GameSound.SHIELD_ACTIVE)
            PowerUpType.FREEZE -> onPlaySound(GameSound.FREEZE_CAST)
            PowerUpType.INVISIBILITY -> onPlaySound(GameSound.INVISIBILITY_ON)
            else -> {}
        }

        _state.value = currentState.copy(powerUps = newPowerUps, activePowerUps = newActivePowerUps)
    }

    /** Reset game to ready state. */
    fun resetGame() {
        cancelAllJobs()
        comboTracker.reset()
        _state.value = GameState.Ready
    }

    // Helper to sync CoinManager state to GameState
    private fun updateCoinsFromManager(currentState: GameState.Running) {
        val arCoins = coinManager.getActiveCoins()
        // Map ARCoins to legacy Coins
        val mappedCoins =
                arCoins.map { arCoin ->
                    // Preserve existing Scale/Type if ID matches, else create new
                    val existing = currentState.coins.find { it.id == arCoin.id }
                    if (existing != null) {
                        existing.copy(position3D = arCoin.worldPosition)
                    } else {
                        Coin(
                                id = arCoin.id,
                                x = 0.5f,
                                y = 0.5f,
                                scale = 1.0f, // Use default or manager provided
                                type =
                                        if (arCoin.type == 0) CoinType.BANK_HAPOALIM
                                        else CoinType.BANK_LEUMI, // Simple mapping
                                spawnTime = arCoin.spawnTime,
                                position3D = arCoin.worldPosition
                        )
                    }
                }
        _state.value = currentState.copy(coins = mappedCoins)
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob =
                coroutineScope.launch {
                    while (true) {
                        delay(TICK_INTERVAL_MS)
                        val currentState = _state.value as? GameState.Running ?: break

                        // For survival mode, don't decrease time
                        if (config.mode == GameMode.SURVIVAL) {
                            updateDifficulty(currentState)
                            continue
                        }

                        if (currentState.timeRemaining <= 1) {
                            endGame(currentState.score)
                            break
                        } else {
                            val newTimeRemaining = currentState.timeRemaining - 1
                            updateDifficulty(currentState.copy(timeRemaining = newTimeRemaining))

                            // Warning sound at 10 seconds
                            if (newTimeRemaining == 10) {
                                onPlaySound(GameSound.COUNTDOWN_TICK)
                            }
                        }
                    }
                }
    }

    private fun updateDifficulty(currentState: GameState.Running) {
        val elapsedTime =
                if (config.mode == GameMode.SURVIVAL) {
                    ((getCurrentTimeMillis() - gameStartTime) / 1000).toInt()
                } else {
                    config.duration - currentState.timeRemaining
                }

        val newScale = calculateScale(elapsedTime)
        val shouldUpdateScale = elapsedTime % DIFFICULTY_INCREASE_INTERVAL == 0

        val updatedCoins =
                if (shouldUpdateScale && config.difficultyScaling && newScale < 1.0f) {
                    currentState.coins.map { it.copy(scale = newScale) }
                } else {
                    currentState.coins
                }

        // Clean up expired active power-ups
        val validActivePowerUps = currentState.activePowerUps.filterNot { it.isExpired() }

        _state.value =
                currentState.copy(
                        timeRemaining =
                                if (config.mode == GameMode.SURVIVAL) currentState.timeRemaining
                                else currentState.timeRemaining,
                        coins = updatedCoins,
                        activePowerUps = validActivePowerUps
                )
    }

    private fun startCoinMaintenance() {
        coinMaintenanceJob?.cancel()
        coinMaintenanceJob =
                coroutineScope.launch {
                    while (true) {
                        delay(COIN_MAINTENANCE_INTERVAL_MS)
                        val currentState = _state.value as? GameState.Running ?: break

                        // Update CoinManager based on current camera pose
                        val pose = cameraProvider.poseFlow.value
                        val transform = Transform(pose.position, pose.rotation)
                        val context = cameraProvider.getARContext()

                        coinManager.update(transform, context)

                        updateCoinsFromManager(currentState)
                    }
                }
    }

    private fun startCoinCleanup() {
        // Handled by CoinManager now
    }

    private fun startPowerUpSpawn() {
        powerUpSpawnJob?.cancel()
        powerUpSpawnJob =
                coroutineScope.launch {
                    delay(5000) // Initial delay before first power-up
                    while (true) {
                        delay(config.powerUpSpawnIntervalMs)
                        val currentState = _state.value as? GameState.Running ?: break

                        // Wait for tracking to be stable
                        if (!isTrackingStable) continue

                        // Limit total power-ups spawned per match
                        if (powerUpsSpawned >= config.powerUpCount) continue

                        // Max 2 power-ups in world at once
                        if (currentState.powerUps.size < 2) {
                            val newPowerUp =
                                    PowerUp.createRandom3D(
                                            distanceRange =
                                                    config.coinDistanceMin..config.coinDistanceMax,
                                            isMultiplayer = config.mode.isMultiplayer
                                    )
                            powerUpsSpawned++
                            _state.value =
                                    currentState.copy(powerUps = currentState.powerUps + newPowerUp)
                        }
                    }
                }
    }

    private fun startPowerUpCleanup() {
        powerUpCleanupJob?.cancel()
        powerUpCleanupJob =
                coroutineScope.launch {
                    while (true) {
                        delay(POWERUP_CLEANUP_INTERVAL_MS)
                        val currentState = _state.value as? GameState.Running ?: break

                        val validPowerUps = currentState.powerUps.filterNot { it.isExpired() }
                        if (validPowerUps.size != currentState.powerUps.size) {
                            _state.value = currentState.copy(powerUps = validPowerUps)
                        }
                    }
                }
    }

    private fun startComboTick() {
        comboTickJob?.cancel()
        comboTickJob =
                coroutineScope.launch {
                    while (true) {
                        delay(COMBO_TICK_INTERVAL_MS)
                        val currentState = _state.value as? GameState.Running ?: break

                        val expired = comboTracker.tick()
                        if (expired) {
                            onPlaySound(GameSound.COMBO_BREAK)
                        }

                        // Update state with current combo
                        _state.value =
                                currentState.copy(comboCount = comboTracker.state.value.count)
                    }
                }
    }

    private fun calculateScale(elapsedSeconds: Int): Float {
        if (!config.difficultyScaling) return 1.0f
        val intervals = elapsedSeconds / DIFFICULTY_INCREASE_INTERVAL
        val scale = 1.0f - (intervals * SCALE_REDUCTION_PER_INTERVAL)
        return scale.coerceAtLeast(MIN_COIN_SCALE)
    }

    private fun endGame(finalScore: Int) {
        cancelAllJobs()

        val highScore = getHighScore()
        val isNewHighScore = finalScore > highScore

        if (isNewHighScore) {
            saveHighScore(finalScore)
            onPlaySound(GameSound.NEW_HIGH_SCORE)
            onHaptic(HapticType.SUCCESS)
        } else {
            onPlaySound(GameSound.GAME_END)
            onHaptic(HapticType.MEDIUM)
        }

        val currentState = _state.value as? GameState.Running

        _state.value =
                GameState.Finished(
                        finalScore = finalScore,
                        isNewHighScore = isNewHighScore,
                        coinsCollected = currentState?.coinsCollected ?: 0,
                        bestCombo = bestComboThisGame,
                        powerUpsCollected = powerUpsCollectedThisGame,
                        wasPerfectRun = currentState?.isPerfectRun() ?: false,
                        playTimeMs = getCurrentTimeMillis() - gameStartTime,
                        gameMode = config.mode
                )
    }

    private fun cancelAllJobs() {
        gameLoopJob?.cancel()
        coinMaintenanceJob?.cancel()
        coinCleanupJob?.cancel()
        powerUpSpawnJob?.cancel()
        powerUpCleanupJob?.cancel()
        comboTickJob?.cancel()
        antiStallJob?.cancel()
        trackingStabilityJob?.cancel()
    }

    // === NEW AR GAMEPLAY FUNCTIONS ===

    private var antiStallJob: Job? = null
    private var trackingStabilityJob: Job? = null

    /** Wait for tracking to stabilize before spawning initial coins. */
    private fun startTrackingStabilityAndSpawn() {
        trackingStabilityJob?.cancel()
        trackingStabilityJob =
                coroutineScope.launch {
                    // Wait for tracking stability
                    delay(config.trackingStabilityDelayMs)
                    isTrackingStable = true

                    // Initial spawn via CoinManager
                    val currentState = _state.value as? GameState.Running ?: return@launch
                    val pose = cameraProvider.poseFlow.value

                    coinManager.spawnInitialCoins(
                            cameraPosition = pose.position,
                            cameraRotation = pose.rotation,
                            context = cameraProvider.getARContext()
                    )

                    updateCoinsFromManager(currentState)
                }
    }

    // Legacy functions removed or delegated to CoinManager
    private fun createOnboardingCoins(): List<Coin> = emptyList() // Now handled by CoinManager
    private fun scheduleRespawn() {
        /* Handled by CoinManager update loop */
    }
    private fun startAntiStallCheck() {
        /* CoinManager maintains density automatically */
    }
    private fun spawnSingleCoin() {
        /* Handled by CoinManager */
    }
}
