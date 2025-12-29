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
     */
    data class Running(
        val timeRemaining: Int,
        val score: Int,
        val coins: List<Coin>
    ) : GameState()

    /**
     * Game over state.
     * @param finalScore The final score achieved
     * @param isNewHighScore Whether this score beats the previous high score
     */
    data class Finished(
        val finalScore: Int,
        val isNewHighScore: Boolean
    ) : GameState()
}
