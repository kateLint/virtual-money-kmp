package com.keren.virtualmoney.ui.particles

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.keren.virtualmoney.platform.getCurrentTimeMillis
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Types of particle effects in the game. */
enum class ParticleEffectType {
    COIN_COLLECT, // Gold sparkles when collecting good coins
    COIN_PENALTY, // Red explosion when hitting penalty coins
    POWER_UP_COLLECT, // Rainbow burst for power-ups
    COMBO_MILESTONE, // Special effect for combo achievements
    LEVEL_UP, // Celebration particles for leveling up
    ACHIEVEMENT // Fireworks for achievements
}

/** Individual particle in an effect. */
data class Particle(
        val id: String,
        val startPosition: Offset,
        val velocity: Offset,
        val color: Color,
        val size: Float,
        val lifetime: Long,
        val spawnTime: Long = getCurrentTimeMillis(),
        val rotation: Float = 0f,
        val rotationSpeed: Float = 0f,
        val gravity: Float = 0f,
        val fadeOut: Boolean = true
) {
    /** Calculate current position based on elapsed time. */
    fun getCurrentPosition(): Offset {
        val elapsed = (getCurrentTimeMillis() - spawnTime) / 1000f
        val gravityOffset = if (gravity != 0f) 0.5f * gravity * elapsed * elapsed else 0f

        return Offset(
                x = startPosition.x + velocity.x * elapsed,
                y = startPosition.y + velocity.y * elapsed + gravityOffset
        )
    }

    /** Calculate current rotation. */
    fun getCurrentRotation(): Float {
        val elapsed = (getCurrentTimeMillis() - spawnTime) / 1000f
        return rotation + rotationSpeed * elapsed
    }

    /** Calculate alpha (transparency) based on lifetime. */
    fun getAlpha(): Float {
        if (!fadeOut) return 1f
        val elapsed = getCurrentTimeMillis() - spawnTime
        return (1f - (elapsed.toFloat() / lifetime)).coerceIn(0f, 1f)
    }

    /** Check if particle has expired. */
    fun isExpired(): Boolean {
        return getCurrentTimeMillis() - spawnTime > lifetime
    }

    companion object {
        fun generateId(): String = "particle_${getCurrentTimeMillis()}_${Random.nextInt()}"
    }
}

