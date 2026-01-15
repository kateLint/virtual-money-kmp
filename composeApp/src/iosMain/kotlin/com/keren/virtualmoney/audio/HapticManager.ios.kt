package com.keren.virtualmoney.audio

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UISelectionFeedbackGenerator

actual class HapticManager actual constructor() {
    private var enabled = true
    private var initialized = false

    private val lightImpact by lazy { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight) }
    private val mediumImpact by lazy { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium) }
    private val heavyImpact by lazy { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy) }
    private val notification by lazy { UINotificationFeedbackGenerator() }
    private val selection by lazy { UISelectionFeedbackGenerator() }

    actual fun initialize() {
        if (initialized) return

        // Prepare haptic generators
        lightImpact.prepare()
        mediumImpact.prepare()
        heavyImpact.prepare()
        notification.prepare()
        selection.prepare()

        initialized = true
    }

    actual fun vibrate(type: HapticType) {
        if (!enabled || !initialized) return

        when (type) {
            HapticType.LIGHT -> lightImpact.impactOccurred()
            HapticType.MEDIUM -> mediumImpact.impactOccurred()
            HapticType.HEAVY -> heavyImpact.impactOccurred()
            HapticType.SUCCESS -> notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
            HapticType.WARNING -> notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeWarning)
            HapticType.ERROR -> notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
            HapticType.SELECTION -> selection.selectionChanged()
        }
    }

    actual fun vibratePattern(pattern: LongArray) {
        if (!enabled || !initialized) return

        // iOS doesn't support custom vibration patterns like Android
        // We approximate by analyzing the pattern intensity and choosing appropriate feedback
        val totalDuration = pattern.sum()
        val vibrateCount = pattern.size / 2 // pairs of (delay, vibrate)

        when {
            totalDuration > 500 -> {
                // Long intense pattern - use notification
                notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
            }
            totalDuration > 200 -> {
                // Medium pattern - heavy impact
                heavyImpact.impactOccurred()
            }
            vibrateCount > 3 -> {
                // Multiple quick pulses - medium impact
                mediumImpact.impactOccurred()
            }
            totalDuration > 50 -> {
                // Short pattern - light impact
                lightImpact.impactOccurred()
            }
            else -> {
                // Very short - selection feedback
                selection.selectionChanged()
            }
        }
    }

    actual fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    actual fun isEnabled(): Boolean = enabled

    actual fun release() {
        initialized = false
    }
}
