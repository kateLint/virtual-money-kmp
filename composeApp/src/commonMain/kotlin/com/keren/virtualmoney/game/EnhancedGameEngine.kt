package com.keren.virtualmoney.game

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

/**
 * Enhanced game engine with power-ups, combos, and multiple game modes.
 */
class EnhancedGameEngine(
    private val coroutineScope: CoroutineScope,
    private val config: GameConfig = GameConfig.classic(),
    private val onPlaySound: (GameSound) -> Unit = {},
    private val onHaptic: (HapticType) -> Unit = {},
    private val getHighScore: () -> Int,
    private val saveHighScore: (Int) -> Unit
) {
    private val _state = MutableStateFlow<GameState>(GameState.Ready)
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val comboTracker = ComboTracker()
    val comboState: StateFlow<ComboState> = comboTracker.state

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
        private const val MIN_HAPOALIM_COIN_COUNT = 4
        private const val MIN_PENALTY_COIN_COUNT = 3
        private const val COIN_CLEANUP_INTERVAL_MS = 100L
        private const val COIN_MAINTENANCE_INTERVAL_MS = 200L
        private const val POWERUP_SPAWN_INTERVAL_MS = 8000L // Spawn power-up every 8 seconds
        private const val POWERUP_CLEANUP_INTERVAL_MS = 500L
        private const val COMBO_TICK_INTERVAL_MS = 100L
        private const val DIFFICULTY_INCREASE_INTERVAL = 15
        private const val SCALE_REDUCTION_PER_INTERVAL = 0.1f
        private const val MIN_COIN_SCALE = 0.5f
    }

    /**
     * Start a new game with the configured mode.
     */
    fun startGame() {
        if (_state.value !is GameState.Ready) return

        gameStartTime = getCurrentTimeMillis()
        powerUpsCollectedThisGame = 0
        bestComboThisGame = 0
        comboTracker.reset()

        val initialCoins = createInitialCoins()

        _state.value = GameState.Running(
            timeRemaining = config.duration,
            score = 0,
            coins = initialCoins,
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
        startCoinMaintenance()
        startComboTick()

        if (config.powerUpsEnabled) {
            startPowerUpSpawn()
            startPowerUpCleanup()
        }
    }

    /**
     * Collect a coin.
     */
    fun collectCoin(coinId: String) {
        val currentState = _state.value as? GameState.Running ?: return
        val coin = currentState.coins.find { it.id == coinId } ?: return

        val isPenalty = Coin.isPenaltyCoin(coin.type)

        // Check shield for penalties
        if (isPenalty && currentState.hasShield()) {
            // Shield blocks penalty - just remove coin, no point loss
            onPlaySound(GameSound.SHIELD_BLOCK)
            onHaptic(HapticType.LIGHT)

            _state.value = currentState.copy(
                coins = currentState.coins.filterNot { it.id == coinId }
            )
            return
        }

        // Calculate points
        var basePoints = Coin.getValue(coin.type)

        // Apply combo multiplier for good coins
        val comboMultiplier = if (!isPenalty) {
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
        val newCoinsCollected = if (!isPenalty) currentState.coinsCollected + 1 else currentState.coinsCollected
        val newPenaltiesHit = if (isPenalty) currentState.penaltiesHit + 1 else currentState.penaltiesHit

        // Update lives for survival mode
        var newLives = currentState.lives
        if (isPenalty && config.mode == GameMode.SURVIVAL) {
            newLives--
        }

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

        _state.value = currentState.copy(
            score = newScore,
            coins = currentState.coins.filterNot { it.id == coinId },
            comboCount = comboTracker.state.value.count,
            lives = newLives,
            coinsCollected = newCoinsCollected,
            penaltiesHit = newPenaltiesHit
        )

        // Check for game over in survival mode
        if (config.mode == GameMode.SURVIVAL && newLives <= 0) {
            endGame(newScore)
        }
    }

    /**
     * Collect a power-up.
     */
    fun collectPowerUp(powerUpId: String) {
        val currentState = _state.value as? GameState.Running ?: return
        val powerUp = currentState.powerUps.find { it.id == powerUpId } ?: return

        powerUpsCollectedThisGame++

        // Create active power-up effect
        val activePowerUp = ActivePowerUp(type = powerUp.type)

        // Remove from world, add to active
        val newPowerUps = currentState.powerUps.filterNot { it.id == powerUpId }
        val newActivePowerUps = currentState.activePowerUps
            .filterNot { it.type == powerUp.type } + activePowerUp // Replace existing of same type

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

        _state.value = currentState.copy(
            powerUps = newPowerUps,
            activePowerUps = newActivePowerUps
        )
    }

    /**
     * Reset game to ready state.
     */
    fun resetGame() {
        cancelAllJobs()
        comboTracker.reset()
        _state.value = GameState.Ready
    }

    private fun createInitialCoins(): List<Coin> {
        val hapoalimCoins = (1..config.initialCoinCount).map {
            val distanceRange = listOf(0.3f..0.6f, 0.6f..1.0f, 1.0f..1.5f).random()
            Coin.createRandom3D(distanceRange = distanceRange, type = CoinType.BANK_HAPOALIM)
        }

        val penaltyTypes = listOf(CoinType.BANK_LEUMI, CoinType.BANK_MIZRAHI, CoinType.BANK_DISCOUNT)
        val penaltyCoins = (1..config.initialPenaltyCoinCount).map {
            val distanceRange = listOf(0.3f..0.6f, 0.6f..1.0f, 1.0f..1.5f).random()
            Coin.createRandom3D(distanceRange = distanceRange, type = penaltyTypes.random())
        }

        return hapoalimCoins + penaltyCoins
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = coroutineScope.launch {
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
        val elapsedTime = if (config.mode == GameMode.SURVIVAL) {
            ((getCurrentTimeMillis() - gameStartTime) / 1000).toInt()
        } else {
            config.duration - currentState.timeRemaining
        }

        val newScale = calculateScale(elapsedTime)
        val shouldUpdateScale = elapsedTime % DIFFICULTY_INCREASE_INTERVAL == 0

        val updatedCoins = if (shouldUpdateScale && config.difficultyScaling && newScale < 1.0f) {
            currentState.coins.map { it.copy(scale = newScale) }
        } else {
            currentState.coins
        }

        // Clean up expired active power-ups
        val validActivePowerUps = currentState.activePowerUps.filterNot { it.isExpired() }

        _state.value = currentState.copy(
            timeRemaining = if (config.mode == GameMode.SURVIVAL) currentState.timeRemaining else currentState.timeRemaining,
            coins = updatedCoins,
            activePowerUps = validActivePowerUps
        )
    }

    private fun startCoinMaintenance() {
        coinMaintenanceJob?.cancel()
        coinMaintenanceJob = coroutineScope.launch {
            while (true) {
                delay(COIN_MAINTENANCE_INTERVAL_MS)
                val currentState = _state.value as? GameState.Running ?: break

                val hapoalimCount = currentState.coins.count { it.type == CoinType.BANK_HAPOALIM }
                val penaltyCount = currentState.coins.count { Coin.isPenaltyCoin(it.type) }

                val elapsedTime = if (config.mode == GameMode.SURVIVAL) {
                    ((getCurrentTimeMillis() - gameStartTime) / 1000).toInt()
                } else {
                    config.duration - currentState.timeRemaining
                }
                val currentScale = calculateScale(elapsedTime)

                var newCoins = currentState.coins

                if (hapoalimCount < MIN_HAPOALIM_COIN_COUNT) {
                    repeat(MIN_HAPOALIM_COIN_COUNT - hapoalimCount) {
                        val distanceRange = listOf(0.3f..0.6f, 0.6f..1.0f, 1.0f..1.5f).random()
                        newCoins = newCoins + Coin.createRandom3D(
                            distanceRange = distanceRange,
                            scale = currentScale,
                            type = CoinType.BANK_HAPOALIM
                        )
                    }
                }

                if (penaltyCount < MIN_PENALTY_COIN_COUNT) {
                    val penaltyTypes = listOf(CoinType.BANK_LEUMI, CoinType.BANK_MIZRAHI, CoinType.BANK_DISCOUNT)
                    repeat(MIN_PENALTY_COIN_COUNT - penaltyCount) {
                        val distanceRange = listOf(0.3f..0.6f, 0.6f..1.0f, 1.0f..1.5f).random()
                        newCoins = newCoins + Coin.createRandom3D(
                            distanceRange = distanceRange,
                            scale = currentScale,
                            type = penaltyTypes.random()
                        )
                    }
                }

                if (newCoins.size != currentState.coins.size) {
                    _state.value = currentState.copy(coins = newCoins)
                }
            }
        }
    }

    private fun startCoinCleanup() {
        coinCleanupJob?.cancel()
        coinCleanupJob = coroutineScope.launch {
            while (true) {
                delay(COIN_CLEANUP_INTERVAL_MS)
                val currentState = _state.value as? GameState.Running ?: break
                val currentTime = getCurrentTimeMillis()

                val validCoins = currentState.coins.filter { coin ->
                    if (Coin.isPenaltyCoin(coin.type)) {
                        (currentTime - coin.spawnTime) < Coin.PENALTY_COIN_LIFETIME_MS
                    } else {
                        true
                    }
                }

                if (validCoins.size != currentState.coins.size) {
                    _state.value = currentState.copy(coins = validCoins)
                }
            }
        }
    }

    private fun startPowerUpSpawn() {
        powerUpSpawnJob?.cancel()
        powerUpSpawnJob = coroutineScope.launch {
            delay(5000) // Initial delay before first power-up
            while (true) {
                delay(POWERUP_SPAWN_INTERVAL_MS)
                val currentState = _state.value as? GameState.Running ?: break

                // Max 2 power-ups in world at once
                if (currentState.powerUps.size < 2) {
                    val newPowerUp = PowerUp.createRandom3D(
                        distanceRange = 0.5f..1.2f,
                        isMultiplayer = config.mode.isMultiplayer
                    )
                    _state.value = currentState.copy(
                        powerUps = currentState.powerUps + newPowerUp
                    )
                }
            }
        }
    }

    private fun startPowerUpCleanup() {
        powerUpCleanupJob?.cancel()
        powerUpCleanupJob = coroutineScope.launch {
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
        comboTickJob = coroutineScope.launch {
            while (true) {
                delay(COMBO_TICK_INTERVAL_MS)
                val currentState = _state.value as? GameState.Running ?: break

                val expired = comboTracker.tick()
                if (expired) {
                    onPlaySound(GameSound.COMBO_BREAK)
                }

                // Update state with current combo
                _state.value = currentState.copy(
                    comboCount = comboTracker.state.value.count
                )
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

        _state.value = GameState.Finished(
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
    }
}
