package com.keren.virtualmoney.backend

import com.keren.virtualmoney.progression.PlayerProfile
import com.keren.virtualmoney.progression.PlayerStats
import com.keren.virtualmoney.theme.CoinSkinId
import com.keren.virtualmoney.theme.ThemeId
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import com.keren.virtualmoney.platform.getCurrentTimeMillis

/**
 * Firestore document model for user profile.
 */
@Serializable
data class UserDocument(
    val displayName: String = "Player",
    val avatarUrl: String? = null,
    val level: Int = 1,
    val xp: Int = 0,
    val prestige: Int = 0,
    val selectedTheme: String = "CAMERA",
    val selectedSkin: String = "CLASSIC",
    val createdAt: Long = 0,
    val lastLogin: Long = 0
)

/**
 * Firestore document model for user stats.
 */
@Serializable
data class StatsDocument(
    val gamesPlayed: Int = 0,
    val totalCoins: Int = 0,
    val highScore: Int = 0,
    val bestCombo: Int = 0,
    val perfectRuns: Int = 0,
    val totalPlayTime: Long = 0,
    val powerUpsCollected: Int = 0,
    val multiplayerWins: Int = 0,
    val multiplayerTop10: Int = 0,
    val multiplayerGamesPlayed: Int = 0,
    val battleRoyaleWins: Int = 0
)

/**
 * Repository for managing user profile and stats in Firestore.
 * Provides offline-first functionality with local caching.
 */
