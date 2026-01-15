package com.keren.virtualmoney.game

import com.keren.virtualmoney.audio.GameSound
import com.keren.virtualmoney.audio.HapticType
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
 * Configuration for Team Battle mode.
 */
data class TeamBattleConfig(
    val gameDurationSeconds: Int = 180,        // 3 minute game
    val teamCount: Int = 2,                    // Number of teams
    val teamNames: List<String> = listOf("Red Team", "Blue Team"),
    val teamColors: List<Long> = listOf(0xFFE53935, 0xFF2196F3),
    val territoryEnabled: Boolean = true,      // Enable territory capture
    val territoryCaptureTime: Int = 10,        // Seconds to capture a territory
    val territoryBonus: Int = 50,              // Bonus points per territory owned
    val territoryBonusInterval: Int = 10,      // Award bonus every 10 seconds
    val coinSharePercentage: Float = 0.25f,    // 25% of personal coins shared with team
    val assistBonusPercentage: Float = 0.1f    // 10% bonus when teammate collects near you
)

/**
 * Represents a team in Team Battle.
 */
data class Team(
    val id: Int,
    val name: String,
    val color: Long,
    val members: List<String> = emptyList(),
    val totalScore: Int = 0,
    val territoriesOwned: Int = 0,
    val coinsCollected: Int = 0
)

/**
 * Team member data.
 */
data class TeamMember(
    val playerId: String,
    val displayName: String,
    val teamId: Int,
    var personalScore: Int = 0,
    var teamContribution: Int = 0,
    var coinsCollected: Int = 0
)

/**
 * Territory for capture.
 */
data class Territory(
    val id: String,
    val name: String,
    val owningTeamId: Int? = null,
    val captureProgress: Map<Int, Float> = emptyMap(), // teamId to progress (0-1)
    val position: TerritoryPosition
)

data class TerritoryPosition(
    val x: Float,
    val y: Float,
    val radius: Float = 5f // meters
)

/**
 * Team Battle state.
 */
data class TeamBattleState(
    val phase: TeamBattlePhase,
    val timeRemaining: Int,
    val teams: List<Team>,
    val territories: List<Territory>,
    val myTeamId: Int,
    val leadingTeam: Team?,
    val scoreDifference: Int
)

enum class TeamBattlePhase {
    SETUP,
    PLAYING,
    OVERTIME,   // Tied at end - sudden death
    FINISHED
}

/**
 * Events during Team Battle.
 */
sealed class TeamBattleEvent {
    data class TeammateCollectedCoin(val memberName: String, val points: Int) : TeamBattleEvent()
    data class TerritoryCaptured(val territory: Territory, val byTeam: Team) : TeamBattleEvent()
    data class TerritoryLost(val territory: Territory, val toTeam: Team) : TeamBattleEvent()
    data class TeamScoreUpdate(val team: Team, val newScore: Int) : TeamBattleEvent()
    data class LeadChanged(val newLeader: Team, val byAmount: Int) : TeamBattleEvent()
    data object OvertimeStarted : TeamBattleEvent()
    data class GameEnded(val winningTeam: Team, val didMyTeamWin: Boolean) : TeamBattleEvent()
}

/**
 * Manages Team Battle mode mechanics.
 */
