package com.keren.virtualmoney.platform

/**
 * Platform-specific persistent storage for high score.
 * Uses SharedPreferences on Android and UserDefaults on iOS.
 */
interface HighScoreStorage {
    /**
     * Retrieves the current high score.
     */
    fun getHighScore(): Int

    /**
     * Saves a new high score.
     */
    fun saveHighScore(score: Int)
}

/**
 * Factory function to create platform-specific HighScoreStorage instance.
 */
expect fun createHighScoreStorage(): HighScoreStorage
