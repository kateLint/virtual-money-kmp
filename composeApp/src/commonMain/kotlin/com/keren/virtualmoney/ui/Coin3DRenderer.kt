package com.keren.virtualmoney.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import com.keren.virtualmoney.game.Coin
import com.keren.virtualmoney.game.CoinType
import com.keren.virtualmoney.game.PowerUp
import com.keren.virtualmoney.game.PowerUpType
import com.keren.virtualmoney.platform.getCurrentTimeMillis
import com.keren.virtualmoney.theme.CoinSkin
import com.keren.virtualmoney.theme.CoinSkinId
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * 3D-like coin renderer using Canvas with perspective effects.
 * Creates a realistic spinning coin with lighting, shadows, and depth.
 */
@Composable
fun Coin3D(
    coin: Coin,
    skin: CoinSkin = CoinSkin.CLASSIC,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(getCurrentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60fps
            currentTime = getCurrentTimeMillis()
        }
    }

    val coinAge = currentTime - coin.spawnTime
    val isExpiring = Coin.isPenaltyCoin(coin.type) &&
            coinAge > (Coin.PENALTY_COIN_LIFETIME_MS - 500)

    // 3D rotation animation
    val infiniteTransition = rememberInfiniteTransition()
    val rotationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Floating/bobbing animation
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Sparkle animation for special coins
    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Collect animation state
    var isCollected by remember { mutableStateOf(false) }
    val collectScale by animateFloatAsState(
        targetValue = if (isCollected) 2f else 1f,
        animationSpec = tween(300)
    )
    val collectAlpha by animateFloatAsState(
        targetValue = if (isCollected) 0f else 1f,
        animationSpec = tween(300)
    )

    // Expiring alpha
    val expiringAlpha = if (isExpiring) {
        val timeLeft = Coin.PENALTY_COIN_LIFETIME_MS - coinAge
        (timeLeft.toFloat() / 500f).coerceIn(0f, 1f)
    } else 1f

    val finalAlpha = collectAlpha * expiringAlpha

    Box(
        modifier = modifier
            .size(100.dp)
            .graphicsLayer {
                translationY = floatOffset
                scaleX = collectScale
                scaleY = collectScale
                alpha = finalAlpha
            }
            .clickable(
                onClick = {
                    isCollected = true
                    onTap()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = minOf(size.width, size.height) / 2.5f

            // Calculate 3D perspective based on Y rotation
            val radians = (rotationY * PI / 180.0).toFloat()
            val scaleX = abs(cos(radians))
            val isFrontFacing = cos(radians) > 0

            // Get colors based on coin type and skin
            val colors = getCoin3DColors(coin.type, skin)

            // Draw shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = radius * 0.9f,
                center = Offset(centerX + 5f, centerY + 8f + floatOffset),
                style = Fill
            )

            // Draw coin edge (thickness effect when rotated)
            if (scaleX < 0.95f) {
                val edgeWidth = radius * 2 * (1 - scaleX) * 0.15f
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(colors.edgeDark, colors.edge, colors.edgeDark)
                    ),
                    topLeft = Offset(centerX - edgeWidth / 2, centerY - radius),
                    size = Size(edgeWidth, radius * 2)
                )
            }

            // Draw main coin face
            val faceColor = if (isFrontFacing) colors.front else colors.back
            val highlightColor = if (isFrontFacing) colors.frontHighlight else colors.backHighlight

            // Outer ring
            scale(scaleX = scaleX.coerceAtLeast(0.05f), scaleY = 1f, pivot = Offset(centerX, centerY)) {
                // Coin gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to highlightColor,
                        0.3f to faceColor,
                        0.7f to faceColor,
                        1f to colors.shadow,
                        center = Offset(centerX - radius * 0.3f, centerY - radius * 0.3f)
                    ),
                    radius = radius,
                    center = Offset(centerX, centerY)
                )

                // Inner ring/detail
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to highlightColor.copy(alpha = 0.3f),
                        1f to Color.Transparent
                    ),
                    radius = radius * 0.85f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 3f)
                )

                // Bank emblem area
                drawCircle(
                    color = faceColor.copy(alpha = 0.5f),
                    radius = radius * 0.6f,
                    center = Offset(centerX, centerY),
                    style = Fill
                )

                // Emblem highlight
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to highlightColor.copy(alpha = 0.4f),
                        1f to Color.Transparent,
                        center = Offset(centerX - radius * 0.2f, centerY - radius * 0.2f)
                    ),
                    radius = radius * 0.5f,
                    center = Offset(centerX, centerY)
                )
            }

            // Add glow effect for special skins
            if (skin.glowColor != null) {
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to Color(skin.glowColor).copy(alpha = sparkleAlpha * 0.5f),
                        1f to Color.Transparent
                    ),
                    radius = radius * 1.3f,
                    center = Offset(centerX, centerY)
                )
            }

            // Draw sparkles for gold/special coins
            if (coin.type != CoinType.BANK_DISCOUNT) {
                drawSparkles(
                    center = Offset(centerX, centerY),
                    radius = radius,
                    sparkleAlpha = sparkleAlpha,
                    time = currentTime
                )
            }
        }
    }
}

