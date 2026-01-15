package com.keren.virtualmoney.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keren.virtualmoney.game.ActivePowerUp
import com.keren.virtualmoney.game.PowerUp
import com.keren.virtualmoney.game.PowerUpType

/**
 * Displays active power-ups in the game HUD.
 */
@Composable
fun PowerUpHUD(
    activePowerUps: List<ActivePowerUp>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        activePowerUps.forEach { powerUp ->
            ActivePowerUpIndicator(powerUp)
        }
    }
}

@Composable
private fun ActivePowerUpIndicator(powerUp: ActivePowerUp) {
    val remainingFraction by remember(powerUp) {
        derivedStateOf { powerUp.remainingFraction() }
    }

    // Pulsing animation when low on time
    val pulseAnim = rememberInfiniteTransition()
    val alpha by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (remainingFraction < 0.3f) 0.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        )
    )

    AnimatedVisibility(
        visible = !powerUp.isExpired(),
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(getPowerUpColor(powerUp.type).copy(alpha = 0.8f * alpha))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = PowerUp.getIcon(powerUp.type),
                fontSize = 16.sp
            )

            Column {
                Text(
                    text = powerUp.type.displayName.uppercase(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                LinearProgressIndicator(
                    progress = { remainingFraction },
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }

            Text(
                text = "${(powerUp.remainingTime() / 1000)}s",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Shows when a power-up is collected.
 */
@Composable
fun PowerUpCollectedPopup(
    powerUpType: PowerUpType?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = powerUpType != null,
        enter = scaleIn(initialScale = 0.5f) + fadeIn(),
        exit = scaleOut(targetScale = 1.5f) + fadeOut(),
        modifier = modifier
    ) {
        powerUpType?.let { type ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(getPowerUpColor(type))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = PowerUp.getIcon(type),
                        fontSize = 32.sp
                    )
                    Column {
                        Text(
                            text = type.displayName.uppercase(),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getPowerUpDescription(type),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

private fun getPowerUpColor(type: PowerUpType): Color = when (type) {
    PowerUpType.MAGNET -> Color(0xFF2196F3) // Blue
    PowerUpType.MULTIPLIER -> Color(0xFFFFD700) // Gold
    PowerUpType.SHIELD -> Color(0xFF4CAF50) // Green
    PowerUpType.FREEZE -> Color(0xFF00BCD4) // Cyan
    PowerUpType.INVISIBILITY -> Color(0xFF9C27B0) // Purple
}

private fun getPowerUpDescription(type: PowerUpType): String = when (type) {
    PowerUpType.MAGNET -> "Auto-collect nearby coins!"
    PowerUpType.MULTIPLIER -> "Double points!"
    PowerUpType.SHIELD -> "Protected from penalties!"
    PowerUpType.FREEZE -> "Opponents frozen!"
    PowerUpType.INVISIBILITY -> "Hidden from radar!"
}
