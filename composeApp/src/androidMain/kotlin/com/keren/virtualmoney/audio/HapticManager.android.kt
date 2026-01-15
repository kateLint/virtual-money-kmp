package com.keren.virtualmoney.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.keren.virtualmoney.util.applicationContext

actual class HapticManager actual constructor() {
    private val context: Context = applicationContext
    private var enabled = true
    private var initialized = false

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    actual fun initialize() {
        initialized = true
    }

    actual fun vibrate(type: HapticType) {
        if (!enabled || !initialized) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (type) {
                HapticType.LIGHT -> VibrationEffect.createOneShot(30, 50)
                HapticType.MEDIUM -> VibrationEffect.createOneShot(50, 150)
                HapticType.HEAVY -> VibrationEffect.createOneShot(100, 255)
                HapticType.SUCCESS -> VibrationEffect.createWaveform(
                    longArrayOf(0, 50, 50, 100),
                    intArrayOf(0, 150, 0, 200),
                    -1
                )
                HapticType.WARNING -> VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 50, 100),
                    intArrayOf(0, 100, 0, 100),
                    -1
                )
                HapticType.ERROR -> VibrationEffect.createOneShot(200, 255)
                HapticType.SELECTION -> VibrationEffect.createOneShot(10, 30)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            when (type) {
                HapticType.LIGHT -> vibrator.vibrate(30)
                HapticType.MEDIUM -> vibrator.vibrate(50)
                HapticType.HEAVY -> vibrator.vibrate(100)
                HapticType.SUCCESS -> vibrator.vibrate(longArrayOf(0, 50, 50, 100), -1)
                HapticType.WARNING -> vibrator.vibrate(longArrayOf(0, 100, 50, 100), -1)
                HapticType.ERROR -> vibrator.vibrate(200)
                HapticType.SELECTION -> vibrator.vibrate(10)
            }
        }
    }

    actual fun vibratePattern(pattern: LongArray) {
        if (!enabled || !initialized) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    actual fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    actual fun isEnabled(): Boolean = enabled

    actual fun release() {
        vibrator.cancel()
        initialized = false
    }
}
