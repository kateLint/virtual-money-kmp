package com.keren.virtualmoney.platform

import platform.Foundation.NSUserDefaults

private class IOSHighScoreStorage : HighScoreStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun getHighScore(): Int {
        return userDefaults.integerForKey(KEY_HIGH_SCORE).toInt()
    }

    override fun saveHighScore(score: Int) {
        userDefaults.setInteger(score.toLong(), KEY_HIGH_SCORE)
        userDefaults.synchronize()
    }

    companion object {
        private const val KEY_HIGH_SCORE = "high_score"
    }
}

actual fun createHighScoreStorage(): HighScoreStorage {
    return IOSHighScoreStorage()
}
