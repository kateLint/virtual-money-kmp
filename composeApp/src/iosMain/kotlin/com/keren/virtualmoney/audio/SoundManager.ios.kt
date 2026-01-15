package com.keren.virtualmoney.audio

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
actual class SoundManager actual constructor() {
    private val players = mutableMapOf<GameSound, AVAudioPlayer?>()
    private var loopingPlayers = mutableMapOf<Int, AVAudioPlayer?>()
    private var loopCounter = 0
    private var masterVolume = 1.0f
    private var enabled = true
    private var initialized = false

    actual fun initialize() {
        if (initialized) return

        // Configure audio session
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, null)
            session.setActive(true, null)
        } catch (e: Exception) {
            // Ignore audio session errors
        }

        // Preload sounds
        GameSound.entries.forEach { sound ->
            try {
                val path = NSBundle.mainBundle.pathForResource(sound.fileName, "mp3")
                    ?: NSBundle.mainBundle.pathForResource(sound.fileName, "wav")

                if (path != null) {
                    val url = NSURL.fileURLWithPath(path)
                    val player = AVAudioPlayer(url, null)
                    player?.prepareToPlay()
                    players[sound] = player
                }
            } catch (e: Exception) {
                // Sound file not found, skip
            }
        }

        initialized = true
    }

    actual fun play(sound: GameSound, volume: Float) {
        if (!enabled || !initialized) return

        val player = players[sound]
        if (player != null) {
            player.volume = volume * masterVolume
            player.currentTime = 0.0
            player.play()
        }
    }

    actual fun playLoop(sound: GameSound, volume: Float): Int {
        if (!enabled || !initialized) return -1

        try {
            val path = NSBundle.mainBundle.pathForResource(sound.fileName, "mp3")
                ?: NSBundle.mainBundle.pathForResource(sound.fileName, "wav")

            if (path != null) {
                val url = NSURL.fileURLWithPath(path)
                val player = AVAudioPlayer(url, null)
                if (player != null) {
                    player.volume = volume * masterVolume
                    player.numberOfLoops = -1 // Infinite loop
                    player.play()

                    val handle = ++loopCounter
                    loopingPlayers[handle] = player
                    return handle
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }
        return -1
    }

    actual fun stopLoop(handle: Int) {
        loopingPlayers[handle]?.stop()
        loopingPlayers.remove(handle)
    }

    actual fun stopAll() {
        players.values.forEach { it?.stop() }
        loopingPlayers.values.forEach { it?.stop() }
        loopingPlayers.clear()
    }

    actual fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
        // Update all playing sounds
        players.values.forEach { it?.volume = masterVolume }
        loopingPlayers.values.forEach { it?.volume = masterVolume }
    }

    actual fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (!enabled) {
            stopAll()
        }
    }

    actual fun isEnabled(): Boolean = enabled

    actual fun release() {
        stopAll()
        players.clear()
        initialized = false
    }
}
