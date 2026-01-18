package com.keren.virtualmoney.ui.particles

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay

/**
 * Composable that renders particle effects overlay. This should be placed as the topmost layer in
 * the game UI.
 */
@Composable
fun ParticleEffectOverlay(particleManager: ParticleSystemManager, modifier: Modifier = Modifier) {
    // Force recomposition on each frame
    var frameCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60 FPS
            particleManager.update()
            frameCount++
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val allParticles = particleManager.getAllActiveParticles()

        allParticles.forEach { (effect, particle) ->
            val position = particle.getCurrentPosition()
            val alpha = particle.getAlpha()
            val rotation = particle.getCurrentRotation()

            // Draw particle as a circle
            rotate(degrees = rotation, pivot = position) {
                drawCircle(
                        color = particle.color.copy(alpha = alpha),
                        radius = particle.size,
                        center = position
                )
            }
        }
    }
}

/** Helper extension function to spawn effects easily. */
fun ParticleSystemManager.spawnCoinCollect(position: Offset) {
    spawnEffect(ParticleEffect.coinCollect(position))
}

fun ParticleSystemManager.spawnPenaltyHit(position: Offset) {
    spawnEffect(ParticleEffect.penaltyHit(position))
}

fun ParticleSystemManager.spawnPowerUpCollect(position: Offset) {
    spawnEffect(ParticleEffect.powerUpCollect(position))
}

fun ParticleSystemManager.spawnComboMilestone(position: Offset, comboCount: Int) {
    spawnEffect(ParticleEffect.comboMilestone(position, comboCount))
}

fun ParticleSystemManager.spawnLevelUp(position: Offset) {
    spawnEffect(ParticleEffect.levelUp(position))
}

fun ParticleSystemManager.spawnAchievement(position: Offset) {
    spawnEffect(ParticleEffect.achievement(position))
}
