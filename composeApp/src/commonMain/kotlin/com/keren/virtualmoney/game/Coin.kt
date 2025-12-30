package com.keren.virtualmoney.game

import kotlin.random.Random

/**
 * Types of coins in the game.
 */
enum class CoinType {
    BANK_HAPOALIM,   // בנק הפועלים - Good coin (adds points)
    BANK_LEUMI,      // בנק לאומי - Penalty coin
    BANK_MIZRAHI,    // בנק מזרחי טפחות - Penalty coin
    BANK_DISCOUNT    // בנק דיסקונט - Penalty coin
}

/**
 * Represents a collectible coin in the game.
 * Coordinates are normalized (0.0 to 1.0) to support any screen resolution.
 *
 * @param id Unique identifier for the coin (for tap detection)
 * @param x Horizontal position (0.0 = left edge, 1.0 = right edge)
 * @param y Vertical position (0.0 = top edge, 1.0 = bottom edge)
 * @param scale Size multiplier (1.0 = normal size, smaller for increased difficulty)
 * @param type Type of coin (GOLD or BLACK)
 * @param spawnTime Timestamp when the coin was created (for auto-removal of black coins)
 */
data class Coin(
    val id: String,
    val x: Float,
    val y: Float,
    val scale: Float = 1.0f,
    val type: CoinType = CoinType.BANK_HAPOALIM,
    val spawnTime: Long = System.currentTimeMillis()
) {
    companion object {
        private const val SAFE_MARGIN = 0.1f // 10% margin from edges
        private const val HAPOALIM_COIN_VALUE = 10 // Points for Hapoalim coin
        private const val PENALTY_COIN_VALUE = -15 // Penalty for other bank coins
        const val PENALTY_COIN_LIFETIME_MS = 2000L // Penalty coins disappear after 2 seconds

        /**
         * Creates a random coin with normalized coordinates.
         * Ensures coins don't spawn too close to screen edges.
         * Returns Hapoalim coin by default (good coin).
         */
        fun createRandom(scale: Float = 1.0f): Coin {
            return Coin(
                id = generateId(),
                x = Random.nextFloat() * (1.0f - 2 * SAFE_MARGIN) + SAFE_MARGIN,
                y = Random.nextFloat() * (1.0f - 2 * SAFE_MARGIN) + SAFE_MARGIN,
                scale = scale,
                type = CoinType.BANK_HAPOALIM
            )
        }

        /**
         * Generates a unique ID for coin tracking.
         */
        private fun generateId(): String {
            return "coin_${System.currentTimeMillis()}_${Random.nextInt()}"
        }

        /**
         * Returns the point value for collecting a coin.
         */
        fun getValue(type: CoinType): Int = when (type) {
            CoinType.BANK_HAPOALIM -> HAPOALIM_COIN_VALUE
            CoinType.BANK_LEUMI,
            CoinType.BANK_MIZRAHI,
            CoinType.BANK_DISCOUNT -> PENALTY_COIN_VALUE
        }

        /**
         * Returns the drawable resource name for each coin type.
         */
        fun getDrawableResource(type: CoinType): String = when (type) {
            CoinType.BANK_HAPOALIM -> "bank_hapoalim.xml"   // בנק הפועלים (good)
            CoinType.BANK_LEUMI -> "bank_leumi.xml"          // בנק לאומי (penalty)
            CoinType.BANK_MIZRAHI -> "bank_mizrahi.xml"      // בנק מזרחי (penalty)
            CoinType.BANK_DISCOUNT -> "bank_discount.xml"    // בנק דיסקונט (penalty)
        }

        /**
         * Returns true if this is a penalty coin (not Hapoalim).
         */
        fun isPenaltyCoin(type: CoinType): Boolean = when (type) {
            CoinType.BANK_HAPOALIM -> false
            CoinType.BANK_LEUMI,
            CoinType.BANK_MIZRAHI,
            CoinType.BANK_DISCOUNT -> true
        }
    }
}
