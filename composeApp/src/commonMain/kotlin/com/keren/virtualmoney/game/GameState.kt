package com.keren.virtualmoney.game

/**
 * Represents the finite state machine (FSM) for the game.
 * The game can be in one of three states: Ready, Running, or Finished.
 */
sealed class GameState {
    /**
     * Initial state - waiting for user to start the game.
     */
    data object Ready : GameState()

    /**
     * Active gameplay state.
     * @param timeRemaining Time left in seconds
     * @param score Current score
     * @param coins List of active coins on screen
     * @param powerUps List of active power-ups in the world
     * @param activePowerUps List of currently active power-up effects on player
     * @param comboState Current combo state
     * @param lives Remaining lives (for survival mode)
     * @param coinsCollected Total coins collected this game
     * @param penaltiesHit Total penalties hit this game
     * @param gameMode The current game mode
     */
    data class Running(
        val timeRemaining: Int,
        val score: Int,
        val coins: List<Coin>,
        val powerUps: List<PowerUp> = emptyList(),
        val activePowerUps: List<ActivePowerUp> = emptyList(),
        val comboCount: Int = 0,
        val lives: Int = 3,
        val coinsCollected: Int = 0,
        val penaltiesHit: Int = 0,
        val gameMode: GameMode = GameMode.CLASSIC
    ) : GameState() {
        /**
         * Check if player has shield active.
         */
        fun hasShield(): Boolean = activePowerUps.any {
            it.type == PowerUpType.SHIELD && !it.isExpired()
        }

        /**
         * Check if player has magnet active.
         */
        fun hasMagnet(): Boolean = activePowerUps.any {
            it.type == PowerUpType.MAGNET && !it.isExpired()
        }

        /**
         * Get current score multiplier from power-ups.
         */
        fun getScoreMultiplier(): Float {
            val hasMultiplier = activePowerUps.any {
                it.type == PowerUpType.MULTIPLIER && !it.isExpired()
            }
            return if (hasMultiplier) 2.0f else 1.0f
        }

        /**
         * Check if this is a perfect run so far (no penalties hit).
         */
        fun isPerfectRun(): Boolean = penaltiesHit == 0
    }

    /**
     * Game over state.
     * @param finalScore The final score achieved
     * @param isNewHighScore Whether this score beats the previous high score
     * @param coinsCollected Total coins collected
     * @param bestCombo Best combo achieved
     * @param powerUpsCollected Power-ups collected this game
     * @param wasPerfectRun Whether player avoided all penalties
     * @param playTimeMs Total play time in milliseconds
     * @param gameMode The game mode that was played
     */
    data class Finished(
        val finalScore: Int,
        val isNewHighScore: Boolean,
        val coinsCollected: Int = 0,
        val bestCombo: Int = 0,
        val powerUpsCollected: Int = 0,
        val wasPerfectRun: Boolean = false,
        val playTimeMs: Long = 0,
        val gameMode: GameMode = GameMode.CLASSIC
    ) : GameState()
}
