package com.keren.virtualmoney.multiplayer

import com.keren.virtualmoney.ar.math.Vector3D
import com.keren.virtualmoney.backend.GameRepository
import com.keren.virtualmoney.backend.Lobby
import com.keren.virtualmoney.backend.LobbyStatus
import com.keren.virtualmoney.backend.MultiplayerGameData
import com.keren.virtualmoney.backend.MultiplayerPhase
import com.keren.virtualmoney.game.GameMode
import com.keren.virtualmoney.platform.getCurrentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Connection state for multiplayer.
 */
sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

/**
 * Matchmaking state.
 */
sealed class MatchmakingState {
    data object Idle : MatchmakingState()
    data class Searching(val gameMode: GameMode, val startTime: Long) : MatchmakingState()
    data class Found(val lobbyId: String) : MatchmakingState()
    data class Error(val message: String) : MatchmakingState()
}

/**
 * Current player data in multiplayer.
 */
data class MultiplayerPlayer(
    val odId: String,
    val displayName: String,
    val score: Int = 0,
    val rank: Int = 0,
    val position: Vector3D = Vector3D.ZERO,
    val isEliminated: Boolean = false,
    val teamId: Int? = null,
    val isCurrentPlayer: Boolean = false
)

/**
 * Complete multiplayer game state.
 */
data class MultiplayerGameState(
    val gameId: String,
    val phase: MultiplayerPhase,
    val timeRemaining: Int,
    val players: List<MultiplayerPlayer>,
    val currentPlayerRank: Int,
    val eliminationCountdown: Int? = null,
    val nextEliminationCount: Int? = null,
    val leadingPlayer: MultiplayerPlayer? = null,
    val nearbyPlayers: List<MultiplayerPlayer> = emptyList()
)

/**
 * Events emitted during multiplayer gameplay.
 */
sealed class MultiplayerEvent {
    data class PlayerJoined(val player: MultiplayerPlayer) : MultiplayerEvent()
    data class PlayerLeft(val playerId: String) : MultiplayerEvent()
    data class PlayerEliminated(val player: MultiplayerPlayer) : MultiplayerEvent()
    data class RankChanged(val oldRank: Int, val newRank: Int) : MultiplayerEvent()
    data class EliminationWarning(val countdown: Int, val eliminationCount: Int) : MultiplayerEvent()
    data object PlayerNearby : MultiplayerEvent()
    data class GamePhaseChanged(val phase: MultiplayerPhase) : MultiplayerEvent()
    data class GameEnded(val finalRank: Int, val isWinner: Boolean) : MultiplayerEvent()
}

/**
 * Manages multiplayer game state and synchronization.
 */
