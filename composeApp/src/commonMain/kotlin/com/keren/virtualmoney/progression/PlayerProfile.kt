package com.keren.virtualmoney.progression

import com.keren.virtualmoney.platform.getCurrentTimeMillis

/**
 * Player profile data.
 */
data class PlayerProfile(
    val odId: String = "",
    val displayName: String = "Player",
    val avatarUrl: String? = null,
    val level: Int = 1,
    val xp: Int = 0,
    val prestige: Int = 0,
    val title: String? = null,
    val createdAt: Long = getCurrentTimeMillis(),
    val lastLogin: Long = getCurrentTimeMillis()
) {
    // Alias for total XP
    val totalXP: Int get() = xp
    /**
     * XP needed for next level.
     */
    fun xpToNextLevel(): Int = LevelSystem.xpToNextLevel(xp)

    /**
     * Total XP needed for next level.
     */
    fun xpForNextLevel(): Int = LevelSystem.xpForLevel(level + 1)

    /**
     * Progress towards next level (0.0 to 1.0).
     */
    fun levelProgress(): Float {
        val currentLevelXp = LevelSystem.xpForLevel(level)
        val nextLevelXp = LevelSystem.xpForLevel(level + 1)
        val xpInCurrentLevel = xp - currentLevelXp
        val xpNeededForLevel = nextLevelXp - currentLevelXp
        return (xpInCurrentLevel.toFloat() / xpNeededForLevel).coerceIn(0f, 1f)
    }

    /**
     * Display string for prestige level.
     */
    fun prestigeDisplay(): String? = when {
        prestige <= 0 -> null
        prestige == 1 -> "Bronze"
        prestige == 2 -> "Silver"
        prestige == 3 -> "Gold"
        prestige == 4 -> "Platinum"
        else -> "Diamond"
    }
}

/**
 * Player statistics.
 */
data class PlayerStats(
    val gamesPlayed: Int = 0,
    val totalCoins: Int = 0,
    val highScore: Int = 0,
    val bestCombo: Int = 0,
    val perfectRuns: Int = 0,
    val totalPlayTime: Long = 0, // milliseconds
    val powerUpsCollected: Int = 0,
    val multiplayerWins: Int = 0,
    val multiplayerTop10: Int = 0,
    val multiplayerGamesPlayed: Int = 0,
    val battleRoyaleWins: Int = 0
) {
    // Aliases for UI
    val totalGames: Int get() = gamesPlayed
    val totalCoinsCollected: Int get() = totalCoins
    val totalPlayTimeMinutes: Int get() = (totalPlayTime / 60000).toInt()
    /**
     * Calculate win rate for multiplayer.
     */
    fun multiplayerWinRate(): Float {
        if (multiplayerGamesPlayed == 0) return 0f
        return multiplayerWins.toFloat() / multiplayerGamesPlayed
    }

    /**
     * Format total play time as readable string.
     */
    fun formattedPlayTime(): String {
        val totalSeconds = totalPlayTime / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "<1m"
        }
    }
}

/**
 * Result of a game session for stats tracking.
 */
data class GameResult(
    val score: Int,
    val coinsCollected: Int,
    val bestCombo: Int,
    val powerUpsCollected: Int,
    val wasPerfectRun: Boolean,
    val playTimeMs: Long,
    val gameMode: String = "CLASSIC",
    val isMultiplayer: Boolean = false,
    val multiplayerRank: Int? = null,
    val totalPlayers: Int? = null,
    val wasBattleRoyale: Boolean = false
) {
    // Alias for perfectRun in different naming
    val perfectRun: Boolean get() = wasPerfectRun
    val penaltiesHit: Int get() = if (wasPerfectRun) 0 else 1
}
