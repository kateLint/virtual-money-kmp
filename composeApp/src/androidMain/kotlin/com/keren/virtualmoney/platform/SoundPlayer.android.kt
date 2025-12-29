package com.keren.virtualmoney.platform

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.keren.virtualmoney.util.applicationContext

private class AndroidSoundPlayer(private val context: Context) : SoundPlayer {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // For now, we'll use a system sound. In production, you'd load from resources.
    // We'll use ToneGenerator as a placeholder for coin sound
    private var coinSoundId: Int = -1

    override fun playCoinSound() {
        // Using a simple beep sound for now
        // In production, you would:
        // 1. Add coin.mp3 to res/raw/
        // 2. Load it: coinSoundId = soundPool.load(context, R.raw.coin, 1)
        // 3. Play it: soundPool.play(coinSoundId, 1f, 1f, 1, 0, 1f)

        // For demo purposes, we'll use Android system sound
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        audioManager.playSoundEffect(android.media.AudioManager.FX_KEY_CLICK)
    }

    override fun release() {
        soundPool.release()
    }
}

actual fun createSoundPlayer(): SoundPlayer {
    return AndroidSoundPlayer(applicationContext)
}
