package com.keren.virtualmoney.game

/** Available game modes. */
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
                minPlayers = 2,
                maxPlayers = 100
        ),
        TEAM_BATTLE(
                displayName = "Team Battle",
                description = "Teams compete for highest score",
                duration = 180,
                isMultiplayer = true,
                minPlayers = 2,
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

/** Configuration for a game session with AR-specific parameters. */
data class GameConfig(
        val mode: GameMode,
        val duration: Int = mode.duration ?: 60,
        val startingLives: Int = 3,
        val powerUpsEnabled: Boolean = true,
        val difficultyScaling: Boolean = true,

        // === AR Coin Spawning Configuration ===

        // Visible coin cap (max coins on screen at once)
        val visibleCoinCap: Int = 22,

        // Bad coin ratio (0.0 to 1.0, e.g., 0.30 = 30%)
        val badCoinRatio: Float = 0.30f,

        // Total coins to spawn over match (respawns included)
        val totalSpawnLimit: Int = 40,

        // Initial spawn counts for onboarding phase
        val initialGoodCoins: Int = 7,
        val initialBadCoins: Int = 3,

        // Respawn delay range (ms)
        val respawnDelayMinMs: Long = 500L,
        val respawnDelayMaxMs: Long = 1500L,

        // Power-up count for the match
        val powerUpCount: Int = 3,
        val powerUpSpawnIntervalMs: Long = 20000L, // Spawn at intervals

        // Distance range for coin spawning (meters)
        val coinDistanceMin: Float = 1.2f,
        val coinDistanceMax: Float = 4.5f,

        // Minimum safe distance for bad coins (meters)
        val badCoinMinDistance: Float = 2.0f,

        // Anti-stall: spawn hint coin after this many seconds idle
        val antiStallIdleSeconds: Int = 10,

        // Onboarding: bias first coins to front cone (degrees from forward)
        val onboardingConeAngle: Float = 60f, // ±60° from forward = 120° cone

        // Tracking stability: wait before first spawn (ms)
        val trackingStabilityDelayMs: Long = 1500L,

        // Coin lifetime in milliseconds
        val coinLifetimeMs: Long = 15000L
) {
        companion object {
                // 60-second round: 18-25 visible, 30-45 total, 30% bad, 2-4 power-ups
                fun classic() =
                        GameConfig(
                                mode = GameMode.CLASSIC,
                                visibleCoinCap = 22,
                                badCoinRatio = 0.30f,
                                totalSpawnLimit = 40,
                                initialGoodCoins = 7,
                                initialBadCoins = 3,
                                powerUpCount = 3,
                                powerUpSpawnIntervalMs = 20000L
                        )

                // 30-second blitz: faster, fewer coins
                fun blitz() =
                        GameConfig(
                                mode = GameMode.BLITZ,
                                visibleCoinCap = 18,
                                badCoinRatio = 0.25f,
                                totalSpawnLimit = 25,
                                initialGoodCoins = 5,
                                initialBadCoins = 2,
                                powerUpCount = 2,
                                powerUpSpawnIntervalMs = 10000L
                        )

                fun survival() =
                        GameConfig(
                                mode = GameMode.SURVIVAL,
                                duration = Int.MAX_VALUE,
                                startingLives = 3,
                                visibleCoinCap = 20,
                                badCoinRatio = 0.40f,
                                totalSpawnLimit = Int.MAX_VALUE
                        )

                // 60s multiplayer
                fun quickMatch() =
                        GameConfig(
                                mode = GameMode.QUICK_MATCH,
                                visibleCoinCap = 22,
                                badCoinRatio = 0.30f,
                                totalSpawnLimit = 40,
                                powerUpCount = 3
                        )

                // 180s battle royale
                fun battleRoyale() =
                        GameConfig(
                                mode = GameMode.BATTLE_ROYALE,
                                visibleCoinCap = 30,
                                badCoinRatio = 0.30f,
                                totalSpawnLimit = 70,
                                powerUpCount = 5
                        )

                // 180s team battle
                fun teamBattle() =
                        GameConfig(
                                mode = GameMode.TEAM_BATTLE,
                                visibleCoinCap = 35,
                                badCoinRatio = 0.30f,
                                totalSpawnLimit = 75,
                                powerUpCount = 6
                        )

                // 120s king of hill
                fun kingOfHill() =
                        GameConfig(
                                mode = GameMode.KING_OF_HILL,
                                visibleCoinCap = 28,
                                badCoinRatio = 0.30f,
                                totalSpawnLimit = 60,
                                powerUpCount = 4
                        )
        }
}
