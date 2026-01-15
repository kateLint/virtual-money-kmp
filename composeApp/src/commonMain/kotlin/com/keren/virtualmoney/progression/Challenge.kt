package com.keren.virtualmoney.progression

import com.keren.virtualmoney.platform.getCurrentTimeMillis

/**
 * Challenge types.
 */
enum class ChallengeType {
    // Collection
    COLLECT_COINS,
    COLLECT_POWERUPS,

    // Score
    REACH_SCORE,
    BEAT_SCORE,

    // Combo
    REACH_COMBO,
    MAINTAIN_COMBO,

    // Perfect
    PERFECT_RUN,
    PERFECT_RUNS_COUNT,

    // Games
    PLAY_GAMES,
    WIN_GAMES,

    // Special
    NO_POWERUPS,
    SPEED_RUN,
    MULTIPLAYER_WIN
}

/**
 * Challenge difficulty affects rewards.
 */
enum class ChallengeDifficulty(val xpMultiplier: Float) {
    EASY(1.0f),
    MEDIUM(1.5f),
    HARD(2.0f),
    EXTREME(3.0f)
}

/**
 * A daily or weekly challenge.
 */
data class Challenge(
    val id: String,
    val type: ChallengeType,
    val title: String,
    val description: String,
    val target: Int,
    val difficulty: ChallengeDifficulty,
    val xpReward: Int,
    val skinReward: String? = null,
    val themeReward: String? = null,
    val expiresAt: Long,
    val isDaily: Boolean = true
) {
    /**
     * Check if challenge is expired.
     */
    fun isExpired(): Boolean = getCurrentTimeMillis() >= expiresAt

    /**
     * Time remaining until expiration.
     */
    fun timeRemaining(): Long = maxOf(0, expiresAt - getCurrentTimeMillis())

    /**
     * Format time remaining as readable string.
     */
    fun formattedTimeRemaining(): String {
        val remaining = timeRemaining()
        val hours = remaining / (1000 * 60 * 60)
        val minutes = (remaining % (1000 * 60 * 60)) / (1000 * 60)

        return when {
            hours > 24 -> "${hours / 24}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "<1m"
        }
    }

    companion object {
        /**
         * Generate daily challenges.
         */
        fun generateDailyChallenges(expiresAt: Long): List<Challenge> = listOf(
            Challenge(
                id = "daily_coins_50",
                type = ChallengeType.COLLECT_COINS,
                title = "Coin Collector",
                description = "Collect 50 coins",
                target = 50,
                difficulty = ChallengeDifficulty.EASY,
                xpReward = 50,
                expiresAt = expiresAt
            ),
            Challenge(
                id = "daily_perfect",
                type = ChallengeType.PERFECT_RUN,
                title = "Perfect Run",
                description = "Complete a game without penalties",
                target = 1,
                difficulty = ChallengeDifficulty.MEDIUM,
                xpReward = 100,
                expiresAt = expiresAt
            ),
            Challenge(
                id = "daily_combo_5",
                type = ChallengeType.REACH_COMBO,
                title = "Combo Time",
                description = "Get a 5x combo",
                target = 5,
                difficulty = ChallengeDifficulty.EASY,
                xpReward = 50,
                expiresAt = expiresAt
            ),
            Challenge(
                id = "daily_score_200",
                type = ChallengeType.REACH_SCORE,
                title = "High Scorer",
                description = "Score 200+ in a single game",
                target = 200,
                difficulty = ChallengeDifficulty.MEDIUM,
                xpReward = 75,
                expiresAt = expiresAt
            )
        )

        /**
         * Generate weekly challenges.
         */
        fun generateWeeklyChallenges(expiresAt: Long): List<Challenge> = listOf(
            Challenge(
                id = "weekly_games_30",
                type = ChallengeType.PLAY_GAMES,
                title = "Marathon",
                description = "Play 30 games this week",
                target = 30,
                difficulty = ChallengeDifficulty.MEDIUM,
                xpReward = 300,
                expiresAt = expiresAt,
                isDaily = false
            ),
            Challenge(
                id = "weekly_coins_500",
                type = ChallengeType.COLLECT_COINS,
                title = "Treasure Hunter",
                description = "Collect 500 coins",
                target = 500,
                difficulty = ChallengeDifficulty.MEDIUM,
                xpReward = 350,
                expiresAt = expiresAt,
                isDaily = false
            ),
            Challenge(
                id = "weekly_perfect_5",
                type = ChallengeType.PERFECT_RUNS_COUNT,
                title = "Perfectionist",
                description = "Complete 5 perfect runs",
                target = 5,
                difficulty = ChallengeDifficulty.HARD,
                xpReward = 400,
                skinReward = "fire", // Unlock fire skin
                expiresAt = expiresAt,
                isDaily = false
            ),
            Challenge(
                id = "weekly_combo_15",
                type = ChallengeType.REACH_COMBO,
                title = "Combo Legend",
                description = "Get a 15x combo",
                target = 15,
                difficulty = ChallengeDifficulty.HARD,
                xpReward = 350,
                expiresAt = expiresAt,
                isDaily = false
            ),
            Challenge(
                id = "weekly_mp_wins_5",
                type = ChallengeType.MULTIPLAYER_WIN,
                title = "Champion",
                description = "Win 5 multiplayer games",
                target = 5,
                difficulty = ChallengeDifficulty.HARD,
                xpReward = 500,
                expiresAt = expiresAt,
                isDaily = false
            )
        )
    }
}

/**
 * Player's progress on a challenge.
 */
data class ChallengeProgress(
    val challengeId: String,
    val currentProgress: Int,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
) {
    /**
     * Progress as fraction (0.0 to 1.0).
     */
    fun progressFraction(target: Int): Float =
        (currentProgress.toFloat() / target).coerceIn(0f, 1f)
}