class MultiplayerStateManager(
    private val gameRepository: GameRepository,
    private val currentUserId: String,
    private val scope: CoroutineScope
) {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _matchmakingState = MutableStateFlow<MatchmakingState>(MatchmakingState.Idle)
    val matchmakingState: StateFlow<MatchmakingState> = _matchmakingState.asStateFlow()

    private val _currentLobby = MutableStateFlow<Lobby?>(null)
    val currentLobby: StateFlow<Lobby?> = _currentLobby.asStateFlow()

    private val _gameState = MutableStateFlow<MultiplayerGameState?>(null)
    val gameState: StateFlow<MultiplayerGameState?> = _gameState.asStateFlow()

    private val _events = MutableStateFlow<MultiplayerEvent?>(null)
    val events: StateFlow<MultiplayerEvent?> = _events.asStateFlow()

    private var lobbyObserverJob: Job? = null
    private var gameObserverJob: Job? = null
    private var positionUpdateJob: Job? = null

    /**
     * Start matchmaking for a game mode.
     */
    suspend fun startMatchmaking(gameMode: GameMode, displayName: String) {
        _matchmakingState.value = MatchmakingState.Searching(gameMode, getCurrentTimeMillis())

        try {
            // Join matchmaking queue
            gameRepository.joinMatchmaking(gameMode, displayName)
                .onSuccess {
                    // Start polling for match
                    scope.launch {
                        pollForMatch(gameMode)
                    }
                }
                .onFailure { error ->
                    _matchmakingState.value = MatchmakingState.Error(error.message ?: "Matchmaking failed")
                }
        } catch (e: Exception) {
            _matchmakingState.value = MatchmakingState.Error(e.message ?: "Matchmaking error")
        }
    }

    private suspend fun pollForMatch(gameMode: GameMode) {
        var attempts = 0
        val maxAttempts = 60 // 30 seconds at 500ms intervals

        while (attempts < maxAttempts && _matchmakingState.value is MatchmakingState.Searching) {
            delay(500)
            attempts++

            // Check for available lobbies
            gameRepository.findLobbies(gameMode)
                .onSuccess { lobbies ->
                    val suitableLobby = lobbies.firstOrNull { !it.isFull() }
                    if (suitableLobby != null) {
                        joinLobby(suitableLobby.id, gameMode)
                        return
                    }
                }

            // After 10 seconds, try creating a new lobby
            if (attempts == 20) {
                createLobby(gameMode)
                return
            }
        }

        if (_matchmakingState.value is MatchmakingState.Searching) {
            // Timeout - create a new lobby
            createLobby(gameMode)
        }
    }

    private suspend fun createLobby(gameMode: GameMode) {
        gameRepository.createLobby(gameMode, "Player")
            .onSuccess { lobbyId ->
                _matchmakingState.value = MatchmakingState.Found(lobbyId)
                observeLobby(lobbyId)
            }
            .onFailure { error ->
                _matchmakingState.value = MatchmakingState.Error(error.message ?: "Failed to create lobby")
            }
    }

    private suspend fun joinLobby(lobbyId: String, gameMode: GameMode) {
        gameRepository.joinLobby(lobbyId, "Player")
            .onSuccess {
                _matchmakingState.value = MatchmakingState.Found(lobbyId)
                observeLobby(lobbyId)
            }
            .onFailure {
                // Lobby might be full now, continue polling
            }
    }

    private fun observeLobby(lobbyId: String) {
        lobbyObserverJob?.cancel()
        lobbyObserverJob = scope.launch {
            gameRepository.observeLobby(lobbyId).collect { lobby ->
                _currentLobby.value = lobby

                when (lobby?.getStatus()) {
                    LobbyStatus.IN_GAME -> {
                        // Game started, transition to game observer
                        observeGame(lobbyId)
                    }
                    LobbyStatus.FINISHED -> {
                        // Lobby closed
                        leaveLobby()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observeGame(gameId: String) {
        gameObserverJob?.cancel()
        gameObserverJob = scope.launch {
            _connectionState.value = ConnectionState.Connected

            gameRepository.observeGame(gameId).collect { gameData ->
                gameData?.let { data ->
                    updateGameState(data)
                }
            }
        }

        // Start position updates
        startPositionUpdates(gameId)
    }

    private fun updateGameState(data: MultiplayerGameData) {
        val previousState = _gameState.value

        val players = data.players.map { (id, player) ->
            MultiplayerPlayer(
                odId = id,
                displayName = player.displayName,
                score = player.score,
                rank = player.rank,
                position = player.position(),
                isEliminated = player.isEliminated,
                isCurrentPlayer = id == currentUserId
            )
        }.sortedBy { it.rank }

        val currentPlayer = players.find { it.isCurrentPlayer }
        val currentRank = currentPlayer?.rank ?: players.size

        // Find nearby players (within certain distance)
        val myPosition = currentPlayer?.position ?: Vector3D.ZERO
        val nearbyPlayers = players.filter { player ->
            !player.isCurrentPlayer &&
                    !player.isEliminated &&
                    player.position.distanceTo(myPosition) < 10f // 10 meter radius
        }

        val newState = MultiplayerGameState(
            gameId = data.lobbyId,
            phase = data.getPhase(),
            timeRemaining = data.timeRemaining,
            players = players,
            currentPlayerRank = currentRank,
            eliminationCountdown = data.eliminationCountdown,
            nextEliminationCount = data.nextEliminationCount,
            leadingPlayer = players.firstOrNull(),
            nearbyPlayers = nearbyPlayers
        )

        _gameState.value = newState

        // Emit events
        emitStateChangeEvents(previousState, newState)
    }

    private fun emitStateChangeEvents(previous: MultiplayerGameState?, current: MultiplayerGameState) {
        // Phase change
        if (previous?.phase != current.phase) {
            _events.value = MultiplayerEvent.GamePhaseChanged(current.phase)

            if (current.phase == MultiplayerPhase.FINISHED) {
                val isWinner = current.currentPlayerRank == 1
                _events.value = MultiplayerEvent.GameEnded(current.currentPlayerRank, isWinner)
            }
        }

        // Rank change
        if (previous != null && previous.currentPlayerRank != current.currentPlayerRank) {
            _events.value = MultiplayerEvent.RankChanged(previous.currentPlayerRank, current.currentPlayerRank)
        }

        // Elimination warning
        current.eliminationCountdown?.let { countdown ->
            current.nextEliminationCount?.let { count ->
                _events.value = MultiplayerEvent.EliminationWarning(countdown, count)
            }
        }

        // Nearby players
        if (current.nearbyPlayers.isNotEmpty() && (previous?.nearbyPlayers?.isEmpty() != false)) {
            _events.value = MultiplayerEvent.PlayerNearby
        }

        // Eliminated players
        val previousEliminated = previous?.players?.filter { it.isEliminated }?.map { it.odId } ?: emptyList()
        current.players.filter { it.isEliminated && it.odId !in previousEliminated }.forEach { player ->
            _events.value = MultiplayerEvent.PlayerEliminated(player)
        }
    }

    private fun startPositionUpdates(gameId: String) {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (true) {
                delay(100) // 10 updates per second
                // Position would be updated by the game engine
            }
        }
    }

    /**
     * Update current player's score.
     */
    suspend fun updateScore(score: Int) {
        val gameId = _gameState.value?.gameId ?: return
        gameRepository.updateScore(gameId, score)
    }

    /**
     * Update current player's position.
     */
    suspend fun updatePosition(position: Vector3D) {
        val gameId = _gameState.value?.gameId ?: return
        gameRepository.updatePosition(gameId, position)
    }

    /**
     * Set ready status in lobby.
     */
    suspend fun setReady(ready: Boolean) {
        val lobbyId = _currentLobby.value?.id ?: return
        gameRepository.setReady(lobbyId, ready)
    }

    /**
     * Leave current lobby.
     */
    suspend fun leaveLobby() {
        val lobbyId = _currentLobby.value?.id ?: return
        gameRepository.leaveLobby(lobbyId)
        cleanup()
    }

    /**
     * Cancel matchmaking.
     */
    suspend fun cancelMatchmaking() {
        val searchState = _matchmakingState.value
        if (searchState is MatchmakingState.Searching) {
            gameRepository.leaveMatchmaking(searchState.gameMode)
        }
        _matchmakingState.value = MatchmakingState.Idle
    }

    /**
     * Clean up all observers and state.
     */
    fun cleanup() {
        lobbyObserverJob?.cancel()
        gameObserverJob?.cancel()
        positionUpdateJob?.cancel()

        _connectionState.value = ConnectionState.Disconnected
        _matchmakingState.value = MatchmakingState.Idle
        _currentLobby.value = null
        _gameState.value = null
        _events.value = null
    }
}
