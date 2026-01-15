package com.keren.virtualmoney.progression

import com.keren.virtualmoney.platform.getCurrentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages player progression: XP, levels, achievements, and challenges.
 */
class ProgressionManager(
    private val saveProfile: (PlayerProfile) -> Unit,
    private val loadProfile: () -> PlayerProfile,
    private val saveStats: (PlayerStats) -> Unit,
    private val loadStats: () -> PlayerStats,
    private val saveAchievements: (Set<String>) -> Unit,
    private val loadAchievements: () -> Set<String>,
    private val saveChallengeProgress: (Map<String, Int>) -> Unit,
    private val loadChallengeProgress: () -> Map<String, Int>
) {
    private val _profile = MutableStateFlow(PlayerProfile())
    val profile: StateFlow<PlayerProfile> = _profile.asStateFlow()

    private val _stats = MutableStateFlow(PlayerStats())
    val stats: StateFlow<PlayerStats> = _stats.asStateFlow()

    private val _earnedAchievements = MutableStateFlow<Set<String>>(emptySet())
    val earnedAchievements: StateFlow<Set<String>> = _earnedAchievements.asStateFlow()

    // Alias for easier access
    val achievements: StateFlow<Set<String>> = _earnedAchievements

    private val _challengeProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val challengeProgress: StateFlow<Map<String, Int>> = _challengeProgress.asStateFlow()

    private val _activeChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val activeChallenges: StateFlow<List<Challenge>> = _activeChallenges.asStateFlow()

    // Callbacks for UI notifications
    var onLevelUp: ((oldLevel: Int, newLevel: Int) -> Unit)? = null
    var onAchievementUnlocked: ((AchievementId) -> Unit)? = null
    var onChallengeCompleted: ((Challenge) -> Unit)? = null

    /**
     * Initialize manager and load saved data.
     */
    fun initialize() {
        _profile.value = loadProfile()
        _stats.value = loadStats()
        _earnedAchievements.value = loadAchievements()
        _challengeProgress.value = loadChallengeProgress()
    }

    /**
     * Process a game result and update all progression (simple version).
     */
    fun processGameResult(result: GameResult) {
        processGameResult(result, _activeChallenges.value)
    }

    /**
     * Process a game result and update all progression.
     * @return List of newly unlocked achievements
     */
    fun processGameResult(
        result: GameResult,
        challenges: List<Challenge>
    ): GameProgressionResult {
        val oldProfile = _profile.value
        val oldStats = _stats.value
        val oldAchievements = _earnedAchievements.value

        // Update stats
        val newStats = updateStats(result)
        _stats.value = newStats
        saveStats(newStats)

        // Calculate XP
        val beatHighScore = result.score > oldStats.highScore
        val xpBreakdown = LevelSystem.calculateXpReward(result, beatHighScore, oldProfile.prestige)

        // Update profile with XP
        val newXp = oldProfile.xp + xpBreakdown.totalXp
        val newLevel = LevelSystem.levelForXp(newXp)
        val leveledUp = newLevel > oldProfile.level

        val newProfile = oldProfile.copy(
            xp = newXp,
            level = newLevel,
            lastLogin = getCurrentTimeMillis()
        )
        _profile.value = newProfile
        saveProfile(newProfile)

        if (leveledUp) {
            onLevelUp?.invoke(oldProfile.level, newLevel)
        }

        // Check achievements
        val newlyUnlocked = checkAchievements(newStats, newProfile)
        if (newlyUnlocked.isNotEmpty()) {
            val updatedAchievements = oldAchievements + newlyUnlocked.map { it.name }
            _earnedAchievements.value = updatedAchievements
            saveAchievements(updatedAchievements)

            newlyUnlocked.forEach { achievement ->
                onAchievementUnlocked?.invoke(achievement)
            }
        }

        // Update challenge progress
        val completedChallenges = updateChallengeProgress(result, challenges)

        return GameProgressionResult(
            xpEarned = xpBreakdown,
            leveledUp = leveledUp,
            oldLevel = oldProfile.level,
            newLevel = newLevel,
            newlyUnlockedAchievements = newlyUnlocked,
            completedChallenges = completedChallenges,
            newHighScore = beatHighScore
        )
    }

    /**
     * Update player stats with game result.
     */
    private fun updateStats(result: GameResult): PlayerStats {
        val old = _stats.value
        return old.copy(
            gamesPlayed = old.gamesPlayed + 1,
            totalCoins = old.totalCoins + result.coinsCollected,
            highScore = maxOf(old.highScore, result.score),
            bestCombo = maxOf(old.bestCombo, result.bestCombo),
            perfectRuns = if (result.wasPerfectRun) old.perfectRuns + 1 else old.perfectRuns,
            totalPlayTime = old.totalPlayTime + result.playTimeMs,
            powerUpsCollected = old.powerUpsCollected + result.powerUpsCollected,
            multiplayerWins = if (result.isMultiplayer && result.multiplayerRank == 1)
                old.multiplayerWins + 1 else old.multiplayerWins,
            multiplayerTop10 = if (result.isMultiplayer &&
                result.multiplayerRank != null &&
                result.totalPlayers != null &&
                result.multiplayerRank <= (result.totalPlayers * 0.1).toInt().coerceAtLeast(1))
                old.multiplayerTop10 + 1 else old.multiplayerTop10,
            multiplayerGamesPlayed = if (result.isMultiplayer)
                old.multiplayerGamesPlayed + 1 else old.multiplayerGamesPlayed,
            battleRoyaleWins = if (result.wasBattleRoyale && result.multiplayerRank == 1)
                old.battleRoyaleWins + 1 else old.battleRoyaleWins
        )
    }

    /**
     * Check and return newly unlocked achievements.
     */
    private fun checkAchievements(stats: PlayerStats, profile: PlayerProfile): List<AchievementId> {
        val earned = _earnedAchievements.value
        val newlyUnlocked = mutableListOf<AchievementId>()

        for (achievement in Achievement.all()) {
            if (earned.contains(achievement.id.name)) continue

            val isUnlocked = when (val req = achievement.requirement) {
                is AchievementRequirement.GamesPlayed -> stats.gamesPlayed >= req.count
                is AchievementRequirement.TotalCoins -> stats.totalCoins >= req.count
                is AchievementRequirement.ScoreInGame -> stats.highScore >= req.score
                is AchievementRequirement.ComboReached -> stats.bestCombo >= req.combo
                is AchievementRequirement.PerfectRuns -> stats.perfectRuns >= req.count
                is AchievementRequirement.PowerUpsCollected -> stats.powerUpsCollected >= req.count
                is AchievementRequirement.MultiplayerWins -> stats.multiplayerWins >= req.count
                is AchievementRequirement.MultiplayerTop10 -> stats.multiplayerTop10 >= req.count
                is AchievementRequirement.BattleRoyaleWins -> stats.battleRoyaleWins >= req.count
                is AchievementRequirement.ReachLevel -> profile.level >= req.level
                is AchievementRequirement.ReachPrestige -> profile.prestige >= req.prestige
                is AchievementRequirement.SpecificPowerUp -> false // Tracked separately
                is AchievementRequirement.LeaderboardRank -> false // Tracked from server
            }

            if (isUnlocked) {
                newlyUnlocked.add(achievement.id)
            }
        }

        return newlyUnlocked
    }

    /**
     * Update challenge progress and return completed challenges.
     */
    private fun updateChallengeProgress(
        result: GameResult,
        challenges: List<Challenge>
    ): List<Challenge> {
        val currentProgress = _challengeProgress.value.toMutableMap()
        val completed = mutableListOf<Challenge>()

        for (challenge in challenges) {
            if (challenge.isExpired()) continue

            val oldProgress = currentProgress[challenge.id] ?: 0
            if (oldProgress >= challenge.target) continue // Already completed

            val increment = when (challenge.type) {
                ChallengeType.COLLECT_COINS -> result.coinsCollected
                ChallengeType.COLLECT_POWERUPS -> result.powerUpsCollected
                ChallengeType.REACH_SCORE -> if (result.score >= challenge.target) 1 else 0
                ChallengeType.BEAT_SCORE -> if (result.score >= challenge.target) 1 else 0
                ChallengeType.REACH_COMBO -> if (result.bestCombo >= challenge.target) 1 else 0
                ChallengeType.MAINTAIN_COMBO -> if (result.bestCombo >= challenge.target) 1 else 0
                ChallengeType.PERFECT_RUN -> if (result.wasPerfectRun) 1 else 0
                ChallengeType.PERFECT_RUNS_COUNT -> if (result.wasPerfectRun) 1 else 0
                ChallengeType.PLAY_GAMES -> 1
                ChallengeType.WIN_GAMES -> if (result.isMultiplayer && result.multiplayerRank == 1) 1 else 0
                ChallengeType.NO_POWERUPS -> if (result.powerUpsCollected == 0) 1 else 0
                ChallengeType.SPEED_RUN -> 0 // Special handling needed
                ChallengeType.MULTIPLAYER_WIN -> if (result.isMultiplayer && result.multiplayerRank == 1) 1 else 0
            }

            if (increment > 0) {
                val newProgress = oldProgress + increment
                currentProgress[challenge.id] = newProgress

                if (newProgress >= challenge.target && oldProgress < challenge.target) {
                    completed.add(challenge)
                    onChallengeCompleted?.invoke(challenge)
                }
            }
        }

        _challengeProgress.value = currentProgress
        saveChallengeProgress(currentProgress)

        return completed
    }

    /**
     * Get progress for a specific challenge.
     */
    fun getProgress(challengeId: String): Int =
        _challengeProgress.value[challengeId] ?: 0

    /**
     * Check if player can prestige.
     */
    fun canPrestige(): Boolean =
        LevelSystem.canPrestige(_profile.value.level)

    /**
     * Perform prestige if eligible.
     */
    fun prestige(): Boolean {
        if (!canPrestige()) return false

        val newProfile = LevelSystem.prestige(_profile.value)
        _profile.value = newProfile
        saveProfile(newProfile)

        // Check prestige achievement
        if (!_earnedAchievements.value.contains(AchievementId.PRESTIGE.name)) {
            val updated = _earnedAchievements.value + AchievementId.PRESTIGE.name
            _earnedAchievements.value = updated
            saveAchievements(updated)
            onAchievementUnlocked?.invoke(AchievementId.PRESTIGE)
        }

        return true
    }

    /**
     * Update display name.
     */
    fun updateDisplayName(name: String) {
        val newProfile = _profile.value.copy(displayName = name)
        _profile.value = newProfile
        saveProfile(newProfile)
    }
}

/**
 * Result of processing a game for progression.
 */
data class GameProgressionResult(
    val xpEarned: XpRewardBreakdown,
    val leveledUp: Boolean,
    val oldLevel: Int,
    val newLevel: Int,
    val newlyUnlockedAchievements: List<AchievementId>,
    val completedChallenges: List<Challenge>,
    val newHighScore: Boolean
)
