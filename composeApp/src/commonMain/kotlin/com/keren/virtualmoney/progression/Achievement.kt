package com.keren.virtualmoney.progression

/**
 * Achievement IDs.
 */
enum class AchievementId(
    val displayName: String,
    val description: String,
    val xpReward: Int,
    val iconName: String
) {
    // Beginner
    FIRST_GAME("First Steps", "Complete your first game", 25, "achievement_first"),
    SCORE_100("Getting Started", "Score 100 points in a game", 25, "achievement_score"),
    SCORE_200("Rising Star", "Score 200 points in a game", 50, "achievement_score"),
    SCORE_300("Skilled Hunter", "Score 300 points in a game", 75, "achievement_score"),
    SCORE_500("Elite Hunter", "Score 500 points in a game", 150, "achievement_elite"),

    // Collector
    COINS_100("Coin Collector", "Collect 100 coins total", 50, "achievement_coins"),
    COINS_500("Hoarder", "Collect 500 coins total", 100, "achievement_coins"),
    COINS_1000("Treasure Hunter", "Collect 1000 coins total", 200, "achievement_treasure"),
    COINS_5000("Coin Master", "Collect 5000 coins total", 500, "achievement_master"),

    // Combo
    COMBO_5("Combo Starter", "Get a 5x combo", 50, "achievement_combo"),
    COMBO_10("Combo Master", "Get a 10x combo", 100, "achievement_combo"),
    COMBO_20("Combo Legend", "Get a 20x combo", 200, "achievement_legend"),
    COMBO_30("Combo God", "Get a 30x combo", 400, "achievement_god"),

    // Perfect
    PERFECT_1("Clean Run", "Complete a game without penalties", 75, "achievement_perfect"),
    PERFECT_5("Perfectionist", "5 perfect runs", 150, "achievement_perfect"),
    PERFECT_10("Untouchable", "10 perfect runs", 300, "achievement_untouchable"),
    PERFECT_25("Flawless", "25 perfect runs", 500, "achievement_flawless"),

    // Games played
    GAMES_10("Dedicated", "Play 10 games", 50, "achievement_games"),
    GAMES_50("Committed", "Play 50 games", 100, "achievement_games"),
    GAMES_100("Veteran", "Play 100 games", 200, "achievement_veteran"),
    GAMES_500("Addicted", "Play 500 games", 500, "achievement_addicted"),

    // Power-ups
    POWERUPS_10("Power Player", "Collect 10 power-ups", 50, "achievement_powerup"),
    POWERUPS_50("Power Hungry", "Collect 50 power-ups", 100, "achievement_powerup"),
    POWERUPS_100("Power Master", "Collect 100 power-ups", 200, "achievement_powerup"),
    FREEZE_50("Ice Cold", "Use Freeze 50 times", 150, "achievement_freeze"),

    // Multiplayer
    MP_FIRST_WIN("First Victory", "Win a multiplayer game", 100, "achievement_victory"),
    MP_WINS_10("Champion", "Win 10 multiplayer games", 300, "achievement_champion"),
    MP_WINS_50("Dominator", "Win 50 multiplayer games", 750, "achievement_dominator"),
    MP_TOP10_50("Consistent", "Finish top 10% fifty times", 250, "achievement_consistent"),
    BR_WIN("Battle Royale Victor", "Win a Battle Royale", 200, "achievement_br"),
    BR_WINS_10("Battle Royale Champion", "Win 10 Battle Royales", 500, "achievement_br_champ"),

    // Special
    LEADERBOARD_1("Number One", "Reach #1 on any leaderboard", 1000, "achievement_number_one"),
    MAX_LEVEL("Max Level", "Reach level 50", 500, "achievement_max"),
    PRESTIGE("Prestige", "Prestige for the first time", 500, "achievement_prestige")
}

/**
 * Achievement requirement types.
 */
sealed class AchievementRequirement {
    data class GamesPlayed(val count: Int) : AchievementRequirement()
    data class TotalCoins(val count: Int) : AchievementRequirement()
    data class ScoreInGame(val score: Int) : AchievementRequirement()
    data class ComboReached(val combo: Int) : AchievementRequirement()
    data class PerfectRuns(val count: Int) : AchievementRequirement()
    data class PowerUpsCollected(val count: Int) : AchievementRequirement()
    data class SpecificPowerUp(val powerUpType: String, val count: Int) : AchievementRequirement()
    data class MultiplayerWins(val count: Int) : AchievementRequirement()
    data class MultiplayerTop10(val count: Int) : AchievementRequirement()
    data class BattleRoyaleWins(val count: Int) : AchievementRequirement()
    data class ReachLevel(val level: Int) : AchievementRequirement()
    data class ReachPrestige(val prestige: Int) : AchievementRequirement()
    data class LeaderboardRank(val rank: Int) : AchievementRequirement()
}

