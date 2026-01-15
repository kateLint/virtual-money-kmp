package com.keren.virtualmoney.game

import com.keren.virtualmoney.audio.GameSound
import com.keren.virtualmoney.audio.HapticType
import com.keren.virtualmoney.backend.MultiplayerPhase
import com.keren.virtualmoney.multiplayer.MultiplayerPlayer
import com.keren.virtualmoney.platform.getCurrentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Configuration for Battle Royale elimination.
 */
data class BattleRoyaleConfig(
    val totalDurationSeconds: Int = 300,      // 5 minute total game
    val eliminationIntervalSeconds: Int = 30, // Eliminate every 30 seconds
    val eliminationPercentage: Float = 0.2f,  // Eliminate bottom 20% each round
    val minPlayersForElimination: Int = 3,    // Don't eliminate if 3 or fewer remain
    val warningCountdownSeconds: Int = 10,    // Show warning 10 seconds before elimination
    val finalShowdownPlayers: Int = 5,        // Trigger final showdown at 5 players
    val finalShowdownDuration: Int = 60       // 60 seconds for final showdown
)

/**
 * State of the Battle Royale game.
 */
data class BattleRoyaleState(
    val phase: BattleRoyalePhase,
    val timeRemaining: Int,
    val eliminationCountdown: Int?,
    val nextEliminationCount: Int?,
    val alivePlayers: Int,
    val eliminatedPlayers: List<String>,
    val safeZoneRadius: Float = 1.0f // Percentage of original play area
)

/**
 * Phases of Battle Royale.
 */
enum class BattleRoyalePhase {
    COUNTDOWN,           // Pre-game countdown
    GRACE_PERIOD,        // Initial period with no eliminations
    NORMAL,              // Regular gameplay with periodic eliminations
    ELIMINATION_WARNING, // About to eliminate
    FINAL_SHOWDOWN,      // Final few players
    FINISHED             // Game over
}

/**
 * Battle Royale elimination event.
 */
sealed class BattleRoyaleEvent {
    data class EliminationWarning(val countdown: Int, val count: Int) : BattleRoyaleEvent()
    data class PlayersEliminated(val players: List<String>, val remainingCount: Int) : BattleRoyaleEvent()
    data object FinalShowdownStarted : BattleRoyaleEvent()
    data class YouWereEliminated(val finalRank: Int) : BattleRoyaleEvent()
    data class GameEnded(val winner: String?, val yourRank: Int) : BattleRoyaleEvent()
    data class SafeZoneShrinking(val newRadius: Float) : BattleRoyaleEvent()
}

/**
 * Manages Battle Royale elimination mechanics.
 */
