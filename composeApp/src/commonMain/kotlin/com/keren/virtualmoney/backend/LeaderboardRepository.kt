package com.keren.virtualmoney.backend

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.keren.virtualmoney.platform.getCurrentTimeMillis
import kotlinx.serialization.Serializable

/**
 * Leaderboard entry data.
 */
@Serializable
data class LeaderboardEntry(
    val odId: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val score: Int = 0,
    val level: Int = 1,
    val rank: Int = 0,
    val timestamp: Long = 0
)

/**
 * Leaderboard types.
 */
enum class LeaderboardType(val path: String) {
    DAILY("daily"),
    WEEKLY("weekly"),
    ALL_TIME("allTime"),
    MULTIPLAYER("multiplayer")
}

/**
 * Repository for managing leaderboards in Firestore.
 */
class LeaderboardRepository(
    private val authManager: AuthManager
) {
    private val firestore = Firebase.firestore

    private fun leaderboardsCollection() = firestore.collection("leaderboards")

    /**
     * Get today's date key for daily leaderboard.
     */
    private fun getTodayKey(): String {
        // Get today's date from milliseconds
        val now = getCurrentTimeMillis()
        val daysSinceEpoch = now / (24 * 60 * 60 * 1000)
        // Simple date key based on days since epoch
        return "day-$daysSinceEpoch"
    }

    /**
     * Get current week key for weekly leaderboard.
     */
    private fun getWeekKey(): String {
        val now = getCurrentTimeMillis()
        val weeksSinceEpoch = now / (7 * 24 * 60 * 60 * 1000)
        return "week-$weeksSinceEpoch"
    }

    /**
     * Submit a score to the leaderboard.
     */
    suspend fun submitScore(
        score: Int,
        displayName: String,
        avatarUrl: String?,
        level: Int
    ): Result<Unit> {
        return try {
            val userId = authManager.getUserId() ?: throw Exception("Not signed in")
            val timestamp = getCurrentTimeMillis()

            val entry = LeaderboardEntry(
                odId = userId,
                displayName = displayName,
                avatarUrl = avatarUrl,
                score = score,
                level = level,
                timestamp = timestamp
            )

            // Submit to daily leaderboard
            val todayKey = getTodayKey()
            val dailyRef = leaderboardsCollection()
                .document(LeaderboardType.DAILY.path)
                .collection(todayKey)
                .document(userId)

            // Only update if score is higher
            val existingDaily = try {
                dailyRef.get().data<LeaderboardEntry>()
            } catch (e: Exception) {
                null
            }

            if (existingDaily == null || score > existingDaily.score) {
                dailyRef.set(entry)
            }

            // Submit to weekly leaderboard
            val weekKey = getWeekKey()
            val weeklyRef = leaderboardsCollection()
                .document(LeaderboardType.WEEKLY.path)
                .collection(weekKey)
                .document(userId)

            val existingWeekly = try {
                weeklyRef.get().data<LeaderboardEntry>()
            } catch (e: Exception) {
                null
            }

            if (existingWeekly == null || score > existingWeekly.score) {
                weeklyRef.set(entry)
            }

            // Submit to all-time leaderboard
            val allTimeRef = leaderboardsCollection()
                .document(LeaderboardType.ALL_TIME.path)
                .collection("scores")
                .document(userId)

            val existingAllTime = try {
                allTimeRef.get().data<LeaderboardEntry>()
            } catch (e: Exception) {
                null
            }

            if (existingAllTime == null || score > existingAllTime.score) {
                allTimeRef.set(entry)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get top scores for a leaderboard type.
     */
    suspend fun getTopScores(
        type: LeaderboardType,
        limit: Int = 100
    ): Result<List<LeaderboardEntry>> {
        return try {
            val collectionPath = when (type) {
                LeaderboardType.DAILY -> "${type.path}/${getTodayKey()}"
                LeaderboardType.WEEKLY -> "${type.path}/${getWeekKey()}"
                LeaderboardType.ALL_TIME -> "${type.path}/scores"
                LeaderboardType.MULTIPLAYER -> "${type.path}/current_season"
            }

            val snapshot = leaderboardsCollection()
                .document(collectionPath.substringBefore("/"))
                .collection(collectionPath.substringAfter("/"))
                .orderBy("score", Direction.DESCENDING)
                .limit(limit)
                .get()

            val entries = snapshot.documents.mapIndexed { index, doc ->
                val entry = doc.data<LeaderboardEntry>()
                entry.copy(rank = index + 1)
            }

            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current user's rank on a leaderboard.
     */
    suspend fun getUserRank(type: LeaderboardType): Result<LeaderboardEntry?> {
        return try {
            val userId = authManager.getUserId() ?: throw Exception("Not signed in")

            val collectionPath = when (type) {
                LeaderboardType.DAILY -> "${type.path}/${getTodayKey()}"
                LeaderboardType.WEEKLY -> "${type.path}/${getWeekKey()}"
                LeaderboardType.ALL_TIME -> "${type.path}/scores"
                LeaderboardType.MULTIPLAYER -> "${type.path}/current_season"
            }

            // Get user's entry
            val userDoc = leaderboardsCollection()
                .document(collectionPath.substringBefore("/"))
                .collection(collectionPath.substringAfter("/"))
                .document(userId)
                .get()

            if (!userDoc.exists) {
                return Result.success(null)
            }

            val userEntry = userDoc.data<LeaderboardEntry>()

            // Count how many scores are higher to get rank
            val higherScores = leaderboardsCollection()
                .document(collectionPath.substringBefore("/"))
                .collection(collectionPath.substringAfter("/"))
                .where { "score" greaterThan userEntry.score }
                .get()

            val rank = higherScores.documents.size + 1

            Result.success(userEntry.copy(rank = rank))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get leaderboard entries around the current user.
     */
    suspend fun getEntriesAroundUser(
        type: LeaderboardType,
        range: Int = 5
    ): Result<List<LeaderboardEntry>> {
        return try {
            // First get user's rank
            val userRankResult = getUserRank(type)
            if (userRankResult.isFailure) {
                return Result.failure(userRankResult.exceptionOrNull()!!)
            }

            val userEntry = userRankResult.getOrNull()
                ?: return getTopScores(type, range * 2)

            // Get entries around user's score
            val collectionPath = when (type) {
                LeaderboardType.DAILY -> "${type.path}/${getTodayKey()}"
                LeaderboardType.WEEKLY -> "${type.path}/${getWeekKey()}"
                LeaderboardType.ALL_TIME -> "${type.path}/scores"
                LeaderboardType.MULTIPLAYER -> "${type.path}/current_season"
            }

            // Get entries with scores around user
            val snapshot = leaderboardsCollection()
                .document(collectionPath.substringBefore("/"))
                .collection(collectionPath.substringAfter("/"))
                .orderBy("score", Direction.DESCENDING)
                .get()

            val allEntries = snapshot.documents.mapIndexed { index, doc ->
                val entry = doc.data<LeaderboardEntry>()
                entry.copy(rank = index + 1)
            }

            // Find user's position and get surrounding entries
            val userIndex = allEntries.indexOfFirst { it.odId == userEntry.odId }
            if (userIndex == -1) {
                return Result.success(allEntries.take(range * 2))
            }

            val startIndex = maxOf(0, userIndex - range)
            val endIndex = minOf(allEntries.size, userIndex + range + 1)

            Result.success(allEntries.subList(startIndex, endIndex))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Flow of top scores (for real-time updates).
     */
    fun observeTopScores(
        type: LeaderboardType,
        limit: Int = 10
    ): Flow<List<LeaderboardEntry>> = flow {
        // For now, just emit once. Real-time listeners can be added later.
        val result = getTopScores(type, limit)
        if (result.isSuccess) {
            emit(result.getOrNull() ?: emptyList())
        } else {
            emit(emptyList())
        }
    }
}