/**
 * Achievement definition with requirement.
 */
data class Achievement(
    val id: AchievementId,
    val requirement: AchievementRequirement
) {
    companion object {
        /**
         * Get all achievements with their requirements.
         */
        fun all(): List<Achievement> = listOf(
            // Beginner
            Achievement(AchievementId.FIRST_GAME, AchievementRequirement.GamesPlayed(1)),
            Achievement(AchievementId.SCORE_100, AchievementRequirement.ScoreInGame(100)),
            Achievement(AchievementId.SCORE_200, AchievementRequirement.ScoreInGame(200)),
            Achievement(AchievementId.SCORE_300, AchievementRequirement.ScoreInGame(300)),
            Achievement(AchievementId.SCORE_500, AchievementRequirement.ScoreInGame(500)),

            // Collector
            Achievement(AchievementId.COINS_100, AchievementRequirement.TotalCoins(100)),
            Achievement(AchievementId.COINS_500, AchievementRequirement.TotalCoins(500)),
            Achievement(AchievementId.COINS_1000, AchievementRequirement.TotalCoins(1000)),
            Achievement(AchievementId.COINS_5000, AchievementRequirement.TotalCoins(5000)),

            // Combo
            Achievement(AchievementId.COMBO_5, AchievementRequirement.ComboReached(5)),
            Achievement(AchievementId.COMBO_10, AchievementRequirement.ComboReached(10)),
            Achievement(AchievementId.COMBO_20, AchievementRequirement.ComboReached(20)),
            Achievement(AchievementId.COMBO_30, AchievementRequirement.ComboReached(30)),

            // Perfect
            Achievement(AchievementId.PERFECT_1, AchievementRequirement.PerfectRuns(1)),
            Achievement(AchievementId.PERFECT_5, AchievementRequirement.PerfectRuns(5)),
            Achievement(AchievementId.PERFECT_10, AchievementRequirement.PerfectRuns(10)),
            Achievement(AchievementId.PERFECT_25, AchievementRequirement.PerfectRuns(25)),

            // Games
            Achievement(AchievementId.GAMES_10, AchievementRequirement.GamesPlayed(10)),
            Achievement(AchievementId.GAMES_50, AchievementRequirement.GamesPlayed(50)),
            Achievement(AchievementId.GAMES_100, AchievementRequirement.GamesPlayed(100)),
            Achievement(AchievementId.GAMES_500, AchievementRequirement.GamesPlayed(500)),

            // Power-ups
            Achievement(AchievementId.POWERUPS_10, AchievementRequirement.PowerUpsCollected(10)),
            Achievement(AchievementId.POWERUPS_50, AchievementRequirement.PowerUpsCollected(50)),
            Achievement(AchievementId.POWERUPS_100, AchievementRequirement.PowerUpsCollected(100)),
            Achievement(AchievementId.FREEZE_50, AchievementRequirement.SpecificPowerUp("FREEZE", 50)),

            // Multiplayer
            Achievement(AchievementId.MP_FIRST_WIN, AchievementRequirement.MultiplayerWins(1)),
            Achievement(AchievementId.MP_WINS_10, AchievementRequirement.MultiplayerWins(10)),
            Achievement(AchievementId.MP_WINS_50, AchievementRequirement.MultiplayerWins(50)),
            Achievement(AchievementId.MP_TOP10_50, AchievementRequirement.MultiplayerTop10(50)),
            Achievement(AchievementId.BR_WIN, AchievementRequirement.BattleRoyaleWins(1)),
            Achievement(AchievementId.BR_WINS_10, AchievementRequirement.BattleRoyaleWins(10)),

            // Special
            Achievement(AchievementId.LEADERBOARD_1, AchievementRequirement.LeaderboardRank(1)),
            Achievement(AchievementId.MAX_LEVEL, AchievementRequirement.ReachLevel(50)),
            Achievement(AchievementId.PRESTIGE, AchievementRequirement.ReachPrestige(1))
        )

        /**
         * Get achievement by ID.
         */
        fun fromId(id: AchievementId): Achievement =
            all().first { it.id == id }
    }
}

/**
 * Earned achievement with timestamp.
 */
data class EarnedAchievement(
    val id: AchievementId,
    val earnedAt: Long
)
