package com.keren.virtualmoney.platform

import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

private class IOSSoundPlayer : SoundPlayer {
    private var audioPlayer: AVAudioPlayer? = null

    init {
        // Configure audio session
        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategory(
            AVAudioSessionCategoryPlayback,
            error = null
        )
        audioSession.setActive(true, null)
    }

    override fun playCoinSound() {
        // For now, we'll use a system sound
        // In production, you would:
        // 1. Add coin.mp3 to iOS project resources
        // 2. Load it: val soundURL = NSBundle.mainBundle.URLForResource("coin", "mp3")
        // 3. Create player: audioPlayer = AVAudioPlayer(soundURL, null)
        // 4. Play it: audioPlayer?.play()

        // Using system sound for demo
        platform.AudioToolbox.AudioServicesPlaySystemSound(1057u) // System click sound
    }

    override fun release() {
        audioPlayer?.stop()
        audioPlayer = null
    }
}

actual fun createSoundPlayer(): SoundPlayer {
    return IOSSoundPlayer()
}
