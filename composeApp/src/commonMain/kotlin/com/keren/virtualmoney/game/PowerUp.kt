package com.keren.virtualmoney.game

import com.keren.virtualmoney.ar.math.Vector3D
import com.keren.virtualmoney.platform.getCurrentTimeMillis
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Types of power-ups available in the game.
 */
enum class PowerUpType(
    val displayName: String,
    val durationMs: Long,
    val spawnWeight: Float,
    val multiplayerOnly: Boolean
) {
    MAGNET("Magnet", 5000L, 0.15f, false),
    MULTIPLIER("2x Points", 10000L, 0.20f, false),
    SHIELD("Shield", 8000L, 0.20f, false),
    FREEZE("Freeze", 3000L, 0.10f, true),
    INVISIBILITY("Invisibility", 5000L, 0.10f, true);

    companion object {
        /**
         * Returns power-ups available for single player mode.
         */
        fun singlePlayerTypes(): List<PowerUpType> =
            entries.filter { !it.multiplayerOnly }

        /**
         * Returns all power-ups for multiplayer mode.
         */
        fun multiplayerTypes(): List<PowerUpType> = entries.toList()

        /**
         * Randomly selects a power-up type based on spawn weights.
         */
        fun randomWeighted(isMultiplayer: Boolean = false): PowerUpType {
            val availableTypes = if (isMultiplayer) multiplayerTypes() else singlePlayerTypes()
            val totalWeight = availableTypes.sumOf { it.spawnWeight.toDouble() }.toFloat()
            var random = Random.nextFloat() * totalWeight

            for (type in availableTypes) {
                random -= type.spawnWeight
                if (random <= 0) return type
            }
            return availableTypes.last()
        }
    }
}

/**
 * Represents a power-up collectible in the game world.
 */
data class PowerUp(
    val id: String,
    val type: PowerUpType,
    val position3D: Vector3D,
    val spawnTime: Long = getCurrentTimeMillis(),
    val expiresAt: Long = getCurrentTimeMillis() + LIFETIME_MS
) {
    companion object {
        const val LIFETIME_MS = 10000L // Power-ups despawn after 10 seconds if not collected

        /**
         * Creates a random power-up in 3D space.
         */
        fun createRandom3D(
            distanceRange: ClosedFloatingPointRange<Float> = 0.5f..1.2f,
            isMultiplayer: Boolean = false
        ): PowerUp {
            val distance = Random.nextFloat() * (distanceRange.endInclusive - distanceRange.start) + distanceRange.start

            // Azimuth: -90° to +90° (180° horizontal field)
            val azimuthDegrees = Random.nextFloat() * 180f - 90f
            val azimuthRadians = (azimuthDegrees * PI / 180.0).toFloat()

            // Elevation: Mostly eye level for easy collection
            val elevationDegrees = Random.nextFloat() * 60f - 20f // -20° to +40°
            val elevationRadians = (elevationDegrees * PI / 180.0).toFloat()

            // Convert spherical to Cartesian coordinates
            val x = distance * sin(azimuthRadians) * cos(elevationRadians)
            val y = distance * sin(elevationRadians)
            val z = -distance * cos(azimuthRadians) * cos(elevationRadians)

            return PowerUp(
                id = "powerup_${getCurrentTimeMillis()}_${Random.nextInt()}",
                type = PowerUpType.randomWeighted(isMultiplayer),
                position3D = Vector3D(x, y, z)
            )
        }

        /**
         * Returns the drawable resource name for each power-up type.
         */
        fun getDrawableResource(type: PowerUpType): String = when (type) {
            PowerUpType.MAGNET -> "powerup_magnet.png"
            PowerUpType.MULTIPLIER -> "powerup_multiplier.png"
            PowerUpType.SHIELD -> "powerup_shield.png"
            PowerUpType.FREEZE -> "powerup_freeze.png"
            PowerUpType.INVISIBILITY -> "powerup_invisibility.png"
        }

        /**
         * Returns the icon emoji for each power-up type (fallback).
         */
        fun getIcon(type: PowerUpType): String = when (type) {
            PowerUpType.MAGNET -> "🧲"
            PowerUpType.MULTIPLIER -> "✨"
            PowerUpType.SHIELD -> "🛡️"
            PowerUpType.FREEZE -> "❄️"
            PowerUpType.INVISIBILITY -> "👻"
        }
    }

    /**
     * Check if this power-up has expired (should be removed from world).
     */
    fun isExpired(): Boolean = getCurrentTimeMillis() >= expiresAt

    /**
     * Returns remaining time until expiration in milliseconds.
     */
    fun remainingLifetime(): Long = maxOf(0, expiresAt - getCurrentTimeMillis())
}

/**
 * Represents an active power-up effect on a player.
 */
data class ActivePowerUp(
    val type: PowerUpType,
    val startTime: Long = getCurrentTimeMillis(),
    val endTime: Long = getCurrentTimeMillis() + type.durationMs
) {
    /**
     * Returns remaining duration in milliseconds.
     */
    fun remainingTime(): Long = maxOf(0, endTime - getCurrentTimeMillis())

    /**
     * Returns remaining duration as a fraction (0.0 to 1.0).
     */
    fun remainingFraction(): Float {
        val total = type.durationMs.toFloat()
        val remaining = remainingTime().toFloat()
        return (remaining / total).coerceIn(0f, 1f)
    }

    /**
     * Check if this power-up effect has expired.
     */
    fun isExpired(): Boolean = getCurrentTimeMillis() >= endTime
}
