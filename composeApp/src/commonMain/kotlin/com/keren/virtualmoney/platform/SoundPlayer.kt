package com.keren.virtualmoney.platform

/**
 * Platform-specific sound player interface.
 * Each platform (Android/iOS) will provide its own implementation.
 */
interface SoundPlayer {
    /**
     * Plays the coin collection sound effect.
     */
    fun playCoinSound()

    /**
     * Releases resources (call when done).
     */
    fun release()
}

/**
 * Factory function to create platform-specific SoundPlayer instance.
 */
expect fun createSoundPlayer(): SoundPlayer
