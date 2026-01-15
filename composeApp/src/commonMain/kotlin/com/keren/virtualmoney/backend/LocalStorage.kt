package com.keren.virtualmoney.backend

import com.keren.virtualmoney.game.GameMode
import com.keren.virtualmoney.progression.PlayerProfile
import com.keren.virtualmoney.progression.PlayerStats
import com.keren.virtualmoney.theme.CoinSkinId
import com.keren.virtualmoney.theme.ThemeId
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Local storage for offline data persistence.
 * Uses multiplatform-settings for KMP compatibility.
 */
class LocalStorage(
    private val settings: Settings = Settings()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    companion object {
        // Keys
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val KEY_LEVEL = "level"
        private const val KEY_XP = "xp"
        private const val KEY_PRESTIGE = "prestige"

        private const val KEY_GAMES_PLAYED = "games_played"
        private const val KEY_TOTAL_COINS = "total_coins"
        private const val KEY_HIGH_SCORE = "high_score"
        private const val KEY_HIGH_SCORE_BLITZ = "high_score_blitz"
        private const val KEY_HIGH_SCORE_SURVIVAL = "high_score_survival"
        private const val KEY_BEST_COMBO = "best_combo"
        private const val KEY_PERFECT_RUNS = "perfect_runs"
        private const val KEY_TOTAL_PLAY_TIME = "total_play_time"
        private const val KEY_POWERUPS_COLLECTED = "powerups_collected"
        private const val KEY_MP_WINS = "mp_wins"
        private const val KEY_MP_TOP10 = "mp_top10"
        private const val KEY_MP_GAMES = "mp_games"
        private const val KEY_BR_WINS = "br_wins"

        private const val KEY_SELECTED_THEME = "selected_theme"
        private const val KEY_SELECTED_SKIN = "selected_skin"
        private const val KEY_UNLOCKED_THEMES = "unlocked_themes"
        private const val KEY_UNLOCKED_SKINS = "unlocked_skins"
        private const val KEY_EARNED_ACHIEVEMENTS = "earned_achievements"
        private const val KEY_CHALLENGE_PROGRESS = "challenge_progress"

        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        private const val KEY_MASTER_VOLUME = "master_volume"

        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    // ==================== Profile ====================

    fun saveProfile(profile: PlayerProfile) {
        settings[KEY_USER_ID] = profile.odId
        settings[KEY_DISPLAY_NAME] = profile.displayName
        settings[KEY_AVATAR_URL] = profile.avatarUrl ?: ""
        settings[KEY_LEVEL] = profile.level
        settings[KEY_XP] = profile.xp
        settings[KEY_PRESTIGE] = profile.prestige
    }

    fun loadProfile(): PlayerProfile {
        return PlayerProfile(
            odId = settings[KEY_USER_ID, ""],
            displayName = settings[KEY_DISPLAY_NAME, "Player"],
            avatarUrl = settings.getStringOrNull(KEY_AVATAR_URL)?.takeIf { it.isNotEmpty() },
            level = settings[KEY_LEVEL, 1],
            xp = settings[KEY_XP, 0],
            prestige = settings[KEY_PRESTIGE, 0]
        )
    }

    // ==================== Stats ====================

    fun saveStats(stats: PlayerStats) {
        settings[KEY_GAMES_PLAYED] = stats.gamesPlayed
        settings[KEY_TOTAL_COINS] = stats.totalCoins
        settings[KEY_HIGH_SCORE] = stats.highScore
        settings[KEY_BEST_COMBO] = stats.bestCombo
        settings[KEY_PERFECT_RUNS] = stats.perfectRuns
        settings[KEY_TOTAL_PLAY_TIME] = stats.totalPlayTime
        settings[KEY_POWERUPS_COLLECTED] = stats.powerUpsCollected
        settings[KEY_MP_WINS] = stats.multiplayerWins
        settings[KEY_MP_TOP10] = stats.multiplayerTop10
        settings[KEY_MP_GAMES] = stats.multiplayerGamesPlayed
        settings[KEY_BR_WINS] = stats.battleRoyaleWins
    }

    fun loadStats(): PlayerStats {
        return PlayerStats(
            gamesPlayed = settings[KEY_GAMES_PLAYED, 0],
            totalCoins = settings[KEY_TOTAL_COINS, 0],
            highScore = settings[KEY_HIGH_SCORE, 0],
            bestCombo = settings[KEY_BEST_COMBO, 0],
            perfectRuns = settings[KEY_PERFECT_RUNS, 0],
            totalPlayTime = settings[KEY_TOTAL_PLAY_TIME, 0L],
            powerUpsCollected = settings[KEY_POWERUPS_COLLECTED, 0],
            multiplayerWins = settings[KEY_MP_WINS, 0],
            multiplayerTop10 = settings[KEY_MP_TOP10, 0],
            multiplayerGamesPlayed = settings[KEY_MP_GAMES, 0],
            battleRoyaleWins = settings[KEY_BR_WINS, 0]
        )
    }

    // ==================== High Scores ====================

    fun getHighScore(mode: GameMode = GameMode.CLASSIC): Int {
        return when (mode) {
            GameMode.CLASSIC -> settings[KEY_HIGH_SCORE, 0]
            GameMode.BLITZ -> settings[KEY_HIGH_SCORE_BLITZ, 0]
            GameMode.SURVIVAL -> settings[KEY_HIGH_SCORE_SURVIVAL, 0]
            else -> settings[KEY_HIGH_SCORE, 0]
        }
    }

    fun saveHighScore(score: Int, mode: GameMode = GameMode.CLASSIC) {
        when (mode) {
            GameMode.CLASSIC -> settings[KEY_HIGH_SCORE] = score
            GameMode.BLITZ -> settings[KEY_HIGH_SCORE_BLITZ] = score
            GameMode.SURVIVAL -> settings[KEY_HIGH_SCORE_SURVIVAL] = score
            else -> settings[KEY_HIGH_SCORE] = score
        }
    }

    fun getAllHighScores(): Map<GameMode, Int> {
        return mapOf(
            GameMode.CLASSIC to settings[KEY_HIGH_SCORE, 0],
            GameMode.BLITZ to settings[KEY_HIGH_SCORE_BLITZ, 0],
            GameMode.SURVIVAL to settings[KEY_HIGH_SCORE_SURVIVAL, 0]
        )
    }

    // ==================== Theme & Skin ====================

    fun saveSelectedTheme(themeId: ThemeId) {
        settings[KEY_SELECTED_THEME] = themeId.name
    }

    fun loadSelectedTheme(): ThemeId {
        val name = settings[KEY_SELECTED_THEME, ThemeId.CAMERA.name]
        return try {
            ThemeId.valueOf(name)
        } catch (e: Exception) {
            ThemeId.CAMERA
        }
    }

    fun saveSelectedSkin(skinId: CoinSkinId) {
        settings[KEY_SELECTED_SKIN] = skinId.name
    }

    fun loadSelectedSkin(): CoinSkinId {
        val name = settings[KEY_SELECTED_SKIN, CoinSkinId.CLASSIC.name]
        return try {
            CoinSkinId.valueOf(name)
        } catch (e: Exception) {
            CoinSkinId.CLASSIC
        }
    }

    // ==================== Achievements ====================

    fun saveAchievements(achievements: Set<String>) {
        settings[KEY_EARNED_ACHIEVEMENTS] = json.encodeToString(achievements.toList())
    }

    fun loadAchievements(): Set<String> {
        val jsonStr = settings[KEY_EARNED_ACHIEVEMENTS, "[]"]
        return try {
            json.decodeFromString<List<String>>(jsonStr).toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    // ==================== Challenge Progress ====================

    fun saveChallengeProgress(progress: Map<String, Int>) {
        settings[KEY_CHALLENGE_PROGRESS] = json.encodeToString(progress)
    }

    fun loadChallengeProgress(): Map<String, Int> {
        val jsonStr = settings[KEY_CHALLENGE_PROGRESS, "{}"]
        return try {
            json.decodeFromString<Map<String, Int>>(jsonStr)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // ==================== Settings ====================

    fun isSoundEnabled(): Boolean = settings[KEY_SOUND_ENABLED, true]
    fun setSoundEnabled(enabled: Boolean) { settings[KEY_SOUND_ENABLED] = enabled }

    fun isHapticEnabled(): Boolean = settings[KEY_HAPTIC_ENABLED, true]
    fun setHapticEnabled(enabled: Boolean) { settings[KEY_HAPTIC_ENABLED] = enabled }

    fun getMasterVolume(): Float = settings[KEY_MASTER_VOLUME, 1.0f]
    fun setMasterVolume(volume: Float) { settings[KEY_MASTER_VOLUME] = volume }

    // ==================== Auth Tokens ====================

    fun saveAuthToken(token: String) {
        settings[KEY_AUTH_TOKEN] = token
    }

    fun getAuthToken(): String? {
        return settings.getStringOrNull(KEY_AUTH_TOKEN)
    }

    fun saveRefreshToken(token: String) {
        settings[KEY_REFRESH_TOKEN] = token
    }

    fun getRefreshToken(): String? {
        return settings.getStringOrNull(KEY_REFRESH_TOKEN)
    }

    fun clearAuthTokens() {
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
    }

    // ==================== User ID ====================

    fun getUserId(): String? {
        val id = settings[KEY_USER_ID, ""]
        return id.takeIf { it.isNotEmpty() }
    }

    fun setUserId(userId: String) {
        settings[KEY_USER_ID] = userId
    }

    // ==================== Clear All ====================

    fun clearAll() {
        settings.clear()
    }
}
