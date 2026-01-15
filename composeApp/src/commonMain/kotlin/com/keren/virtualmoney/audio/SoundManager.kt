package com.keren.virtualmoney.audio

/**
 * Platform-agnostic sound manager interface.
 * Implementations handle platform-specific audio playback.
 */
expect class SoundManager() {
    /**
     * Initialize the sound manager and preload sounds.
     */
    fun initialize()

    /**
     * Play a sound effect.
     * @param sound The sound to play
     * @param volume Volume level (0.0 to 1.0)
     */
    fun play(sound: GameSound, volume: Float = 1.0f)

    /**
     * Play a looping sound (e.g., power-up active sound).
     * @param sound The sound to loop
     * @param volume Volume level (0.0 to 1.0)
     * @return A handle to stop the loop later
     */
    fun playLoop(sound: GameSound, volume: Float = 0.5f): Int

    /**
     * Stop a looping sound.
     * @param handle The handle returned from playLoop
     */
    fun stopLoop(handle: Int)

    /**
     * Stop all currently playing sounds.
     */
    fun stopAll()

    /**
     * Set master volume for all sounds.
     * @param volume Volume level (0.0 to 1.0)
     */
    fun setMasterVolume(volume: Float)

    /**
     * Enable or disable sound effects.
     */
    fun setEnabled(enabled: Boolean)

    /**
     * Check if sound is enabled.
     */
    fun isEnabled(): Boolean

    /**
     * Release resources when no longer needed.
     */
    fun release()
}