class TeamBattleManager(
    private val scope: CoroutineScope,
    private val config: TeamBattleConfig = TeamBattleConfig(),
    private val currentPlayerId: String,
    private val onPlaySound: (GameSound) -> Unit = {},
    private val onHaptic: (HapticType) -> Unit = {},
    private val onScoreSync: suspend (teamId: Int, score: Int) -> Unit = { _, _ -> }
) {
    private val _state = MutableStateFlow(
        TeamBattleState(
            phase = TeamBattlePhase.SETUP,
            timeRemaining = config.gameDurationSeconds,
            teams = emptyList(),
            territories = emptyList(),
            myTeamId = 0,
            leadingTeam = null,
            scoreDifference = 0
        )
    )
    val state: StateFlow<TeamBattleState> = _state.asStateFlow()

    private val _events = MutableStateFlow<TeamBattleEvent?>(null)
    val events: StateFlow<TeamBattleEvent?> = _events.asStateFlow()

    private var gameLoopJob: Job? = null
    private var territoryJob: Job? = null
    private var syncJob: Job? = null

    private val teamMembers = mutableMapOf<String, TeamMember>()
    private var gameStartTime: Long = 0
    private var lastLeadingTeamId: Int? = null

    /**
     * Initialize teams and players.
     */
    fun initialize(players: List<MultiplayerPlayer>) {
        // Divide players into teams
        val shuffledPlayers = players.shuffled()
        val teamAssignments = mutableMapOf<Int, MutableList<String>>()

        for (i in 0 until config.teamCount) {
            teamAssignments[i] = mutableListOf()
        }

        shuffledPlayers.forEachIndexed { index, player ->
            val teamId = index % config.teamCount
            teamAssignments[teamId]?.add(player.odId)

            teamMembers[player.odId] = TeamMember(
                playerId = player.odId,
                displayName = player.displayName,
                teamId = teamId
            )
        }

        val teams = (0 until config.teamCount).map { teamId ->
            Team(
                id = teamId,
                name = config.teamNames.getOrElse(teamId) { "Team ${teamId + 1}" },
                color = config.teamColors.getOrElse(teamId) { 0xFFFFFFFF },
                members = teamAssignments[teamId] ?: emptyList()
            )
        }

        val myTeamId = teamMembers[currentPlayerId]?.teamId ?: 0

        // Create territories if enabled
        val territories = if (config.territoryEnabled) {
            createTerritories()
        } else {
            emptyList()
        }

        _state.value = TeamBattleState(
            phase = TeamBattlePhase.SETUP,
            timeRemaining = config.gameDurationSeconds,
            teams = teams,
            territories = territories,
            myTeamId = myTeamId,
            leadingTeam = null,
            scoreDifference = 0
        )
    }

    private fun createTerritories(): List<Territory> {
        // Create a few territories around the play area
        return listOf(
            Territory(
                id = "territory_center",
                name = "Central Zone",
                position = TerritoryPosition(0f, 0f, 5f)
            ),
            Territory(
                id = "territory_north",
                name = "North Zone",
                position = TerritoryPosition(0f, 20f, 4f)
            ),
            Territory(
                id = "territory_south",
                name = "South Zone",
                position = TerritoryPosition(0f, -20f, 4f)
            ),
            Territory(
                id = "territory_east",
                name = "East Zone",
                position = TerritoryPosition(20f, 0f, 4f)
            ),
            Territory(
                id = "territory_west",
                name = "West Zone",
                position = TerritoryPosition(-20f, 0f, 4f)
            )
        )
    }

    /**
     * Start the Team Battle game.
     */
    fun startGame() {
        gameStartTime = getCurrentTimeMillis()

        _state.value = _state.value.copy(
            phase = TeamBattlePhase.PLAYING
        )

        onPlaySound(GameSound.GAME_START)
        onHaptic(HapticType.MEDIUM)

        startGameLoop()

        if (config.territoryEnabled) {
            startTerritoryLoop()
        }
    }

    /**
     * Player collected a coin.
     */
    fun onCoinCollected(playerId: String, points: Int) {
        val member = teamMembers[playerId] ?: return
        val teamId = member.teamId

        // Personal score
        member.personalScore += points
        member.coinsCollected++

        // Team contribution (shared percentage)
        val teamBonus = (points * config.coinSharePercentage).toInt()
        member.teamContribution += teamBonus

        // Update team score
        updateTeamScore(teamId, points + teamBonus)

        // Notify teammates
        if (playerId != currentPlayerId && member.teamId == teamMembers[currentPlayerId]?.teamId) {
            _events.value = TeamBattleEvent.TeammateCollectedCoin(member.displayName, points)
        }

        // Check for assist bonus (if teammate nearby)
        val myTeamId = teamMembers[currentPlayerId]?.teamId
        if (playerId != currentPlayerId && member.teamId == myTeamId) {
            // In real implementation, would check distance
            val assistBonus = (points * config.assistBonusPercentage).toInt()
            if (assistBonus > 0) {
                teamMembers[currentPlayerId]?.teamContribution?.plus(assistBonus)
                updateTeamScore(myTeamId ?: 0, assistBonus)
            }
        }
    }

    private fun updateTeamScore(teamId: Int, pointsToAdd: Int) {
        val currentState = _state.value
        val updatedTeams = currentState.teams.map { team ->
            if (team.id == teamId) {
                team.copy(totalScore = team.totalScore + pointsToAdd)
            } else {
                team
            }
        }

        // Determine leading team
        val sortedTeams = updatedTeams.sortedByDescending { it.totalScore }
        val leadingTeam = sortedTeams.firstOrNull()
        val scoreDifference = if (sortedTeams.size >= 2) {
            sortedTeams[0].totalScore - sortedTeams[1].totalScore
        } else {
            0
        }

        // Check for lead change
        if (leadingTeam != null && leadingTeam.id != lastLeadingTeamId) {
            lastLeadingTeamId = leadingTeam.id
            _events.value = TeamBattleEvent.LeadChanged(leadingTeam, scoreDifference)

            if (leadingTeam.id == currentState.myTeamId) {
                onPlaySound(GameSound.COMBO_MILESTONE)
                onHaptic(HapticType.SUCCESS)
            } else {
                onHaptic(HapticType.WARNING)
            }
        }

        _state.value = currentState.copy(
            teams = updatedTeams,
            leadingTeam = leadingTeam,
            scoreDifference = scoreDifference
        )

        // Sync to server
        scope.launch {
            updatedTeams.find { it.id == teamId }?.let { team ->
                onScoreSync(teamId, team.totalScore)
            }
        }
    }

    /**
     * Update territory capture progress.
     */
    fun updateTerritoryProgress(territoryId: String, teamId: Int, progress: Float) {
        val currentState = _state.value
        val updatedTerritories = currentState.territories.map { territory ->
            if (territory.id == territoryId) {
                val newProgress = territory.captureProgress.toMutableMap()
                newProgress[teamId] = progress.coerceIn(0f, 1f)

                // Check if captured
                if (progress >= 1f && territory.owningTeamId != teamId) {
                    val capturingTeam = currentState.teams.find { it.id == teamId }

                    // Emit capture event
                    capturingTeam?.let { team ->
                        _events.value = TeamBattleEvent.TerritoryCaptured(territory, team)
                        if (teamId == currentState.myTeamId) {
                            onPlaySound(GameSound.POWERUP_COLLECT)
                            onHaptic(HapticType.SUCCESS)
                        }
                    }

                    // Emit lost event for previous owner
                    territory.owningTeamId?.let { previousOwnerId ->
                        val lostTeam = currentState.teams.find { it.id == previousOwnerId }
                        lostTeam?.let {
                            if (previousOwnerId == currentState.myTeamId) {
                                _events.value = TeamBattleEvent.TerritoryLost(territory, capturingTeam!!)
                                onHaptic(HapticType.ERROR)
                            }
                        }
                    }

                    territory.copy(
                        owningTeamId = teamId,
                        captureProgress = mapOf(teamId to 1f)
                    )
                } else {
                    territory.copy(captureProgress = newProgress)
                }
            } else {
                territory
            }
        }

        // Update team territory counts
        val updatedTeams = currentState.teams.map { team ->
            val territoryCount = updatedTerritories.count { it.owningTeamId == team.id }
            team.copy(territoriesOwned = territoryCount)
        }

        _state.value = currentState.copy(
            territories = updatedTerritories,
            teams = updatedTeams
        )
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = scope.launch {
            while (true) {
                delay(1000)

                val currentState = _state.value
                val elapsedSeconds = ((getCurrentTimeMillis() - gameStartTime) / 1000).toInt()
                val timeRemaining = (config.gameDurationSeconds - elapsedSeconds).coerceAtLeast(0)

                if (timeRemaining <= 0) {
                    // Check for tie
                    val teams = currentState.teams.sortedByDescending { it.totalScore }
                    if (teams.size >= 2 && teams[0].totalScore == teams[1].totalScore) {
                        // Overtime!
                        startOvertime()
                    } else {
                        endGame()
                    }
                    break
                }

                _state.value = currentState.copy(timeRemaining = timeRemaining)

                // Warning at 30 seconds
                if (timeRemaining == 30) {
                    onPlaySound(GameSound.COUNTDOWN_TICK)
                }

                // Countdown in last 10 seconds
                if (timeRemaining <= 10) {
                    onPlaySound(GameSound.COUNTDOWN_TICK)
                }
            }
        }
    }

    private fun startTerritoryLoop() {
        territoryJob?.cancel()
        territoryJob = scope.launch {
            while (true) {
                delay((config.territoryBonusInterval * 1000).toLong())

                val currentState = _state.value
                if (currentState.phase != TeamBattlePhase.PLAYING &&
                    currentState.phase != TeamBattlePhase.OVERTIME
                ) break

                // Award territory bonuses
                currentState.teams.forEach { team ->
                    if (team.territoriesOwned > 0) {
                        val bonus = team.territoriesOwned * config.territoryBonus
                        updateTeamScore(team.id, bonus)
                    }
                }
            }
        }
    }

    private fun startOvertime() {
        _state.value = _state.value.copy(
            phase = TeamBattlePhase.OVERTIME,
            timeRemaining = 60 // 60 second overtime
        )

        _events.value = TeamBattleEvent.OvertimeStarted
        onPlaySound(GameSound.COMBO_MILESTONE)
        onHaptic(HapticType.WARNING)

        // Overtime loop - first to score wins
        scope.launch {
            var overtime = 60
            val startingScores = _state.value.teams.associate { it.id to it.totalScore }

            while (overtime > 0) {
                delay(1000)
                overtime--
                _state.value = _state.value.copy(timeRemaining = overtime)

                // Check if someone pulled ahead
                val currentTeams = _state.value.teams
                val changed = currentTeams.any { team ->
                    team.totalScore > (startingScores[team.id] ?: 0)
                }

                if (changed) {
                    // Someone scored, they win!
                    endGame()
                    return@launch
                }
            }

            // Overtime ended with no change - draw or use tiebreaker
            endGame()
        }
    }

    private fun endGame() {
        gameLoopJob?.cancel()
        territoryJob?.cancel()

        val teams = _state.value.teams.sortedByDescending { it.totalScore }
        val winningTeam = teams.firstOrNull()

        _state.value = _state.value.copy(phase = TeamBattlePhase.FINISHED)

        winningTeam?.let { winner ->
            val didMyTeamWin = winner.id == _state.value.myTeamId
            _events.value = TeamBattleEvent.GameEnded(winner, didMyTeamWin)

            if (didMyTeamWin) {
                onPlaySound(GameSound.NEW_HIGH_SCORE)
                onHaptic(HapticType.SUCCESS)
            } else {
                onPlaySound(GameSound.GAME_END)
                onHaptic(HapticType.MEDIUM)
            }
        }
    }

    /**
     * Get current player's team.
     */
    fun getMyTeam(): Team? {
        return _state.value.teams.find { it.id == _state.value.myTeamId }
    }

    /**
     * Get player's personal stats.
     */
    fun getMyStats(): TeamMember? {
        return teamMembers[currentPlayerId]
    }

    /**
     * Get teammates.
     */
    fun getTeammates(): List<TeamMember> {
        val myTeamId = teamMembers[currentPlayerId]?.teamId ?: return emptyList()
        return teamMembers.values.filter {
            it.teamId == myTeamId && it.playerId != currentPlayerId
        }
    }

    /**
     * Cleanup resources.
     */
    fun cleanup() {
        gameLoopJob?.cancel()
        territoryJob?.cancel()
        syncJob?.cancel()
        teamMembers.clear()
        _state.value = TeamBattleState(
            phase = TeamBattlePhase.SETUP,
            timeRemaining = config.gameDurationSeconds,
            teams = emptyList(),
            territories = emptyList(),
            myTeamId = 0,
            leadingTeam = null,
            scoreDifference = 0
        )
    }
}
