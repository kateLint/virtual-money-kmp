package com.keren.virtualmoney.game

import com.keren.virtualmoney.ar.math.Vector3D
import com.keren.virtualmoney.platform.getCurrentTimeMillis
import kotlin.random.Random
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos

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
 * @param x Horizontal position (0.0 = left edge, 1.0 = right edge) - DEPRECATED: Use position3D for AR mode
 * @param y Vertical position (0.0 = top edge, 1.0 = bottom edge) - DEPRECATED: Use position3D for AR mode
 * @param scale Size multiplier (1.0 = normal size, smaller for increased difficulty)
 * @param type Type of coin (GOLD or BLACK)
 * @param spawnTime Timestamp when the coin was created (for auto-removal of black coins)
 * @param position3D 3D position in AR space (null for 2D mode, set for AR mode)
 */
data class Coin(
    val id: String,
    val x: Float,
    val y: Float,
    val scale: Float = 1.0f,
    val type: CoinType = CoinType.BANK_HAPOALIM,
    val spawnTime: Long = getCurrentTimeMillis(),
    val position3D: Vector3D? = null
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
         * Creates a random coin with 3D position in AR space.
         * Distributes coins in a hemisphere in front of the camera.
         *
         * @param distanceRange Range of distances from camera (min to max in meters)
         * @param scale Size multiplier (1.0 = normal size)
         * @param type Type of coin to create
         * @return Coin with 3D position set (x/y set to 0.5 as fallback for 2D rendering)
         */
        fun createRandom3D(
            distanceRange: ClosedFloatingPointRange<Float> = 1.5f..4.0f,
            scale: Float = 1.0f,
            type: CoinType = CoinType.BANK_HAPOALIM
        ): Coin {
            // Generate spherical coordinates
            val distance = Random.nextFloat() * (distanceRange.endInclusive - distanceRange.start) + distanceRange.start

            // Azimuth: -90° to +90° (180° horizontal field - full left to right)
            val azimuthDegrees = Random.nextFloat() * 180f - 90f
            val azimuthRadians = (azimuthDegrees * PI / 180.0).toFloat()

            // Elevation: EXTREME ceiling emphasis to force coins overhead
            // 70% CEILING (+40° to +85°) - MOST coins require looking UP!
            // 15% eye level (-15° to +40°)
            // 15% below eye level (-85° to -15°)
            val rand = Random.nextFloat()
            val elevationDegrees = when {
                rand < 0.7f -> Random.nextFloat() * 45f + 40f   // CEILING: 70% chance, very steep angles
                rand < 0.85f -> Random.nextFloat() * 55f - 15f  // Middle: 15% chance
                else -> Random.nextFloat() * 70f - 85f          // Lower: 15% chance
            }
            val elevationRadians = (elevationDegrees * PI / 180.0).toFloat()

            // Convert spherical to Cartesian coordinates
            // Camera space: +X = right, +Y = up, -Z = forward
            val x = distance * sin(azimuthRadians) * cos(elevationRadians)
            val y = distance * sin(elevationRadians)
            val z = -distance * cos(azimuthRadians) * cos(elevationRadians)

            return Coin(
                id = generateId(),
                x = 0.5f,  // Center screen as 2D fallback
                y = 0.5f,  // Center screen as 2D fallback
                scale = scale,
                type = type,
                position3D = Vector3D(x, y, z)
            )
        }

        /**
         * Generates a unique ID for coin tracking.
         */
        private fun generateId(): String {
            return "coin_${getCurrentTimeMillis()}_${Random.nextInt()}"
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
         * Returns the emoji/icon for each coin type.
         */
        fun getIcon(type: CoinType): String = when (type) {
            CoinType.BANK_HAPOALIM -> "🏛️"   // בנק הפועלים (good)
            CoinType.BANK_LEUMI -> "🏦"      // בנק לאומי (penalty)
            CoinType.BANK_MIZRAHI -> "💰"    // בנק מזרחי (penalty)
            CoinType.BANK_DISCOUNT -> "💳"   // בנק דיסקונט (penalty)
        }

        /**
         * Returns the drawable resource name for each coin type.
         * Now using actual bank logo PNG images.
         */
        fun getDrawableResource(type: CoinType): String = when (type) {
            CoinType.BANK_HAPOALIM -> "bank_hapoalim.png"   // בנק הפועלים (good)
            CoinType.BANK_LEUMI -> "bank_leumi.png"          // בנק לאומי (penalty)
            CoinType.BANK_MIZRAHI -> "bank_mizrahi.png"      // בנק מזרחי (penalty)
            CoinType.BANK_DISCOUNT -> "bank_discount.png"    // בנק דיסקונט (penalty)
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
