package com.keren.virtualmoney.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import com.keren.virtualmoney.platform.getCurrentTimeMillis
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keren.virtualmoney.game.Coin
import com.keren.virtualmoney.game.CoinType
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import virtualmoney.composeapp.generated.resources.*
import kotlin.math.roundToInt

/**
 * Renders all coins on screen using normalized coordinates.
 * @param coins List of coins to display
 * @param screenSize Current screen dimensions
 * @param onCoinTapped Callback when a coin is tapped
 */
@Composable
fun CoinOverlay(
    coins: List<Coin>,
    screenSize: IntSize,
    onCoinTapped: (String) -> Unit
) {
    Layout(
        content = {
            coins.forEach { coin ->
                AnimatedCoin(
                    coin = coin,
                    onTap = { onCoinTapped(coin.id) }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val coin = coins[index]

                // Convert normalized coordinates (0.0-1.0) to actual pixel positions
                val x = (coin.x * constraints.maxWidth).roundToInt()
                val y = (coin.y * constraints.maxHeight).roundToInt()

                // Center the coin at the calculated position
                placeable.place(
                    x = x - (placeable.width / 2),
                    y = y - (placeable.height / 2)
                )
            }
        }
    }
}

/**
 * Single animated coin with rotation and pulse effects.
 */
@Composable
internal fun AnimatedCoin(
    coin: Coin,
    onTap: () -> Unit
) {
    // Track age of coin for black coin fade-out
    var currentTime by remember { mutableStateOf(getCurrentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            currentTime = getCurrentTimeMillis()
        }
    }

    val coinAge = currentTime - coin.spawnTime
    val isExpiring = Coin.isPenaltyCoin(coin.type) &&
            coinAge > (Coin.PENALTY_COIN_LIFETIME_MS - 500)

    // Continuous rotation animation
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Collect animation (fade out + scale up when tapped)
    var isCollected by remember { mutableStateOf(false) }
    val collectAlpha by animateFloatAsState(
        targetValue = if (isCollected) 0f else 1f,
        animationSpec = tween(300)
    )
    val collectScale by animateFloatAsState(
        targetValue = if (isCollected) 2f else 1f,
        animationSpec = tween(300)
    )

    // Calculate alpha for expiring penalty coins
    val expiringAlpha = if (isExpiring) {
        val timeLeft = Coin.PENALTY_COIN_LIFETIME_MS - coinAge
        (timeLeft.toFloat() / 500f).coerceIn(0f, 1f)
    } else {
        1f
    }

    Box(
        modifier = Modifier
            .size(120.dp) // Fixed large size for all coins
            .graphicsLayer {
                rotationZ = rotation
                scaleX = collectScale * (if (isExpiring) (expiringAlpha + 0.5f) else 1f)
                scaleY = collectScale * (if (isExpiring) (expiringAlpha + 0.5f) else 1f)
                alpha = collectAlpha * expiringAlpha
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
        // Bank logo image
        Image(
            painter = painterResource(getBankLogoResource(coin.type)),
            contentDescription = when (coin.type) {
                CoinType.BANK_HAPOALIM -> "Bank Hapoalim"
                CoinType.BANK_LEUMI -> "Bank Leumi"
                CoinType.BANK_MIZRAHI -> "Bank Mizrahi"
                CoinType.BANK_DISCOUNT -> "Bank Discount"
            },
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Returns the drawable resource for each bank logo.
 */
@Composable
internal fun getBankLogoResource(type: CoinType): org.jetbrains.compose.resources.DrawableResource {
    return when (type) {
        CoinType.BANK_HAPOALIM -> Res.drawable.bank_hapoalim
        CoinType.BANK_LEUMI -> Res.drawable.bank_leumi
        CoinType.BANK_MIZRAHI -> Res.drawable.bank_mizrahi
        CoinType.BANK_DISCOUNT -> Res.drawable.bank_discount
    }
}
