package com.keren.virtualmoney.backend

import com.keren.virtualmoney.ar.math.Vector3D
import com.keren.virtualmoney.game.GameMode
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.DataSnapshot
import dev.gitlive.firebase.database.database
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.keren.virtualmoney.platform.getCurrentTimeMillis
import kotlinx.serialization.Serializable

/**
 * Lobby status.
 */
enum class LobbyStatus {
    WAITING,
    COUNTDOWN,
    STARTING,
    IN_GAME,
    FINISHED
}

/**
 * Game phase for multiplayer.
 */
enum class MultiplayerPhase {
    COUNTDOWN,
    PLAYING,
    ELIMINATION_WARNING,
    ELIMINATION,
    FINAL_SHOWDOWN,
    FINISHED
}

/**
 * Player in a lobby.
 */
@Serializable
data class LobbyPlayer(
    val odId: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val level: Int = 1,
    val ready: Boolean = false,
    val teamId: Int? = null,
    val joinedAt: Long = 0
)

/**
 * Lobby data.
 */
@Serializable
data class Lobby(
    val id: String = "",
    val gameMode: String = GameMode.QUICK_MATCH.name,
    val maxPlayers: Int = 10,
    val status: String = LobbyStatus.WAITING.name,
    val hostId: String = "",
    val createdAt: Long = 0,
    val startTime: Long? = null,
    val players: Map<String, LobbyPlayer> = emptyMap()
) {
    fun getGameMode(): GameMode = try {
        GameMode.valueOf(gameMode)
    } catch (e: Exception) {
        GameMode.QUICK_MATCH
    }

    fun getStatus(): LobbyStatus = try {
        LobbyStatus.valueOf(status)
    } catch (e: Exception) {
        LobbyStatus.WAITING
    }

    fun playerCount(): Int = players.size
    fun isFull(): Boolean = players.size >= maxPlayers
}

/**
 * Player state in a game.
 */
@Serializable
data class GamePlayer(
    val displayName: String = "",
    val score: Int = 0,
    val rank: Int = 0,
    val isEliminated: Boolean = false,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val lastUpdate: Long = 0
) {
    fun position(): Vector3D = Vector3D(x, y, z)
}

/**
 * Multiplayer game state.
 */
@Serializable
data class MultiplayerGameData(
    val lobbyId: String = "",
    val gameMode: String = "",
    val phase: String = MultiplayerPhase.COUNTDOWN.name,
    val startTime: Long = 0,
    val timeRemaining: Int = 60,
    val eliminationCountdown: Int? = null,
    val nextEliminationCount: Int? = null,
    val players: Map<String, GamePlayer> = emptyMap()
) {
    fun getPhase(): MultiplayerPhase = try {
        MultiplayerPhase.valueOf(phase)
    } catch (e: Exception) {
        MultiplayerPhase.PLAYING
    }
}

/**
 * Repository for multiplayer game state using Firebase Realtime Database.
 */