/**
 * Renders a 3D-style power-up.
 */
@Composable
fun PowerUp3D(
    powerUp: PowerUp,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Pulsing glow
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    var isCollected by remember { mutableStateOf(false) }
    val collectScale by animateFloatAsState(
        targetValue = if (isCollected) 2f else 1f,
        animationSpec = tween(400)
    )
    val collectAlpha by animateFloatAsState(
        targetValue = if (isCollected) 0f else 1f,
        animationSpec = tween(400)
    )

    val colors = getPowerUpColors(powerUp.type)

    Box(
        modifier = modifier
            .size(80.dp)
            .graphicsLayer {
                scaleX = collectScale * pulseScale
                scaleY = collectScale * pulseScale
                rotationZ = rotation
                alpha = collectAlpha
            }
            .clickable(
                onClick = {
                    isCollected = true
                    onTap()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = minOf(size.width, size.height) / 2.5f

            // Outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    0f to colors.primary.copy(alpha = 0.6f),
                    0.5f to colors.primary.copy(alpha = 0.3f),
                    1f to Color.Transparent
                ),
                radius = radius * 1.5f,
                center = Offset(centerX, centerY)
            )

            // Power-up orb
            drawCircle(
                brush = Brush.radialGradient(
                    0f to colors.highlight,
                    0.4f to colors.primary,
                    1f to colors.shadow
                ),
                radius = radius,
                center = Offset(centerX, centerY)
            )

            // Inner symbol area
            drawCircle(
                brush = Brush.radialGradient(
                    0f to colors.highlight.copy(alpha = 0.8f),
                    1f to Color.Transparent
                ),
                radius = radius * 0.5f,
                center = Offset(centerX, centerY)
            )

            // Draw power-up specific icon
            drawPowerUpIcon(powerUp.type, Offset(centerX, centerY), radius * 0.6f, colors)
        }
    }
}

private fun DrawScope.drawSparkles(
    center: Offset,
    radius: Float,
    sparkleAlpha: Float,
    time: Long
) {
    val sparkleCount = 4
    for (i in 0 until sparkleCount) {
        val angle = (time / 50.0 + i * 90).toFloat()
        val rad = (angle * PI / 180.0).toFloat()
        val distance = radius * 0.8f
        val sparkleX = center.x + cos(rad) * distance
        val sparkleY = center.y + sin(rad) * distance
        val sparkleSize = 4f

        drawCircle(
            color = Color.White.copy(alpha = sparkleAlpha * 0.8f),
            radius = sparkleSize,
            center = Offset(sparkleX, sparkleY)
        )
    }
}

