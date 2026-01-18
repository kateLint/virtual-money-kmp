package com.keren.virtualmoney.game

import com.keren.virtualmoney.ar.math.Vector3D
import com.keren.virtualmoney.platform.getCurrentTimeMillis
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Types of coins in the game. */
enum class CoinType {
        BANK_HAPOALIM, // בנק הפועלים - Good coin (adds points)
        BANK_LEUMI, // בנק לאומי - Penalty coin
        BANK_MIZRAHI, // בנק מזרחי טפחות - Penalty coin
        BANK_DISCOUNT // בנק דיסקונט - Penalty coin
}

/**
 * Represents a collectible coin in the game. Coordinates are normalized (0.0 to 1.0) to support any
 * screen resolution.
 *
 * @param id Unique identifier for the coin (for tap detection)
 * @param x Horizontal position (0.0 = left edge, 1.0 = right edge) - DEPRECATED: Use position3D for
 * AR mode
 * @param y Vertical position (0.0 = top edge, 1.0 = bottom edge) - DEPRECATED: Use position3D for
 * AR mode
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
                const val PENALTY_COIN_LIFETIME_MS =
                        2000L // Penalty coins disappear after 2 seconds

                /**
                 * Creates a random coin with normalized coordinates. Ensures coins don't spawn too
                 * close to screen edges. Returns Hapoalim coin by default (good coin).
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
                 * Creates a random coin with 3D position in AR space. Distributes coins in a FULL
                 * SPHERE around the player (360°). Player must physically move phone to look around
                 * and find coins!
                 *
                 * @param distanceRange Range of distances from camera (min to max in meters)
                 * @param scale Size multiplier (1.0 = normal size)
                 * @param type Type of coin to create
                 * @return Coin with 3D position set (x/y set randomly for 2D rendering fallback)
                 */
                fun createRandom3D(
                        distanceRange: ClosedFloatingPointRange<Float> = 1.5f..4.0f,
                        scale: Float = 1.0f,
                        type: CoinType = CoinType.BANK_HAPOALIM
                ): Coin {
                        // Generate spherical coordinates
                        val distance =
                                Random.nextFloat() *
                                        (distanceRange.endInclusive - distanceRange.start) +
                                        distanceRange.start

                        // FULL 360° horizontal spread - coins can be ANYWHERE around you!
                        // Player must turn around to find them
                        val azimuthDegrees =
                                Random.nextFloat() * 360f - 180f // -180° to +180° = full circle
                        val azimuthRadians = (azimuthDegrees * PI / 180.0).toFloat()

                        // Elevation: Spread coins from floor to ceiling
                        // 30% above (looking up): +20° to +60°
                        // 40% eye level: -20° to +20°
                        // 30% below (looking down): -60° to -20°
                        val rand = Random.nextFloat()
                        val elevationDegrees =
                                when {
                                        rand < 0.30f ->
                                                Random.nextFloat() * 40f +
                                                        20f // Above: 30% chance (+20° to +60°)
                                        rand < 0.70f ->
                                                Random.nextFloat() * 40f -
                                                        20f // Eye level: 40% chance (-20° to +20°)
                                        else ->
                                                Random.nextFloat() * 40f -
                                                        60f // Below: 30% chance (-60° to -20°)
                                }
                        val elevationRadians = (elevationDegrees * PI / 180.0).toFloat()

                        // Convert spherical to Cartesian coordinates
                        // Camera space: +X = right, +Y = up, -Z = forward
                        val x3D = distance * sin(azimuthRadians) * cos(elevationRadians)
                        val y3D = distance * sin(elevationRadians)
                        val z3D = -distance * cos(azimuthRadians) * cos(elevationRadians)

                        // Generate random 2D positions for fallback rendering
                        // Spread coins across the screen with proper padding
                        val x2D = Random.nextFloat() * (1.0f - 2 * SAFE_MARGIN) + SAFE_MARGIN
                        val y2D = Random.nextFloat() * (1.0f - 2 * SAFE_MARGIN) + SAFE_MARGIN

                        return Coin(
                                id = generateId(),
                                x = x2D, // Random 2D position for non-AR mode
                                y = y2D, // Random 2D position for non-AR mode
                                scale = scale,
                                type = type,
                                position3D = Vector3D(x3D, y3D, z3D)
                        )
                }

                /**
                 * Creates a coin spawned within a forward cone (for onboarding).
                 * @param distanceRange Range of distances in meters
                 * @param coneAngle Half-angle of the cone in degrees (e.g., 60 = ±60° = 120° total)
                 * @param type The coin type
                 */
                fun createRandomInCone(
                        distanceRange: ClosedFloatingPointRange<Float>,
                        coneAngle: Float = 60f,
                        scale: Float = 1.0f,
                        type: CoinType = CoinType.BANK_HAPOALIM
                ): Coin {
                        // Random distance within range
                        val distance =
                                Random.nextFloat() *
                                        (distanceRange.endInclusive - distanceRange.start) +
                                        distanceRange.start

                        // Azimuth limited to cone angle (±coneAngle from forward)
                        val azimuthDegrees = Random.nextFloat() * (coneAngle * 2) - coneAngle
                        val azimuthRadians = (azimuthDegrees * PI / 180.0).toFloat()

                        // Elevation - mostly at eye level for onboarding
                        val elevationDegrees = Random.nextFloat() * 40f - 20f // -20° to +20°
                        val elevationRadians = (elevationDegrees * PI / 180.0).toFloat()

                        // Convert spherical to Cartesian
                        val x3D = distance * sin(azimuthRadians) * cos(elevationRadians)
                        val y3D = distance * sin(elevationRadians)
                        val z3D = -distance * cos(azimuthRadians) * cos(elevationRadians)

                        // Random 2D fallback
                        val x2D = Random.nextFloat() * (1.0f - 2 * SAFE_MARGIN) + SAFE_MARGIN
                        val y2D = Random.nextFloat() * (1.0f - 2 * SAFE_MARGIN) + SAFE_MARGIN

                        return Coin(
                                id = generateId(),
                                x = x2D,
                                y = y2D,
                                scale = scale,
                                type = type,
                                position3D = Vector3D(x3D, y3D, z3D)
                        )
                }

                /** Generates a unique ID for coin tracking. */
                private fun generateId(): String {
                        return "coin_${getCurrentTimeMillis()}_${Random.nextInt()}"
                }

                /** Returns the point value for collecting a coin. */
                fun getValue(type: CoinType): Int =
                        when (type) {
                                CoinType.BANK_HAPOALIM -> HAPOALIM_COIN_VALUE
                                CoinType.BANK_LEUMI,
                                CoinType.BANK_MIZRAHI,
                                CoinType.BANK_DISCOUNT -> PENALTY_COIN_VALUE
                        }

                /** Returns the emoji/icon for each coin type. */
                fun getIcon(type: CoinType): String =
                        when (type) {
                                CoinType.BANK_HAPOALIM -> "🏛️" // בנק הפועלים (good)
                                CoinType.BANK_LEUMI -> "🏦" // בנק לאומי (penalty)
                                CoinType.BANK_MIZRAHI -> "💰" // בנק מזרחי (penalty)
                                CoinType.BANK_DISCOUNT -> "💳" // בנק דיסקונט (penalty)
                        }

                /**
                 * Returns the drawable resource name for each coin type. Now using actual bank logo
                 * PNG images.
                 */
                fun getDrawableResource(type: CoinType): String =
                        when (type) {
                                CoinType.BANK_HAPOALIM -> "bank_hapoalim.png" // בנק הפועלים (good)
                                CoinType.BANK_LEUMI -> "bank_leumi.png" // בנק לאומי (penalty)
                                CoinType.BANK_MIZRAHI -> "bank_mizrahi.png" // בנק מזרחי (penalty)
                                CoinType.BANK_DISCOUNT ->
                                        "bank_discount.png" // בנק דיסקונט (penalty)
                        }

                /** Returns true if this is a penalty coin (not Hapoalim). */
                fun isPenaltyCoin(type: CoinType): Boolean =
                        when (type) {
                                CoinType.BANK_HAPOALIM -> false
                                CoinType.BANK_LEUMI,
                                CoinType.BANK_MIZRAHI,
                                CoinType.BANK_DISCOUNT -> true
                        }
        }
}
