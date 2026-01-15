package com.keren.virtualmoney.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keren.virtualmoney.game.ComboState

/**
 * Displays the current combo status.
 */
@Composable
fun ComboDisplay(
    comboState: ComboState,
    modifier: Modifier = Modifier
) {
    val isActive = comboState.isActive()
    val count = comboState.count

    // Scale animation based on combo count
    val scale by animateFloatAsState(
        targetValue = when {
            count >= 20 -> 1.3f
            count >= 10 -> 1.2f
            count >= 5 -> 1.1f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // Pulse animation for high combos
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (count >= 10) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        )
    )

    AnimatedVisibility(
        visible = isActive && count >= 3,
        enter = scaleIn(initialScale = 0.5f) + fadeIn(),
        exit = scaleOut(targetScale = 0.5f) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .scale(scale * pulse)
                .clip(RoundedCornerShape(12.dp))
                .background(getComboGradient(count))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Combo tier name
                comboState.tierName()?.let { tierName ->
                    Text(
                        text = tierName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Combo count
                Text(
                    text = "x$count",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                // Multiplier
                if (comboState.multiplier() > 1f) {
                    Text(
                        text = "+${((comboState.multiplier() - 1) * 100).toInt()}% bonus",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time remaining bar
                LinearProgressIndicator(
                    progress = { comboState.timeRemainingFraction() },
                    modifier = Modifier
                        .width(80.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

/**
 * Popup shown when combo milestones are reached.
 */
@Composable
fun ComboMilestonePopup(
    comboCount: Int,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(initialScale = 0.3f) + fadeIn(),
        exit = scaleOut(targetScale = 1.5f) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(getComboGradient(comboCount))
                .padding(horizontal = 32.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = getMilestoneEmoji(comboCount),
                    fontSize = 40.sp
                )
                Text(
                    text = "${comboCount}x COMBO!",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Floating "+points" text animation.
 */
@Composable
fun FloatingPointsText(
    points: Int,
    comboMultiplier: Float,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val offsetY by animateFloatAsState(
        targetValue = if (visible) -50f else 0f,
        animationSpec = tween(500)
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 0f else 1f,
        animationSpec = tween(500)
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.offset(y = offsetY.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "+$points",
                color = if (points > 0) Color(0xFFFFD700) else Color(0xFFFF4444),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            if (comboMultiplier > 1f) {
                Text(
                    text = "x${(comboMultiplier * 10).toInt() / 10.0}",
                    color = Color(0xFFFF9800),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun getComboGradient(count: Int): Brush = when {
    count >= 20 -> Brush.linearGradient(
        colors = listOf(Color(0xFFFF4081), Color(0xFFFF1744)) // Pink/Red - Legendary
    )
    count >= 10 -> Brush.linearGradient(
        colors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00)) // Gold/Orange - Amazing
    )
    count >= 5 -> Brush.linearGradient(
        colors = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32)) // Green - Great
    )
    else -> Brush.linearGradient(
        colors = listOf(Color(0xFF2196F3), Color(0xFF1565C0)) // Blue - Combo
    )
}

private fun getMilestoneEmoji(count: Int): String = when {
    count >= 30 -> "🌟"
    count >= 20 -> "🔥"
    count >= 10 -> "⚡"
    count >= 5 -> "✨"
    else -> "💫"
}