private fun DrawScope.drawPowerUpIcon(
    type: PowerUpType,
    center: Offset,
    size: Float,
    colors: PowerUpColorScheme
) {
    when (type) {
        PowerUpType.MAGNET -> {
            // Draw U-shaped magnet
            val path = Path().apply {
                moveTo(center.x - size * 0.4f, center.y - size * 0.3f)
                lineTo(center.x - size * 0.4f, center.y + size * 0.3f)
                quadraticTo(center.x, center.y + size * 0.6f, center.x + size * 0.4f, center.y + size * 0.3f)
                lineTo(center.x + size * 0.4f, center.y - size * 0.3f)
            }
            drawPath(path, Color.White, style = Stroke(width = size * 0.15f, cap = StrokeCap.Round))
        }
        PowerUpType.MULTIPLIER -> {
            // Draw 2x symbol
            drawCircle(Color.White, size * 0.3f, center, style = Stroke(width = 3f))
            drawLine(Color.White,
                Offset(center.x - size * 0.15f, center.y - size * 0.15f),
                Offset(center.x + size * 0.15f, center.y + size * 0.15f),
                strokeWidth = 3f
            )
            drawLine(Color.White,
                Offset(center.x + size * 0.15f, center.y - size * 0.15f),
                Offset(center.x - size * 0.15f, center.y + size * 0.15f),
                strokeWidth = 3f
            )
        }
        PowerUpType.SHIELD -> {
            // Draw shield shape
            val path = Path().apply {
                moveTo(center.x, center.y - size * 0.4f)
                lineTo(center.x + size * 0.35f, center.y - size * 0.2f)
                lineTo(center.x + size * 0.35f, center.y + size * 0.1f)
                quadraticTo(center.x, center.y + size * 0.5f, center.x - size * 0.35f, center.y + size * 0.1f)
                lineTo(center.x - size * 0.35f, center.y - size * 0.2f)
                close()
            }
            drawPath(path, Color.White, style = Stroke(width = 3f))
        }
        PowerUpType.FREEZE -> {
            // Draw snowflake
            for (i in 0 until 6) {
                val angle = i * 60f
                val rad = (angle * PI / 180.0).toFloat()
                drawLine(
                    Color.White,
                    center,
                    Offset(center.x + cos(rad) * size * 0.4f, center.y + sin(rad) * size * 0.4f),
                    strokeWidth = 2f
                )
            }
            drawCircle(Color.White, size * 0.1f, center)
        }
        PowerUpType.INVISIBILITY -> {
            // Draw eye with slash
            val eyeWidth = size * 0.4f
            val eyeHeight = size * 0.2f
            drawOval(
                Color.White,
                Offset(center.x - eyeWidth, center.y - eyeHeight),
                Size(eyeWidth * 2, eyeHeight * 2),
                style = Stroke(width = 2f)
            )
            drawLine(
                Color.White,
                Offset(center.x - size * 0.3f, center.y + size * 0.3f),
                Offset(center.x + size * 0.3f, center.y - size * 0.3f),
                strokeWidth = 3f
            )
        }
    }
}

data class Coin3DColors(
    val front: Color,
    val frontHighlight: Color,
    val back: Color,
    val backHighlight: Color,
    val edge: Color,
    val edgeDark: Color,
    val shadow: Color
)

data class PowerUpColorScheme(
    val primary: Color,
    val highlight: Color,
    val shadow: Color
)

