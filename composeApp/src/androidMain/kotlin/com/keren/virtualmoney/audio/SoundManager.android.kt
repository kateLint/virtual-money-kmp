package com.keren.virtualmoney.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.keren.virtualmoney.util.applicationContext

actual class SoundManager actual constructor() {
    private val context: Context = applicationContext

    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<GameSound, Int>()
    private var masterVolume = 1.0f
    private var enabled = true
    private var initialized = false

    actual fun initialize() {
        if (initialized) return

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        // Load all sounds
        // Note: In production, these would be actual sound files in res/raw/
        // For now, we'll use placeholder loading that won't crash if files don't exist
        GameSound.entries.forEach { sound ->
            try {
                val resId = context.resources.getIdentifier(
                    sound.fileName,
                    "raw",
                    context.packageName
                )
                if (resId != 0) {
                    soundIds[sound] = soundPool?.load(context, resId, 1) ?: -1
                }
            } catch (e: Exception) {
                // Sound file not found, skip
            }
        }

        initialized = true
    }

    actual fun play(sound: GameSound, volume: Float) {
        if (!enabled || !initialized) return

        val soundId = soundIds[sound]
        if (soundId != null && soundId != -1) {
            val finalVolume = volume * masterVolume
            soundPool?.play(soundId, finalVolume, finalVolume, 1, 0, 1.0f)
        } else {
            // Fallback: use system sound for feedback
            playFallbackSound(sound)
        }
    }

    actual fun playLoop(sound: GameSound, volume: Float): Int {
        if (!enabled || !initialized) return -1

        val soundId = soundIds[sound]
        if (soundId != null && soundId != -1) {
            val finalVolume = volume * masterVolume
            return soundPool?.play(soundId, finalVolume, finalVolume, 1, -1, 1.0f) ?: -1
        }
        return -1
    }

    actual fun stopLoop(handle: Int) {
        if (handle != -1) {
            soundPool?.stop(handle)
        }
    }

    actual fun stopAll() {
        soundPool?.autoPause()
    }

    actual fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
    }

    actual fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (!enabled) {
            stopAll()
        }
    }

    actual fun isEnabled(): Boolean = enabled

    actual fun release() {
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        initialized = false
    }

    private fun playFallbackSound(sound: GameSound) {
        // Use system sounds as fallback when custom sounds aren't loaded
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            when (sound) {
                GameSound.COIN_COLLECT,
                GameSound.POWERUP_COLLECT,
                GameSound.COMBO_MILESTONE -> {
                    audioManager.playSoundEffect(android.media.AudioManager.FX_KEY_CLICK, masterVolume)
                }
                GameSound.PENALTY_HIT,
                GameSound.COMBO_BREAK -> {
                    audioManager.playSoundEffect(android.media.AudioManager.FX_KEYPRESS_INVALID, masterVolume)
                }
                GameSound.BUTTON_CLICK -> {
                    audioManager.playSoundEffect(android.media.AudioManager.FX_KEY_CLICK, masterVolume)
                }
                else -> {
                    // No fallback for other sounds
                }
            }
        } catch (e: Exception) {
            // Ignore sound errors
        }
    }
}
