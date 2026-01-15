package com.keren.virtualmoney.game

/**
 * Available game modes.
 */
enum class GameMode(
    val displayName: String,
    val description: String,
    val duration: Int?, // null = endless
    val isMultiplayer: Boolean,
    val minPlayers: Int = 1,
    val maxPlayers: Int = 1
) {
    // Single Player Modes
    CLASSIC(
        displayName = "Classic",
        description = "60 seconds to collect coins",
        duration = 60,
        isMultiplayer = false
    ),
    BLITZ(
        displayName = "Blitz",
        description = "Fast 30 second round",
        duration = 30,
        isMultiplayer = false
    ),
    SURVIVAL(
        displayName = "Survival",
        description = "3 lives, last as long as you can",
        duration = null,
        isMultiplayer = false
    ),

    // Multiplayer Modes
    QUICK_MATCH(
        displayName = "Quick Match",
        description = "60s battle, highest score wins",
        duration = 60,
        isMultiplayer = true,
        minPlayers = 2,
        maxPlayers = 10
    ),
    BATTLE_ROYALE(
        displayName = "Battle Royale",
        description = "Last player standing wins",
        duration = 180,
        isMultiplayer = true,
        minPlayers = 10,
        maxPlayers = 100
    ),
    TEAM_BATTLE(
        displayName = "Team Battle",
        description = "Teams compete for highest score",
        duration = 180,
        isMultiplayer = true,
        minPlayers = 4,
        maxPlayers = 50
    ),
    KING_OF_HILL(
        displayName = "King of the Hill",
        description = "Hold #1 position for 30s to win",
        duration = 120,
        isMultiplayer = true,
        minPlayers = 2,
        maxPlayers = 20
    );

    companion object {
        fun singlePlayerModes(): List<GameMode> = entries.filter { !it.isMultiplayer }
        fun multiplayerModes(): List<GameMode> = entries.filter { it.isMultiplayer }
    }
}

/**
 * Configuration for a game session.
 */
data class GameConfig(
    val mode: GameMode,
    val duration: Int = mode.duration ?: 60,
    val startingLives: Int = 3, // For survival mode
    val powerUpsEnabled: Boolean = true,
    val difficultyScaling: Boolean = true,
    val initialCoinCount: Int = 4,
    val initialPenaltyCoinCount: Int = 3
) {
    companion object {
        fun classic() = GameConfig(mode = GameMode.CLASSIC)
        fun blitz() = GameConfig(mode = GameMode.BLITZ)
        fun survival() = GameConfig(
            mode = GameMode.SURVIVAL,
            duration = Int.MAX_VALUE,
            startingLives = 3
        )

        fun quickMatch() = GameConfig(mode = GameMode.QUICK_MATCH)
        fun battleRoyale() = GameConfig(mode = GameMode.BATTLE_ROYALE)
        fun teamBattle() = GameConfig(mode = GameMode.TEAM_BATTLE)
        fun kingOfHill() = GameConfig(mode = GameMode.KING_OF_HILL)
    }
}