class BattleRoyaleManager(
    private val scope: CoroutineScope,
    private val config: BattleRoyaleConfig = BattleRoyaleConfig(),
    private val currentPlayerId: String,
    private val onPlaySound: (GameSound) -> Unit = {},
    private val onHaptic: (HapticType) -> Unit = {},
    private val onPlayerEliminated: suspend (List<String>) -> Unit = {}
) {
    private val _state = MutableStateFlow(
        BattleRoyaleState(
            phase = BattleRoyalePhase.COUNTDOWN,
            timeRemaining = config.totalDurationSeconds,
            eliminationCountdown = null,
            nextEliminationCount = null,
            alivePlayers = 0,
            eliminatedPlayers = emptyList()
        )
    )
    val state: StateFlow<BattleRoyaleState> = _state.asStateFlow()

    private val _events = MutableStateFlow<BattleRoyaleEvent?>(null)
    val events: StateFlow<BattleRoyaleEvent?> = _events.asStateFlow()

    private var gameLoopJob: Job? = null
    private var eliminationJob: Job? = null

    private var allPlayers: MutableList<PlayerRanking> = mutableListOf()
    private var gameStartTime: Long = 0
    private var nextEliminationTime: Long = 0

    data class PlayerRanking(
        val playerId: String,
        var score: Int,
        var isEliminated: Boolean = false,
        var eliminationRank: Int? = null
    )

    /**
     * Initialize with player list.
     */
    fun initialize(players: List<MultiplayerPlayer>) {
        allPlayers = players.map {
            PlayerRanking(
                playerId = it.odId,
                score = it.score,
                isEliminated = false
            )
        }.toMutableList()

        _state.value = _state.value.copy(
            alivePlayers = allPlayers.size
        )
    }

    /**
     * Start the Battle Royale game.
     */
    fun startGame() {
        gameStartTime = getCurrentTimeMillis()
        nextEliminationTime = gameStartTime + (config.eliminationIntervalSeconds * 1000L)

        _state.value = _state.value.copy(
            phase = BattleRoyalePhase.GRACE_PERIOD,
            timeRemaining = config.totalDurationSeconds
        )

        startGameLoop()
    }

    /**
     * Update player score.
     */
    fun updatePlayerScore(playerId: String, newScore: Int) {
        allPlayers.find { it.playerId == playerId }?.score = newScore
    }

    /**
     * Get current player rankings.
     */
    fun getRankings(): List<PlayerRanking> {
        return allPlayers
            .filter { !it.isEliminated }
            .sortedByDescending { it.score }
    }

    /**
     * Get player's current rank.
     */
    fun getPlayerRank(playerId: String): Int {
        val rankings = getRankings()
        return rankings.indexOfFirst { it.playerId == playerId } + 1
    }

    /**
     * Check if a player is eliminated.
     */
    fun isEliminated(playerId: String): Boolean {
        return allPlayers.find { it.playerId == playerId }?.isEliminated ?: true
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = scope.launch {
            // Initial grace period (15 seconds)
            delay(15000)

            _state.value = _state.value.copy(phase = BattleRoyalePhase.NORMAL)

            while (true) {
                delay(1000)

                val currentState = _state.value
                val alivePlayers = allPlayers.count { !it.isEliminated }

                // Update time
                val elapsedSeconds = ((getCurrentTimeMillis() - gameStartTime) / 1000).toInt()
                val timeRemaining = (config.totalDurationSeconds - elapsedSeconds).coerceAtLeast(0)

                // Check for final showdown
                if (alivePlayers <= config.finalShowdownPlayers && currentState.phase != BattleRoyalePhase.FINAL_SHOWDOWN) {
                    startFinalShowdown()
                    continue
                }

                // Check for game end
                if (timeRemaining <= 0 || alivePlayers <= 1) {
                    endGame()
                    break
                }

                // Check for elimination
                val timeToElimination = ((nextEliminationTime - getCurrentTimeMillis()) / 1000).toInt()

                if (timeToElimination <= config.warningCountdownSeconds &&
                    currentState.phase == BattleRoyalePhase.NORMAL
                ) {
                    // Enter warning phase
                    val eliminationCount = calculateEliminationCount()
                    _state.value = currentState.copy(
                        phase = BattleRoyalePhase.ELIMINATION_WARNING,
                        timeRemaining = timeRemaining,
                        eliminationCountdown = timeToElimination,
                        nextEliminationCount = eliminationCount
                    )

                    _events.value = BattleRoyaleEvent.EliminationWarning(timeToElimination, eliminationCount)
                    onPlaySound(GameSound.COUNTDOWN_TICK)
                    onHaptic(HapticType.WARNING)
                } else if (timeToElimination <= 0 && alivePlayers > config.minPlayersForElimination) {
                    // Execute elimination
                    executeElimination()

                    // Schedule next elimination
                    nextEliminationTime = getCurrentTimeMillis() + (config.eliminationIntervalSeconds * 1000L)

                    _state.value = _state.value.copy(
                        phase = BattleRoyalePhase.NORMAL,
                        eliminationCountdown = null,
                        nextEliminationCount = null
                    )
                } else {
                    // Normal state update
                    _state.value = currentState.copy(
                        timeRemaining = timeRemaining,
                        eliminationCountdown = if (currentState.phase == BattleRoyalePhase.ELIMINATION_WARNING) {
                            timeToElimination.coerceAtLeast(0)
                        } else null
                    )
                }
            }
        }
    }

    private fun calculateEliminationCount(): Int {
        val alivePlayers = allPlayers.count { !it.isEliminated }
        val eliminationCount = (alivePlayers * config.eliminationPercentage).toInt().coerceAtLeast(1)

        // Don't eliminate so many that we go below minimum
        val maxElimination = alivePlayers - config.minPlayersForElimination
        return eliminationCount.coerceAtMost(maxElimination.coerceAtLeast(0))
    }

    private suspend fun executeElimination() {
        val eliminationCount = calculateEliminationCount()
        if (eliminationCount <= 0) return

        // Get players sorted by score (ascending - lowest first)
        val rankings = allPlayers
            .filter { !it.isEliminated }
            .sortedBy { it.score }

        // Eliminate bottom players
        val toEliminate = rankings.take(eliminationCount)
        val eliminatedIds = mutableListOf<String>()

        toEliminate.forEachIndexed { index, player ->
            player.isEliminated = true
            player.eliminationRank = allPlayers.count { !it.isEliminated } + index + 1
            eliminatedIds.add(player.playerId)
        }

        val remainingCount = allPlayers.count { !it.isEliminated }

        _state.value = _state.value.copy(
            alivePlayers = remainingCount,
            eliminatedPlayers = _state.value.eliminatedPlayers + eliminatedIds
        )

        // Check if current player was eliminated
        if (currentPlayerId in eliminatedIds) {
            val myRanking = allPlayers.find { it.playerId == currentPlayerId }
            _events.value = BattleRoyaleEvent.YouWereEliminated(myRanking?.eliminationRank ?: remainingCount + 1)
            onPlaySound(GameSound.GAME_END)
            onHaptic(HapticType.ERROR)
        } else {
            _events.value = BattleRoyaleEvent.PlayersEliminated(eliminatedIds, remainingCount)
            onPlaySound(GameSound.POWERUP_COLLECT) // Survival sound
            onHaptic(HapticType.SUCCESS)
        }

        // Notify callback
        onPlayerEliminated(eliminatedIds)
    }

    private fun startFinalShowdown() {
        _state.value = _state.value.copy(
            phase = BattleRoyalePhase.FINAL_SHOWDOWN,
            timeRemaining = config.finalShowdownDuration,
            eliminationCountdown = null,
            nextEliminationCount = null
        )

        _events.value = BattleRoyaleEvent.FinalShowdownStarted
        onPlaySound(GameSound.COMBO_MILESTONE)
        onHaptic(HapticType.SUCCESS)

        // Start final showdown timer
        scope.launch {
            var remaining = config.finalShowdownDuration
            while (remaining > 0) {
                delay(1000)
                remaining--
                _state.value = _state.value.copy(timeRemaining = remaining)

                // Warning sounds in final 10 seconds
                if (remaining <= 10) {
                    onPlaySound(GameSound.COUNTDOWN_TICK)
                }
            }
            endGame()
        }
    }

    private fun endGame() {
        gameLoopJob?.cancel()
        eliminationJob?.cancel()

        val rankings = getRankings()
        val winner = rankings.firstOrNull()

        val myRanking = getPlayerRank(currentPlayerId)
        val isWinner = myRanking == 1

        _state.value = _state.value.copy(
            phase = BattleRoyalePhase.FINISHED
        )

        _events.value = BattleRoyaleEvent.GameEnded(
            winner = winner?.playerId,
            yourRank = if (isEliminated(currentPlayerId)) {
                allPlayers.find { it.playerId == currentPlayerId }?.eliminationRank ?: allPlayers.size
            } else {
                myRanking
            }
        )

        if (isWinner) {
            onPlaySound(GameSound.NEW_HIGH_SCORE)
            onHaptic(HapticType.SUCCESS)
        } else {
            onPlaySound(GameSound.GAME_END)
            onHaptic(HapticType.MEDIUM)
        }
    }

    /**
     * Shrink the safe zone (for location-based BR).
     */
    fun shrinkSafeZone(percentage: Float) {
        val currentRadius = _state.value.safeZoneRadius
        val newRadius = currentRadius * (1 - percentage)

        _state.value = _state.value.copy(safeZoneRadius = newRadius)
        _events.value = BattleRoyaleEvent.SafeZoneShrinking(newRadius)

        onPlaySound(GameSound.FREEZE_CAST) // Zone shrink sound
        onHaptic(HapticType.WARNING)
    }

    /**
     * Cleanup resources.
     */
    fun cleanup() {
        gameLoopJob?.cancel()
        eliminationJob?.cancel()
        allPlayers.clear()
        _state.value = BattleRoyaleState(
            phase = BattleRoyalePhase.COUNTDOWN,
            timeRemaining = config.totalDurationSeconds,
            eliminationCountdown = null,
            nextEliminationCount = null,
            alivePlayers = 0,
            eliminatedPlayers = emptyList()
        )
    }
}
