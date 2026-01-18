package com.keren.virtualmoney.multiplayer

import com.keren.virtualmoney.game.CoinType
import kotlinx.serialization.Serializable

/**
 * Shared coin data synchronized across all players in multiplayer. All players see the same coins
 * at the same positions.
 */
@Serializable
data class SharedCoin(
        val id: String = "",
        val type: String = CoinType.BANK_HAPOALIM.name,
        val x: Float = 0f,
        val y: Float = 0f,
        val z: Float = 0f,
        val scale: Float = 1.0f,
        val spawnTime: Long = 0,
        val collectedBy: String? = null, // null = available, or playerId who collected it
        val expiresAt: Long = 0 // When to remove this coin from database
) {
    fun getCoinType(): CoinType {
        return try {
            CoinType.valueOf(type)
        } catch (e: Exception) {
            CoinType.BANK_HAPOALIM
        }
    }

    fun isAvailable(): Boolean = collectedBy == null

    fun isExpired(currentTime: Long): Boolean = currentTime > expiresAt
}

/** Result of coin collection attempt. */
@Serializable
data class CoinCollectionResult(
        val success: Boolean = false,
        val points: Int = 0,
        val message: String = "",
        val collectedBy: String? = null
)