class ProfileRepository(
    private val authManager: AuthManager,
    private val localStorage: LocalStorage
) {
    private val firestore = Firebase.firestore

    private fun usersCollection() = firestore.collection("users")
    private fun userDoc(userId: String) = usersCollection().document(userId)
    private fun statsDoc(userId: String) = userDoc(userId).collection("stats").document("global")

    /**
     * Save profile to both local storage and Firestore.
     */
    suspend fun saveProfile(profile: PlayerProfile): Result<Unit> {
        return try {
            // Always save locally first (offline-first)
            localStorage.saveProfile(profile)

            // Then sync to cloud if signed in
            val userId = authManager.getUserId()
            if (userId != null) {
                val doc = UserDocument(
                    displayName = profile.displayName,
                    avatarUrl = profile.avatarUrl,
                    level = profile.level,
                    xp = profile.xp,
                    prestige = profile.prestige,
                    selectedTheme = localStorage.loadSelectedTheme().name,
                    selectedSkin = localStorage.loadSelectedSkin().name,
                    createdAt = profile.createdAt,
                    lastLogin = profile.lastLogin
                )
                userDoc(userId).set(doc)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            // Local save succeeded, cloud sync can be retried
            Result.failure(e)
        }
    }

    /**
     * Load profile from local storage, with cloud sync if available.
     */
    suspend fun loadProfile(): PlayerProfile {
        // Load local first
        var profile = localStorage.loadProfile()

        // Try to sync from cloud
        try {
            val userId = authManager.getUserId()
            if (userId != null) {
                val docSnapshot = userDoc(userId).get()
                if (docSnapshot.exists) {
                    val cloudDoc = docSnapshot.data<UserDocument>()
                    profile = PlayerProfile(
                        odId = userId,
                        displayName = cloudDoc.displayName,
                        avatarUrl = cloudDoc.avatarUrl,
                        level = cloudDoc.level,
                        xp = cloudDoc.xp,
                        prestige = cloudDoc.prestige,
                        createdAt = cloudDoc.createdAt,
                        lastLogin = cloudDoc.lastLogin
                    )
                    // Update local cache
                    localStorage.saveProfile(profile)
                }
            }
        } catch (e: Exception) {
            // Use local data on cloud failure
        }

        return profile
    }

    /**
     * Save stats to both local storage and Firestore.
     */
    suspend fun saveStats(stats: PlayerStats): Result<Unit> {
        return try {
            localStorage.saveStats(stats)

            val userId = authManager.getUserId()
            if (userId != null) {
                val doc = StatsDocument(
                    gamesPlayed = stats.gamesPlayed,
                    totalCoins = stats.totalCoins,
                    highScore = stats.highScore,
                    bestCombo = stats.bestCombo,
                    perfectRuns = stats.perfectRuns,
                    totalPlayTime = stats.totalPlayTime,
                    powerUpsCollected = stats.powerUpsCollected,
                    multiplayerWins = stats.multiplayerWins,
                    multiplayerTop10 = stats.multiplayerTop10,
                    multiplayerGamesPlayed = stats.multiplayerGamesPlayed,
                    battleRoyaleWins = stats.battleRoyaleWins
                )
                statsDoc(userId).set(doc)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Load stats from local storage with cloud sync.
     */
    suspend fun loadStats(): PlayerStats {
        var stats = localStorage.loadStats()

        try {
            val userId = authManager.getUserId()
            if (userId != null) {
                val docSnapshot = statsDoc(userId).get()
                if (docSnapshot.exists) {
                    val cloudDoc = docSnapshot.data<StatsDocument>()
                    stats = PlayerStats(
                        gamesPlayed = cloudDoc.gamesPlayed,
                        totalCoins = cloudDoc.totalCoins,
                        highScore = cloudDoc.highScore,
                        bestCombo = cloudDoc.bestCombo,
                        perfectRuns = cloudDoc.perfectRuns,
                        totalPlayTime = cloudDoc.totalPlayTime,
                        powerUpsCollected = cloudDoc.powerUpsCollected,
                        multiplayerWins = cloudDoc.multiplayerWins,
                        multiplayerTop10 = cloudDoc.multiplayerTop10,
                        multiplayerGamesPlayed = cloudDoc.multiplayerGamesPlayed,
                        battleRoyaleWins = cloudDoc.battleRoyaleWins
                    )
                    localStorage.saveStats(stats)
                }
            }
        } catch (e: Exception) {
            // Use local data
        }

        return stats
    }

    /**
     * Save achievements.
     */
    suspend fun saveAchievements(achievements: Set<String>): Result<Unit> {
        return try {
            localStorage.saveAchievements(achievements)

            val userId = authManager.getUserId()
            if (userId != null) {
                val achievementsDoc = userDoc(userId).collection("achievements").document("earned")
                achievementsDoc.set(mapOf("ids" to achievements.toList()))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Load achievements.
     */
    suspend fun loadAchievements(): Set<String> {
        var achievements = localStorage.loadAchievements()

        try {
            val userId = authManager.getUserId()
            if (userId != null) {
                val docSnapshot = userDoc(userId).collection("achievements").document("earned").get()
                if (docSnapshot.exists) {
                    @Suppress("UNCHECKED_CAST")
                    val cloudIds = docSnapshot.get<List<String>>("ids")
                    achievements = cloudIds.toSet()
                    localStorage.saveAchievements(achievements)
                }
            }
        } catch (e: Exception) {
            // Use local data
        }

        return achievements
    }

    /**
     * Save challenge progress.
     */
    suspend fun saveChallengeProgress(progress: Map<String, Int>): Result<Unit> {
        return try {
            localStorage.saveChallengeProgress(progress)
            // Challenge progress is primarily local, synced through challenges system
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Load challenge progress.
     */
    fun loadChallengeProgress(): Map<String, Int> {
        return localStorage.loadChallengeProgress()
    }

    /**
     * Create initial profile for new user.
     */
    suspend fun createInitialProfile(userId: String, displayName: String?): PlayerProfile {
        val now = getCurrentTimeMillis()
        val profile = PlayerProfile(
            odId = userId,
            displayName = displayName ?: "Player",
            level = 1,
            xp = 0,
            prestige = 0,
            createdAt = now,
            lastLogin = now
        )

        saveProfile(profile)
        saveStats(PlayerStats())

        return profile
    }

    /**
     * Update last login timestamp.
     */
    suspend fun updateLastLogin() {
        val profile = loadProfile()
        val updated = profile.copy(
            lastLogin = getCurrentTimeMillis()
        )
        saveProfile(updated)
    }
}