class GameRepository(
    private val authManager: AuthManager
) {
    private val database = Firebase.database

    private fun lobbiesRef() = database.reference("lobbies")
    private fun gamesRef() = database.reference("games")
    private fun matchmakingRef() = database.reference("matchmaking/queues")
    private fun presenceRef() = database.reference("presence")

    // ==================== Lobbies ====================

    /**
     * Create a new lobby.
     */
    suspend fun createLobby(gameMode: GameMode, displayName: String): Result<String> {
        return try {
            val userId = authManager.getUserId() ?: throw Exception("Not signed in")
            val now = getCurrentTimeMillis()

            val lobbyRef = lobbiesRef().push()
            val lobbyId = lobbyRef.key ?: throw Exception("Failed to create lobby")

            val player = LobbyPlayer(
                odId = userId,
                displayName = displayName,
                ready = true,
                joinedAt = now
            )

            val lobby = Lobby(
                id = lobbyId,
                gameMode = gameMode.name,
                maxPlayers = gameMode.maxPlayers,
                status = LobbyStatus.WAITING.name,
                hostId = userId,
                createdAt = now,
                players = mapOf(userId to player)
            )

            lobbyRef.setValue(lobby)
            Result.success(lobbyId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Join an existing lobby.
     */
    suspend fun joinLobby(lobbyId: String, displayName: String): Result<Unit> {
        return try {
            val userId = authManager.getUserId() ?: throw Exception("Not signed in")
            val now = getCurrentTimeMillis()

            val player = LobbyPlayer(
                odId = userId,
                displayName = displayName,
                ready = false,
                joinedAt = now
            )

            lobbiesRef().child(lobbyId).child("players").child(userId).setValue(player)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Leave a lobby.
     */
    suspend fun leaveLobby(lobbyId: String): Result<Unit> {
        return try {
            val userId = authManager.getUserId() ?: throw Exception("Not signed in")
            lobbiesRef().child(lobbyId).child("players").child(userId).removeValue()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Set player ready status.
     */
    suspend fun setReady(lobbyId: String, ready: Boolean): Result<Unit> {
        return try {
            val userId = authManager.getUserId() ?: throw Exception("Not signed in")
            lobbiesRef().child(lobbyId).child("players").child(userId).child("ready").setValue(ready)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observe lobby state.
     */
    fun observeLobby(lobbyId: String): Flow<Lobby?> {
        return lobbiesRef().child(lobbyId).valueEvents.map { snapshot ->
            try {
                snapshot.value<Lobby>()
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Find available lobbies for a game mode.
     */
    suspend fun findLobbies(gameMode: GameMode): Result<List<Lobby>> {
        return try {
            val snapshot = lobbiesRef().valueEvents.first()

            val lobbies = mutableListOf<Lobby>()
            snapshot.children.forEach { child: DataSnapshot ->
                try {
                    val lobby = child.value<Lobby>()
                    if (lobby.getGameMode() == gameMode &&
                        lobby.getStatus() == LobbyStatus.WAITING &&
                        !lobby.isFull()) {
                        lobbies.add(lobby)
                    }
                } catch (e: Exception) {
                    // Skip invalid entries
                }
            }

            Result.success(lobbies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Matchmaking ====================

    /**
     * Join matchmaking queue.
     */
    suspend fun joinMatchmaking(gameMode: GameMode, displayName: String): Result<Unit> {
        return try {
            val userId = authManager.getUserId() ?: throw Exception("Not signed in")
            val now = getCurrentTimeMillis()

            val queueEntry = mapOf(
                "odId" to userId,
                "displayName" to displayName,
                "joinedAt" to now
            )

            matchmakingRef().child(gameMode.name).child(userId).setValue(queueEntry)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Leave matchmaking queue.
     */
    suspend fun leaveMatchmaking(gameMode: GameMode): Result<Unit> {
        return try {
            val userId = authManager.getUserId() ?: throw Exception("Not signed in")
            matchmakingRef().child(gameMode.name).child(userId).removeValue()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Game State ====================

    /**
     * Observe multiplayer game state.
     */
    fun observeGame(gameId: String): Flow<MultiplayerGameData?> {
        return gamesRef().child(gameId).valueEvents.map { snapshot ->
            try {
                snapshot.value<MultiplayerGameData>()
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Update player position in game.
     */
    suspend fun updatePosition(gameId: String, position: Vector3D): Result<Unit> {
        return try {
            val userId = authManager.getUserId() ?: throw Exception("Not signed in")
            val now = getCurrentTimeMillis()

            val updates = mapOf(
                "x" to position.x,
                "y" to position.y,
                "z" to position.z,
                "lastUpdate" to now
            )

            gamesRef().child(gameId).child("players").child(userId).updateChildren(updates)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update player score in game.
     */
    suspend fun updateScore(gameId: String, score: Int): Result<Unit> {
        return try {
            val userId = authManager.getUserId() ?: throw Exception("Not signed in")
            gamesRef().child(gameId).child("players").child(userId).child("score").setValue(score)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Presence ====================

    /**
     * Set user online presence.
     */
    suspend fun setOnline(inLobby: String? = null, inGame: String? = null): Result<Unit> {
        return try {
            val userId = authManager.getUserId() ?: throw Exception("Not signed in")
            val now = getCurrentTimeMillis()

            val presence = mapOf(
                "online" to true,
                "lastSeen" to now,
                "inLobby" to inLobby,
                "inGame" to inGame
            )

            presenceRef().child(userId).setValue(presence)

            // Set up disconnect handler
            presenceRef().child(userId).child("online").onDisconnect().setValue(false)
            presenceRef().child(userId).child("lastSeen").onDisconnect().setValue(now)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Set user offline.
     */
    suspend fun setOffline(): Result<Unit> {
        return try {
            val userId = authManager.getUserId() ?: throw Exception("Not signed in")
            val now = getCurrentTimeMillis()

            presenceRef().child(userId).updateChildren(
                mapOf(
                    "online" to false,
                    "lastSeen" to now,
                    "inLobby" to null,
                    "inGame" to null
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
