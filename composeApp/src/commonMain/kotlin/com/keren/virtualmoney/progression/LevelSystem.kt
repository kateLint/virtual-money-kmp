package com.keren.virtualmoney.progression

/**
 * XP and leveling system.
 */
object LevelSystem {
    const val MAX_LEVEL = 50
    const val PRESTIGE_LEVEL = 50

    // XP rewards
    const val XP_GAME_COMPLETE = 10
    const val XP_PER_COIN = 1
    const val XP_HIGH_SCORE_BEATEN = 50
    const val XP_PERFECT_RUN = 25
    const val XP_DAILY_CHALLENGE = 75
    const val XP_WEEKLY_CHALLENGE = 300
    const val XP_MULTIPLAYER_WIN = 100
    const val XP_MULTIPLAYER_TOP_10 = 50
    const val XP_MULTIPLAYER_TOP_3 = 75
    const val XP_BATTLE_ROYALE_WIN = 200
    const val XP_ACHIEVEMENT = 50

    // Prestige bonus (10% more XP per prestige level)
    const val PRESTIGE_XP_BONUS = 0.10f

    /**
     * Calculate total XP required to reach a level.
     * Uses a curve: low levels are fast, higher levels slower.
     */
    fun getXPForLevel(level: Int): Int = xpForLevel(level)

    fun xpForLevel(level: Int): Int = when {
        level <= 1 -> 0
        level <= 5 -> 100 * (level - 1) // 100, 200, 300, 400 for levels 2-5
        level <= 10 -> xpForLevel(5) + 150 * (level - 5) // 150 per level 6-10
        level <= 20 -> xpForLevel(10) + 200 * (level - 10) // 200 per level 11-20
        level <= 30 -> xpForLevel(20) + 300 * (level - 20) // 300 per level 21-30
        level <= 40 -> xpForLevel(30) + 400 * (level - 30) // 400 per level 31-40
        level <= 50 -> xpForLevel(40) + 500 * (level - 40) // 500 per level 41-50
        else -> xpForLevel(50) // Cap at level 50
    }

    /**
     * Calculate level from total XP.
     */
    fun levelForXp(xp: Int): Int {
        var level = 1
        while (level < MAX_LEVEL && xpForLevel(level + 1) <= xp) {
            level++
        }
        return level
    }

    /**
     * Calculate XP needed to reach the next level.
     */
    fun xpToNextLevel(currentXp: Int): Int {
        val currentLevel = levelForXp(currentXp)
        if (currentLevel >= MAX_LEVEL) return 0
        return xpForLevel(currentLevel + 1) - currentXp
    }

    /**
     * Calculate XP reward for a game result.
     */
    fun calculateXpReward(
        result: GameResult,
        beatHighScore: Boolean,
        prestige: Int = 0
    ): XpRewardBreakdown {
        var baseXp = XP_GAME_COMPLETE
        val breakdown = mutableListOf<XpComponent>()

        // Base game completion
        breakdown.add(XpComponent("Game Complete", XP_GAME_COMPLETE))

        // Coins collected
        val coinXp = result.coinsCollected * XP_PER_COIN
        if (coinXp > 0) {
            baseXp += coinXp
            breakdown.add(XpComponent("Coins Collected", coinXp))
        }

        // High score
        if (beatHighScore) {
            baseXp += XP_HIGH_SCORE_BEATEN
            breakdown.add(XpComponent("New High Score!", XP_HIGH_SCORE_BEATEN))
        }

        // Perfect run
        if (result.wasPerfectRun) {
            baseXp += XP_PERFECT_RUN
            breakdown.add(XpComponent("Perfect Run!", XP_PERFECT_RUN))
        }

        // Multiplayer rewards
        if (result.isMultiplayer && result.multiplayerRank != null) {
            val totalPlayers = result.totalPlayers ?: 2

            when {
                result.multiplayerRank == 1 -> {
                    val winXp = if (result.wasBattleRoyale) XP_BATTLE_ROYALE_WIN else XP_MULTIPLAYER_WIN
                    baseXp += winXp
                    breakdown.add(XpComponent("Victory!", winXp))
                }
                result.multiplayerRank <= 3 -> {
                    baseXp += XP_MULTIPLAYER_TOP_3
                    breakdown.add(XpComponent("Top 3 Finish", XP_MULTIPLAYER_TOP_3))
                }
                result.multiplayerRank <= (totalPlayers * 0.1).toInt().coerceAtLeast(1) -> {
                    baseXp += XP_MULTIPLAYER_TOP_10
                    breakdown.add(XpComponent("Top 10%", XP_MULTIPLAYER_TOP_10))
                }
            }
        }

        // Prestige bonus
        val prestigeMultiplier = 1.0f + (prestige * PRESTIGE_XP_BONUS)
        val finalXp = (baseXp * prestigeMultiplier).toInt()

        if (prestige > 0) {
            val bonusXp = finalXp - baseXp
            breakdown.add(XpComponent("Prestige Bonus", bonusXp))
        }

        return XpRewardBreakdown(
            totalXp = finalXp,
            components = breakdown
        )
    }

    /**
     * Calculate XP from a game (simple version for GameOverScreen).
     */
    fun calculateXPFromGame(
        score: Int,
        coinsCollected: Int,
        perfectRun: Boolean
    ): Int {
        var xp = XP_GAME_COMPLETE + coinsCollected * XP_PER_COIN
        if (perfectRun) xp += XP_PERFECT_RUN
        // Bonus for high scores
        xp += score / 100
        return xp
    }

    /**
     * Check if player can prestige (level 50).
     */
    fun canPrestige(level: Int): Boolean = level >= PRESTIGE_LEVEL

    /**
     * Calculate new state after prestige.
     */
    fun prestige(currentProfile: PlayerProfile): PlayerProfile {
        if (!canPrestige(currentProfile.level)) return currentProfile

        return currentProfile.copy(
            level = 1,
            xp = 0,
            prestige = currentProfile.prestige + 1
        )
    }
}

/**
 * XP reward breakdown for display.
 */
data class XpRewardBreakdown(
    val totalXp: Int,
    val components: List<XpComponent>
)

/**
 * Individual XP component.
 */
data class XpComponent(
    val description: String,
    val amount: Int
)