/** Particle effect - collection of particles. */
data class ParticleEffect(
        val id: String,
        val type: ParticleEffectType,
        val position: Offset,
        val particles: List<Particle>,
        val spawnTime: Long = getCurrentTimeMillis(),
        val duration: Long = 2000L
) {
    /** Get active (non-expired) particles. */
    fun getActiveParticles(): List<Particle> {
        return particles.filterNot { it.isExpired() }
    }

    /** Check if effect has finished. */
    fun isFinished(): Boolean {
        return getCurrentTimeMillis() - spawnTime > duration || particles.all { it.isExpired() }
    }

    companion object {
        /** Create a coin collection effect (gold sparkles). */
        fun coinCollect(position: Offset): ParticleEffect {
            val particleCount = 15
            val particles =
                    (1..particleCount).map { i ->
                        val angle = (i.toFloat() / particleCount) * 2 * PI
                        val speed = Random.nextFloat() * 200f + 100f

                        Particle(
                                id = Particle.generateId(),
                                startPosition = position,
                                velocity =
                                        Offset(
                                                x = (cos(angle) * speed).toFloat(),
                                                y = (sin(angle) * speed).toFloat()
                                        ),
                                color = Color(0xFFFFD700), // Gold
                                size = Random.nextFloat() * 8f + 4f,
                                lifetime = Random.nextLong(800, 1500),
                                rotation = Random.nextFloat() * 360f,
                                rotationSpeed = Random.nextFloat() * 720f - 360f,
                                gravity = 500f,
                                fadeOut = true
                        )
                    }

            return ParticleEffect(
                    id = "effect_${getCurrentTimeMillis()}",
                    type = ParticleEffectType.COIN_COLLECT,
                    position = position,
                    particles = particles,
                    duration = 1500L
            )
        }

        /** Create a penalty effect (red explosion). */
        fun penaltyHit(position: Offset): ParticleEffect {
            val particleCount = 20
            val particles =
                    (1..particleCount).map { i ->
                        val angle = (i.toFloat() / particleCount) * 2 * PI
                        val speed = Random.nextFloat() * 300f + 150f

                        Particle(
                                id = Particle.generateId(),
                                startPosition = position,
                                velocity =
                                        Offset(
                                                x = (cos(angle) * speed).toFloat(),
                                                y = (sin(angle) * speed).toFloat()
                                        ),
                                color = Color(0xFFFF3333), // Red
                                size = Random.nextFloat() * 10f + 5f,
                                lifetime = Random.nextLong(600, 1200),
                                rotation = Random.nextFloat() * 360f,
                                rotationSpeed = Random.nextFloat() * 1080f - 540f,
                                gravity = 600f,
                                fadeOut = true
                        )
                    }

            return ParticleEffect(
                    id = "effect_${getCurrentTimeMillis()}",
                    type = ParticleEffectType.COIN_PENALTY,
                    position = position,
                    particles = particles,
                    duration = 1200L
            )
        }

        /** Create a power-up collection effect (rainbow burst). */
        fun powerUpCollect(position: Offset): ParticleEffect {
            val colors =
                    listOf(
                            Color(0xFFFF0000), // Red
                            Color(0xFFFF7F00), // Orange
                            Color(0xFFFFFF00), // Yellow
                            Color(0xFF00FF00), // Green
                            Color(0xFF0000FF), // Blue
                            Color(0xFF4B0082), // Indigo
                            Color(0xFF9400D3) // Violet
                    )

            val particleCount = 25
            val particles =
                    (1..particleCount).map { i ->
                        val angle = (i.toFloat() / particleCount) * 2 * PI
                        val speed = Random.nextFloat() * 250f + 150f

                        Particle(
                                id = Particle.generateId(),
                                startPosition = position,
                                velocity =
                                        Offset(
                                                x = (cos(angle) * speed).toFloat(),
                                                y = (sin(angle) * speed).toFloat()
                                        ),
                                color = colors[i % colors.size],
                                size = Random.nextFloat() * 12f + 6f,
                                lifetime = Random.nextLong(1000, 1800),
                                rotation = Random.nextFloat() * 360f,
                                rotationSpeed = Random.nextFloat() * 900f - 450f,
                                gravity = 400f,
                                fadeOut = true
                        )
                    }

            return ParticleEffect(
                    id = "effect_${getCurrentTimeMillis()}",
                    type = ParticleEffectType.POWER_UP_COLLECT,
                    position = position,
                    particles = particles,
                    duration = 1800L
            )
        }

        /** Create a combo milestone effect. */
        fun comboMilestone(position: Offset, comboCount: Int): ParticleEffect {
            val particleCount = 10 + (comboCount / 5) * 5 // More particles for higher combos
            val particles =
                    (1..particleCount).map { i ->
                        val angle = (i.toFloat() / particleCount) * 2 * PI
                        val speed = Random.nextFloat() * 180f + 120f

                        // Alternate between gold and white
                        val color = if (i % 2 == 0) Color(0xFFFFD700) else Color.White

                        Particle(
                                id = Particle.generateId(),
                                startPosition = position,
                                velocity =
                                        Offset(
                                                x = (cos(angle) * speed).toFloat(),
                                                y = (sin(angle) * speed).toFloat()
                                        ),
                                color = color,
                                size = Random.nextFloat() * 10f + 5f,
                                lifetime = Random.nextLong(1200, 2000),
                                rotation = Random.nextFloat() * 360f,
                                rotationSpeed = Random.nextFloat() * 1200f - 600f,
                                gravity = 300f,
                                fadeOut = true
                        )
                    }

            return ParticleEffect(
                    id = "effect_${getCurrentTimeMillis()}",
                    type = ParticleEffectType.COMBO_MILESTONE,
                    position = position,
                    particles = particles,
                    duration = 2000L
            )
        }

        /** Create a level-up celebration effect. */
        fun levelUp(position: Offset): ParticleEffect {
            val particleCount = 30
            val particles =
                    (1..particleCount).map { i ->
                        val angle = (i.toFloat() / particleCount) * 2 * PI
                        val speed = Random.nextFloat() * 300f + 200f

                        // Gold and cyan mix
                        val color = if (i % 3 == 0) Color(0xFFFFD700) else Color(0xFF00FFFF)

                        Particle(
                                id = Particle.generateId(),
                                startPosition = position,
                                velocity =
                                        Offset(
                                                x = (cos(angle) * speed).toFloat(),
                                                y = (sin(angle) * speed).toFloat()
                                        ),
                                color = color,
                                size = Random.nextFloat() * 14f + 8f,
                                lifetime = Random.nextLong(1500, 2500),
                                rotation = Random.nextFloat() * 360f,
                                rotationSpeed = Random.nextFloat() * 1440f - 720f,
                                gravity = 250f,
                                fadeOut = true
                        )
                    }

            return ParticleEffect(
                    id = "effect_${getCurrentTimeMillis()}",
                    type = ParticleEffectType.LEVEL_UP,
                    position = position,
                    particles = particles,
                    duration = 2500L
            )
        }

        /** Create an achievement fireworks effect. */
        fun achievement(position: Offset): ParticleEffect {
            val particleCount = 40
            val particles =
                    (1..particleCount).map { i ->
                        val angle = (i.toFloat() / particleCount) * 2 * PI
                        val speed = Random.nextFloat() * 350f + 200f

                        // Rainbow colors
                        val hue = (i.toFloat() / particleCount) * 360f
                        val color = Color.hsl(hue, 1f, 0.5f)

                        Particle(
                                id = Particle.generateId(),
                                startPosition = position,
                                velocity =
                                        Offset(
                                                x = (cos(angle) * speed).toFloat(),
                                                y = (sin(angle) * speed).toFloat()
                                        ),
                                color = color,
                                size = Random.nextFloat() * 16f + 10f,
                                lifetime = Random.nextLong(2000, 3000),
                                rotation = Random.nextFloat() * 360f,
                                rotationSpeed = Random.nextFloat() * 1800f - 900f,
                                gravity = 200f,
                                fadeOut = true
                        )
                    }

            return ParticleEffect(
                    id = "effect_${getCurrentTimeMillis()}",
                    type = ParticleEffectType.ACHIEVEMENT,
                    position = position,
                    particles = particles,
                    duration = 3000L
            )
        }
    }
}

/** Particle system manager - handles all active particle effects. */
class ParticleSystemManager {
    private val _activeEffects = mutableListOf<ParticleEffect>()
    val activeEffects: List<ParticleEffect>
        get() = _activeEffects.toList()

    /** Spawn a new particle effect. */
    fun spawnEffect(effect: ParticleEffect) {
        _activeEffects.add(effect)
    }

    /** Update and clean up expired effects. */
    fun update() {
        _activeEffects.removeAll { it.isFinished() }
    }

    /** Get all active particles from all effects. */
    fun getAllActiveParticles(): List<Pair<ParticleEffect, Particle>> {
        return _activeEffects.flatMap { effect ->
            effect.getActiveParticles().map { particle -> effect to particle }
        }
    }

    /** Clear all effects. */
    fun clear() {
        _activeEffects.clear()
    }
}
