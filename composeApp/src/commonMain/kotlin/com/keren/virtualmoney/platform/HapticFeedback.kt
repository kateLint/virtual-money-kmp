package com.keren.virtualmoney.platform

/**
 * Platform-specific haptic feedback interface.
 * Each platform (Android/iOS) will provide its own implementation.
 */
interface HapticFeedback {
    /**
     * Triggers a light haptic feedback (for coin collection).
     */
    fun performLight()
}

/**
 * Factory function to create platform-specific HapticFeedback instance.
 */
expect fun createHapticFeedback(): HapticFeedback
