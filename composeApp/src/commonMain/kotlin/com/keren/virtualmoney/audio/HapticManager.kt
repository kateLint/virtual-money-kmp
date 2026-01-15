package com.keren.virtualmoney.audio

/**
 * Platform-agnostic haptic feedback manager.
 * Implementations handle platform-specific vibration/haptics.
 */
expect class HapticManager() {
    /**
     * Initialize the haptic manager.
     */
    fun initialize()

    /**
     * Trigger haptic feedback.
     * @param type The type of haptic feedback
     */
    fun vibrate(type: HapticType)

    /**
     * Trigger a custom vibration pattern.
     * @param pattern Array of durations in milliseconds (vibrate, pause, vibrate, ...)
     */
    fun vibratePattern(pattern: LongArray)

    /**
     * Enable or disable haptic feedback.
     */
    fun setEnabled(enabled: Boolean)

    /**
     * Check if haptics are enabled.
     */
    fun isEnabled(): Boolean

    /**
     * Release resources.
     */
    fun release()
}