private fun getCoin3DColors(type: CoinType, skin: CoinSkin): Coin3DColors {
    val baseColors = when (type) {
        CoinType.BANK_HAPOALIM -> Coin3DColors(
            front = Color(0xFFE53935),      // Red
            frontHighlight = Color(0xFFFF6F60),
            back = Color(0xFFB71C1C),
            backHighlight = Color(0xFFE53935),
            edge = Color(0xFFC62828),
            edgeDark = Color(0xFF8B0000),
            shadow = Color(0xFF7F0000)
        )
        CoinType.BANK_LEUMI -> Coin3DColors(
            front = Color(0xFF1565C0),      // Blue
            frontHighlight = Color(0xFF42A5F5),
            back = Color(0xFF0D47A1),
            backHighlight = Color(0xFF1565C0),
            edge = Color(0xFF0D47A1),
            edgeDark = Color(0xFF002171),
            shadow = Color(0xFF001970)
        )
        CoinType.BANK_MIZRAHI -> Coin3DColors(
            front = Color(0xFF2E7D32),      // Green
            frontHighlight = Color(0xFF66BB6A),
            back = Color(0xFF1B5E20),
            backHighlight = Color(0xFF2E7D32),
            edge = Color(0xFF1B5E20),
            edgeDark = Color(0xFF003300),
            shadow = Color(0xFF002200)
        )
        CoinType.BANK_DISCOUNT -> Coin3DColors(
            front = Color(0xFFFF6F00),      // Orange
            frontHighlight = Color(0xFFFFB74D),
            back = Color(0xFFE65100),
            backHighlight = Color(0xFFFF6F00),
            edge = Color(0xFFE65100),
            edgeDark = Color(0xFFBF360C),
            shadow = Color(0xFFBF360C)
        )
    }

    // Apply skin modifications
    return when (skin.id) {
        CoinSkinId.GOLDEN -> baseColors.copy(
            front = Color(0xFFFFD700),
            frontHighlight = Color(0xFFFFF176),
            back = Color(0xFFFFB300),
            backHighlight = Color(0xFFFFD700)
        )
        CoinSkinId.DIAMOND -> baseColors.copy(
            front = Color(0xFFE0F7FA),
            frontHighlight = Color.White,
            back = Color(0xFFB2EBF2),
            backHighlight = Color(0xFFE0F7FA)
        )
        CoinSkinId.NEON -> baseColors.copy(
            front = Color(0xFF00FF00),
            frontHighlight = Color(0xFF7FFF7F),
            back = Color(0xFF00CC00),
            backHighlight = Color(0xFF00FF00)
        )
        CoinSkinId.FIRE -> baseColors.copy(
            front = Color(0xFFFF4500),
            frontHighlight = Color(0xFFFF7F50),
            back = Color(0xFFFF0000),
            backHighlight = Color(0xFFFF4500)
        )
        CoinSkinId.ICE -> baseColors.copy(
            front = Color(0xFF87CEEB),
            frontHighlight = Color.White,
            back = Color(0xFF5F9EA0),
            backHighlight = Color(0xFF87CEEB)
        )
        else -> baseColors
    }
}

private fun getPowerUpColors(type: PowerUpType): PowerUpColorScheme {
    return when (type) {
        PowerUpType.MAGNET -> PowerUpColorScheme(
            primary = Color(0xFF2196F3),
            highlight = Color(0xFF64B5F6),
            shadow = Color(0xFF0D47A1)
        )
        PowerUpType.MULTIPLIER -> PowerUpColorScheme(
            primary = Color(0xFFFFD700),
            highlight = Color(0xFFFFF176),
            shadow = Color(0xFFFF8F00)
        )
        PowerUpType.SHIELD -> PowerUpColorScheme(
            primary = Color(0xFF4CAF50),
            highlight = Color(0xFF81C784),
            shadow = Color(0xFF1B5E20)
        )
        PowerUpType.FREEZE -> PowerUpColorScheme(
            primary = Color(0xFF00BCD4),
            highlight = Color(0xFF4DD0E1),
            shadow = Color(0xFF006064)
        )
        PowerUpType.INVISIBILITY -> PowerUpColorScheme(
            primary = Color(0xFF9C27B0),
            highlight = Color(0xFFBA68C8),
            shadow = Color(0xFF4A148C)
        )
    }
}
