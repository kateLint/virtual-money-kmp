package com.keren.virtualmoney.platform

import android.content.Context
import android.content.SharedPreferences
import com.keren.virtualmoney.util.applicationContext

private class AndroidHighScoreStorage(context: Context) : HighScoreStorage {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "coin_hunter_prefs",
        Context.MODE_PRIVATE
    )

    override fun getHighScore(): Int {
        return prefs.getInt(KEY_HIGH_SCORE, 0)
    }

    override fun saveHighScore(score: Int) {
        prefs.edit()
            .putInt(KEY_HIGH_SCORE, score)
            .apply()
    }

    companion object {
        private const val KEY_HIGH_SCORE = "high_score"
    }
}

actual fun createHighScoreStorage(): HighScoreStorage {
    return AndroidHighScoreStorage(applicationContext)
}
