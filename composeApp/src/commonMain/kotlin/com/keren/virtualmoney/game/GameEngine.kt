package com.keren.virtualmoney.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Core game engine that manages game logic, state transitions, and timing.
 * Uses StateFlow for reactive state management compatible with Compose.
 */
class GameEngine(
    private val coroutineScope: CoroutineScope,
    private val onCoinCollected: () -> Unit, // Callback for haptic/sound feedback
    private val getHighScore: () -> Int,
    private val saveHighScore: (Int) -> Unit
) {
    private val _state = MutableStateFlow<GameState>(GameState.Ready)
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var gameLoopJob: Job? = null
    private var coinSpawnJob: Job? = null
    private var blackCoinSpawnJob: Job? = null
    private var coinCleanupJob: Job? = null
    private var coinMaintenanceJob: Job? = null

    companion object {
        private const val GAME_DURATION_SECONDS = 60
        private const val TICK_INTERVAL_MS = 1000L
        private const val MIN_HAPOALIM_COIN_COUNT = 4      // Always keep at least 4 Hapoalim coins
        private const val MIN_PENALTY_COIN_COUNT = 3       // Always keep at least 3 penalty coins
        private const val HAPOALIM_RESPAWN_DELAY_MS = 1000L // Hapoalim coin respawns after 1 second
        private const val COIN_CLEANUP_INTERVAL_MS = 100L // Check for expired coins every 100ms
        private const val COIN_MAINTENANCE_INTERVAL_MS = 500L // Check coin counts every 500ms
        private const val DIFFICULTY_INCREASE_INTERVAL = 15 // seconds
        private const val SCALE_REDUCTION_PER_INTERVAL = 0.1f
        private const val MIN_COIN_SCALE = 0.5f
    }

    /**
     * Starts a new game session.
     */
    fun startGame() {
        if (_state.value !is GameState.Ready) return

        // Start with exactly 4 Hapoalim coins + 3 penalty coins (random other banks)
        val initialHapoalimCoins = List(MIN_HAPOALIM_COIN_COUNT) {
            Coin.createRandom().copy(type = CoinType.BANK_HAPOALIM)
        }

        val penaltyBankTypes = listOf(
            CoinType.BANK_LEUMI,
            CoinType.BANK_MIZRAHI,
            CoinType.BANK_DISCOUNT
        )
        val initialPenaltyCoins = List(MIN_PENALTY_COIN_COUNT) {
            Coin.createRandom().copy(type = penaltyBankTypes.random())
        }

        _state.value = GameState.Running(
            timeRemaining = GAME_DURATION_SECONDS,
            score = 0,
            coins = initialHapoalimCoins + initialPenaltyCoins
        )

        startGameLoop()
        startCoinCleanup()
        startCoinMaintenance()
    }

    /**
     * Handles coin collection when user taps a coin.
     * @param coinId The ID of the coin that was tapped
     */
    fun collectCoin(coinId: String) {
        val currentState = _state.value as? GameState.Running ?: return

        // Verify coin exists
        val coin = currentState.coins.find { it.id == coinId } ?: return

        // Calculate points based on coin type
        val points = Coin.getValue(coin.type)
        val newScore = (currentState.score + points).coerceAtLeast(0) // Don't go below 0

        // Remove collected coin
        val updatedCoins = currentState.coins.filterNot { it.id == coinId }

        // Update score and coins
        _state.value = currentState.copy(
            score = newScore,
            coins = updatedCoins
        )

        // Schedule delayed respawn for Hapoalim coins (1 second)
        if (coin.type == CoinType.BANK_HAPOALIM) {
            coroutineScope.launch {
                delay(HAPOALIM_RESPAWN_DELAY_MS)
                val state = _state.value as? GameState.Running ?: return@launch
                val elapsedTime = GAME_DURATION_SECONDS - state.timeRemaining
                val currentScale = calculateScale(elapsedTime)
                val newCoin = Coin.createRandom(currentScale).copy(type = CoinType.BANK_HAPOALIM)

                _state.value = state.copy(
                    coins = state.coins + newCoin
                )
            }
        }
        // Penalty coins respawn is handled by coinMaintenance

        // Trigger feedback (haptic + sound)
        onCoinCollected()
    }

    /**
     * Resets the game to ready state.
     */
    fun resetGame() {
        gameLoopJob?.cancel()
        coinSpawnJob?.cancel()
        blackCoinSpawnJob?.cancel()
        coinCleanupJob?.cancel()
        coinMaintenanceJob?.cancel()
        _state.value = GameState.Ready
    }

    /**
     * Main game loop - runs every second to update timer.
     */
    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = coroutineScope.launch {
            while (true) {
                delay(TICK_INTERVAL_MS)
                val currentState = _state.value as? GameState.Running ?: break

                if (currentState.timeRemaining <= 1) {
                    // Game over
                    endGame(currentState.score)
                    break
                } else {
                    // Update timer and check for difficulty increase
                    val newTimeRemaining = currentState.timeRemaining - 1
                    val elapsedTime = GAME_DURATION_SECONDS - newTimeRemaining
                    val newScale = calculateScale(elapsedTime)

                    // Update coin scales if difficulty increased
                    val shouldUpdateScale = elapsedTime % DIFFICULTY_INCREASE_INTERVAL == 0
                    val updatedCoins = if (shouldUpdateScale && newScale < 1.0f) {
                        currentState.coins.map { it.copy(scale = newScale) }
                    } else {
                        currentState.coins
                    }

                    _state.value = currentState.copy(
                        timeRemaining = newTimeRemaining,
                        coins = updatedCoins
                    )
                }
            }
        }
    }

    /**
     * Maintains minimum coin counts on screen.
     * Checks every 500ms and spawns coins if below minimum (4 Hapoalim, 3 penalty).
     */
    private fun startCoinMaintenance() {
        coinMaintenanceJob?.cancel()
        coinMaintenanceJob = coroutineScope.launch {
            while (true) {
                delay(COIN_MAINTENANCE_INTERVAL_MS)
                val currentState = _state.value as? GameState.Running ?: break

                val hapoalimCount = currentState.coins.count { it.type == CoinType.BANK_HAPOALIM }
                val penaltyCount = currentState.coins.count { Coin.isPenaltyCoin(it.type) }

                val elapsedTime = GAME_DURATION_SECONDS - currentState.timeRemaining
                val currentScale = calculateScale(elapsedTime)

                var newCoins = currentState.coins

                // Spawn Hapoalim coins if below minimum
                if (hapoalimCount < MIN_HAPOALIM_COIN_COUNT) {
                    repeat(MIN_HAPOALIM_COIN_COUNT - hapoalimCount) {
                        newCoins = newCoins + Coin.createRandom(currentScale).copy(type = CoinType.BANK_HAPOALIM)
                    }
                }

                // Spawn penalty coins if below minimum
                if (penaltyCount < MIN_PENALTY_COIN_COUNT) {
                    val penaltyBankTypes = listOf(
                        CoinType.BANK_LEUMI,
                        CoinType.BANK_MIZRAHI,
                        CoinType.BANK_DISCOUNT
                    )
                    repeat(MIN_PENALTY_COIN_COUNT - penaltyCount) {
                        newCoins = newCoins + Coin.createRandom(currentScale).copy(type = penaltyBankTypes.random())
                    }
                }

                if (newCoins.size != currentState.coins.size) {
                    _state.value = currentState.copy(coins = newCoins)
                }
            }
        }
    }

    /**
     * Periodically removes expired penalty coins.
     * Checks every 100ms and removes penalty coins older than 2 seconds.
     */
    private fun startCoinCleanup() {
        coinCleanupJob?.cancel()
        coinCleanupJob = coroutineScope.launch {
            while (true) {
                delay(COIN_CLEANUP_INTERVAL_MS)
                val currentState = _state.value as? GameState.Running ?: break
                val currentTime = System.currentTimeMillis()

                // Filter out expired penalty coins
                val validCoins = currentState.coins.filter { coin ->
                    if (Coin.isPenaltyCoin(coin.type)) {
                        // Remove penalty coins older than 2 seconds
                        (currentTime - coin.spawnTime) < Coin.PENALTY_COIN_LIFETIME_MS
                    } else {
                        // Keep all Hapoalim coins
                        true
                    }
                }

                // Update state only if coins were removed
                if (validCoins.size != currentState.coins.size) {
                    _state.value = currentState.copy(coins = validCoins)
                }
            }
        }
    }

    /**
     * Calculates coin scale based on elapsed time.
     * Scale reduces every DIFFICULTY_INCREASE_INTERVAL seconds.
     */
    private fun calculateScale(elapsedSeconds: Int): Float {
        val intervals = elapsedSeconds / DIFFICULTY_INCREASE_INTERVAL
        val scale = 1.0f - (intervals * SCALE_REDUCTION_PER_INTERVAL)
        return scale.coerceAtLeast(MIN_COIN_SCALE)
    }

    /**
     * Ends the game and checks for high score.
     */
    private fun endGame(finalScore: Int) {
        val highScore = getHighScore()
        val isNewHighScore = finalScore > highScore

        if (isNewHighScore) {
            saveHighScore(finalScore)
        }

        _state.value = GameState.Finished(
            finalScore = finalScore,
            isNewHighScore = isNewHighScore
        )
    }
}
