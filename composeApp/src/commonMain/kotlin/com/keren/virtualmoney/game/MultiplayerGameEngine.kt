package com.keren.virtualmoney.game

import com.keren.virtualmoney.ar.math.Vector3D
import com.keren.virtualmoney.audio.GameSound
import com.keren.virtualmoney.audio.HapticType
import com.keren.virtualmoney.backend.MultiplayerPhase
import com.keren.virtualmoney.multiplayer.MultiplayerEvent
import com.keren.virtualmoney.multiplayer.MultiplayerGameState
import com.keren.virtualmoney.multiplayer.MultiplayerPlayer
import com.keren.virtualmoney.multiplayer.MultiplayerStateManager
import com.keren.virtualmoney.platform.getCurrentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Game engine for multiplayer modes.
 * Wraps the base game engine and adds multiplayer synchronization.
 */
class MultiplayerGameEngine(
    private val coroutineScope: CoroutineScope,
    private val multiplayerManager: MultiplayerStateManager,
    private val gameMode: GameMode,
    private val onPlaySound: (GameSound) -> Unit = {},
    private val onHaptic: (HapticType) -> Unit = {}
) {
    private val _localState = MutableStateFlow<MultiplayerLocalState>(MultiplayerLocalState.Waiting)
    val localState: StateFlow<MultiplayerLocalState> = _localState.asStateFlow()

    // Local game data
    private var localScore: Int = 0
    private var localCoinsCollected: Int = 0
    private var localPenaltiesHit: Int = 0
    private var localPosition: Vector3D = Vector3D.ZERO
    private var comboCount: Int = 0
    private var lastComboTime: Long = 0

    // Jobs
    private var syncJob: Job? = null
    private var gameLoopJob: Job? = null
    private var eliminationCheckJob: Job? = null

    // Combo settings
    private val comboTimeoutMs = 2000L
    private val comboMultipliers = listOf(1.0f, 1.1f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f)

    /**
     * Start the multiplayer game.
     */
    fun startGame() {
        localScore = 0
        localCoinsCollected = 0
        localPenaltiesHit = 0
        comboCount = 0

        _localState.value = MultiplayerLocalState.Playing(
            score = 0,
            coins = createInitialCoins(),
            powerUps = emptyList(),
            activePowerUps = emptyList(),
            comboCount = 0
        )

        onPlaySound(GameSound.GAME_START)
        onHaptic(HapticType.MEDIUM)

        startGameLoop()
        startSyncLoop()

        when (gameMode) {
            GameMode.BATTLE_ROYALE -> startEliminationLoop()
            GameMode.TEAM_BATTLE -> {} // Team logic handled separately
            else -> {}
        }
    }

    /**
     * Collect a coin.
     */
    fun collectCoin(coinId: String) {
        val currentState = _localState.value as? MultiplayerLocalState.Playing ?: return
        val coin = currentState.coins.find { it.id == coinId } ?: return

        val isPenalty = Coin.isPenaltyCoin(coin.type)

        // Calculate points
        var basePoints = Coin.getValue(coin.type)

        // Update combo for good coins
        if (!isPenalty) {
            updateCombo()
            val multiplier = getComboMultiplier()
            basePoints = (basePoints * multiplier).toInt()
            localCoinsCollected++
            onPlaySound(GameSound.COIN_COLLECT)
            onHaptic(HapticType.MEDIUM)
        } else {
            resetCombo()
            localPenaltiesHit++
            onPlaySound(GameSound.PENALTY_HIT)
            onHaptic(HapticType.ERROR)
        }

        // Apply active power-up multipliers
        val powerUpMultiplier = currentState.getScoreMultiplier()
        val finalPoints = (basePoints * powerUpMultiplier).toInt()

        localScore = (localScore + finalPoints).coerceAtLeast(0)

        // Update state
        _localState.value = currentState.copy(
            score = localScore,
            coins = currentState.coins.filterNot { it.id == coinId },
            comboCount = comboCount
        )

        // Sync score to server
        coroutineScope.launch {
            multiplayerManager.updateScore(localScore)
        }
    }

    /**
     * Collect a power-up.
     */
    fun collectPowerUp(powerUpId: String) {
        val currentState = _localState.value as? MultiplayerLocalState.Playing ?: return
        val powerUp = currentState.powerUps.find { it.id == powerUpId } ?: return

        val activePowerUp = ActivePowerUp(type = powerUp.type)
        val newPowerUps = currentState.powerUps.filterNot { it.id == powerUpId }
        val newActivePowerUps = currentState.activePowerUps
            .filterNot { it.type == powerUp.type } + activePowerUp

        onPlaySound(GameSound.POWERUP_COLLECT)
        onHaptic(HapticType.SUCCESS)

        _localState.value = currentState.copy(
            powerUps = newPowerUps,
            activePowerUps = newActivePowerUps
        )
    }

    /**
     * Update player position (for location-based features).
     */
    fun updatePosition(position: Vector3D) {
        localPosition = position
        coroutineScope.launch {
            multiplayerManager.updatePosition(position)
        }
    }

    /**
     * End the game.
     */
    fun endGame() {
        cancelAllJobs()

        val currentState = _localState.value as? MultiplayerLocalState.Playing

        val mpState = multiplayerManager.gameState.value
        val finalRank = mpState?.currentPlayerRank ?: 1
        val isWinner = finalRank == 1

        if (isWinner) {
            onPlaySound(GameSound.NEW_HIGH_SCORE)
            onHaptic(HapticType.SUCCESS)
        } else {
            onPlaySound(GameSound.GAME_END)
            onHaptic(HapticType.MEDIUM)
        }

        _localState.value = MultiplayerLocalState.Finished(
            finalScore = localScore,
            finalRank = finalRank,
            totalPlayers = mpState?.players?.size ?: 1,
            coinsCollected = localCoinsCollected,
            isWinner = isWinner
        )
    }

    /**
     * Reset and cleanup.
     */
    fun cleanup() {
        cancelAllJobs()
        _localState.value = MultiplayerLocalState.Waiting
    }

    private fun createInitialCoins(): List<Coin> {
        val hapoalimCoins = (1..6).map {
            val distanceRange = listOf(0.3f..0.6f, 0.6f..1.0f, 1.0f..1.5f).random()
            Coin.createRandom3D(distanceRange = distanceRange, type = CoinType.BANK_HAPOALIM)
        }

        val penaltyTypes = listOf(CoinType.BANK_LEUMI, CoinType.BANK_MIZRAHI, CoinType.BANK_DISCOUNT)
        val penaltyCoins = (1..4).map {
            val distanceRange = listOf(0.3f..0.6f, 0.6f..1.0f, 1.0f..1.5f).random()
            Coin.createRandom3D(distanceRange = distanceRange, type = penaltyTypes.random())
        }

        return hapoalimCoins + penaltyCoins
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = coroutineScope.launch {
            while (true) {
                delay(200) // 5 updates per second
                val currentState = _localState.value as? MultiplayerLocalState.Playing ?: break

                // Maintain minimum coin count
                val hapoalimCount = currentState.coins.count { it.type == CoinType.BANK_HAPOALIM }
                val penaltyCount = currentState.coins.count { Coin.isPenaltyCoin(it.type) }

                var newCoins = currentState.coins

                if (hapoalimCount < 4) {
                    repeat(4 - hapoalimCount) {
                        val distanceRange = listOf(0.3f..0.6f, 0.6f..1.0f, 1.0f..1.5f).random()
                        newCoins = newCoins + Coin.createRandom3D(
                            distanceRange = distanceRange,
                            type = CoinType.BANK_HAPOALIM
                        )
                    }
                }

                if (penaltyCount < 3) {
                    val penaltyTypes = listOf(CoinType.BANK_LEUMI, CoinType.BANK_MIZRAHI, CoinType.BANK_DISCOUNT)
                    repeat(3 - penaltyCount) {
                        val distanceRange = listOf(0.3f..0.6f, 0.6f..1.0f, 1.0f..1.5f).random()
                        newCoins = newCoins + Coin.createRandom3D(
                            distanceRange = distanceRange,
                            type = penaltyTypes.random()
                        )
                    }
                }

                // Spawn power-ups occasionally
                val currentTime = getCurrentTimeMillis()
                if (currentState.powerUps.size < 2 && kotlin.random.Random.nextFloat() < 0.02f) {
                    val newPowerUp = PowerUp.createRandom3D(
                        distanceRange = 0.5f..1.2f,
                        isMultiplayer = true
                    )
                    _localState.value = currentState.copy(
                        coins = newCoins,
                        powerUps = currentState.powerUps + newPowerUp
                    )
                } else {
                    // Clean up expired active power-ups
                    val validActivePowerUps = currentState.activePowerUps.filterNot { it.isExpired() }

                    _localState.value = currentState.copy(
                        coins = newCoins,
                        activePowerUps = validActivePowerUps
                    )
                }

                // Check combo timeout
                if (comboCount > 0 && currentTime - lastComboTime > comboTimeoutMs) {
                    resetCombo()
                    _localState.value = (_localState.value as? MultiplayerLocalState.Playing)?.copy(
                        comboCount = comboCount
                    ) ?: continue
                }
            }
        }
    }

    private fun startSyncLoop() {
        syncJob?.cancel()
        syncJob = coroutineScope.launch {
            multiplayerManager.gameState.collect { mpState ->
                mpState?.let { state ->
                    // Check for game end
                    if (state.phase == MultiplayerPhase.FINISHED) {
                        endGame()
                    }

                    // Handle events from multiplayer state
                    multiplayerManager.events.value?.let { event ->
                        handleMultiplayerEvent(event, state)
                    }
                }
            }
        }
    }

    private fun handleMultiplayerEvent(event: MultiplayerEvent, state: MultiplayerGameState) {
        when (event) {
            is MultiplayerEvent.RankChanged -> {
                if (event.newRank < event.oldRank) {
                    onPlaySound(GameSound.COMBO_MILESTONE)
                    onHaptic(HapticType.SUCCESS)
                } else {
                    onHaptic(HapticType.WARNING)
                }
            }
            is MultiplayerEvent.EliminationWarning -> {
                onPlaySound(GameSound.COUNTDOWN_TICK)
                onHaptic(HapticType.WARNING)
            }
            is MultiplayerEvent.PlayerEliminated -> {
                // Could be us or another player
                val eliminatedPlayer = event.player
                if (eliminatedPlayer.isCurrentPlayer) {
                    endGame()
                }
            }
            is MultiplayerEvent.PlayerNearby -> {
                onHaptic(HapticType.LIGHT)
            }
            is MultiplayerEvent.GameEnded -> {
                endGame()
            }
            else -> {}
        }
    }

    private fun startEliminationLoop() {
        eliminationCheckJob?.cancel()
        eliminationCheckJob = coroutineScope.launch {
            // Battle royale elimination logic is handled server-side
            // This just monitors for local player elimination
            while (true) {
                delay(1000)
                val mpState = multiplayerManager.gameState.value ?: continue
                val currentPlayer = mpState.players.find { it.isCurrentPlayer }

                if (currentPlayer?.isEliminated == true) {
                    endGame()
                    break
                }
            }
        }
    }

    private fun updateCombo() {
        comboCount++
        lastComboTime = getCurrentTimeMillis()

        // Combo milestone sound
        if (comboCount == 5 || comboCount == 10 || comboCount % 10 == 0) {
            onPlaySound(GameSound.COMBO_MILESTONE)
            onHaptic(HapticType.SUCCESS)
        }
    }

    private fun resetCombo() {
        if (comboCount > 0) {
            onPlaySound(GameSound.COMBO_BREAK)
        }
        comboCount = 0
    }

    private fun getComboMultiplier(): Float {
        val index = (comboCount / 3).coerceAtMost(comboMultipliers.lastIndex)
        return comboMultipliers[index]
    }

    private fun cancelAllJobs() {
        syncJob?.cancel()
        gameLoopJob?.cancel()
        eliminationCheckJob?.cancel()
    }
}

/**
 * Local state for multiplayer game.
 */
sealed class MultiplayerLocalState {
    data object Waiting : MultiplayerLocalState()

    data class Playing(
        val score: Int,
        val coins: List<Coin>,
        val powerUps: List<PowerUp>,
        val activePowerUps: List<ActivePowerUp>,
        val comboCount: Int
    ) : MultiplayerLocalState() {
        fun getScoreMultiplier(): Float {
            return activePowerUps
                .filter { it.type == PowerUpType.SCORE_MULTIPLIER && !it.isExpired() }
                .fold(1f) { acc, _ -> acc * 2f }
        }

        fun hasMagnet(): Boolean = activePowerUps.any {
            it.type == PowerUpType.MAGNET && !it.isExpired()
        }

        fun hasShield(): Boolean = activePowerUps.any {
            it.type == PowerUpType.SHIELD && !it.isExpired()
        }
    }

    data class Finished(
        val finalScore: Int,
        val finalRank: Int,
        val totalPlayers: Int,
        val coinsCollected: Int,
        val isWinner: Boolean
    ) : MultiplayerLocalState()
}
