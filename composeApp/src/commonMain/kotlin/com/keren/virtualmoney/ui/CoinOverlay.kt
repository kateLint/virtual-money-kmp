package com.keren.virtualmoney.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.keren.virtualmoney.game.Coin
import com.keren.virtualmoney.platform.getCurrentTimeMillis
import com.keren.virtualmoney.theme.CoinSkin
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import virtualmoney.composeapp.generated.resources.*

/**
 * Renders all coins on screen using normalized coordinates.
 * @param coins List of coins to display
 * @param screenSize Current screen dimensions
 * @param onCoinTapped Callback when a coin is tapped
 */
@Composable
fun CoinOverlay(
        coins: List<Coin>,
        skin: CoinSkin,
        screenSize: IntSize,
        onCoinTapped: (String) -> Unit
) {
        Layout(
                content = {
                        coins.forEach { coin ->
                                key(coin.id) {
                                        AnimatedCoin(
                                                coin = coin,
                                                skin = skin,
                                                onTap = { onCoinTapped(coin.id) }
                                        )
                                }
                        }
                },
                modifier = Modifier.fillMaxSize()
        ) { measurables, constraints ->
                val placeables =
                        measurables.map {
                                it.measure(constraints.copy(minWidth = 0, minHeight = 0))
                        }

                layout(constraints.maxWidth, constraints.maxHeight) {
                        placeables.forEachIndexed { index, placeable ->
                                val coin = coins[index]
                                val x = (coin.x * constraints.maxWidth).roundToInt()
                                val y = (coin.y * constraints.maxHeight).roundToInt()
                                placeable.place(
                                        x = x - (placeable.width / 2),
                                        y = y - (placeable.height / 2)
                                )
                        }
                }
        }
}

/** Single animated coin with hover/bob and fade-in effects. */
@Composable
internal fun AnimatedCoin(coin: Coin, skin: CoinSkin, onTap: () -> Unit) {
        // Track age of coin for effects
        var currentTime by remember { mutableStateOf(getCurrentTimeMillis()) }
        LaunchedEffect(Unit) {
                while (true) {
                        delay(50)
                        currentTime = getCurrentTimeMillis()
                }
        }

        val coinAge = currentTime - coin.spawnTime
        val isExpiring =
                Coin.isPenaltyCoin(coin.type) && coinAge > (Coin.PENALTY_COIN_LIFETIME_MS - 500)

        // Fade-in animation (400ms)
        val fadeInProgress = (coinAge.toFloat() / 400f).coerceIn(0f, 1f)

        // Hover/bob animation (gentle Y oscillation)
        val infiniteTransition = rememberInfiniteTransition()
        val hoverOffset by
                infiniteTransition.animateFloat(
                        initialValue = -8f,
                        targetValue = 8f,
                        animationSpec =
                                infiniteRepeatable(
                                        animation = tween(1500, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                )
                )

        // Collect animation (fade out + scale up when tapped)
        var isCollected by remember { mutableStateOf(false) }
        val collectAlpha by
                animateFloatAsState(
                        targetValue = if (isCollected) 0f else 1f,
                        animationSpec = tween(300)
                )
        val collectScale by
                animateFloatAsState(
                        targetValue = if (isCollected) 2f else 1f,
                        animationSpec = tween(300)
                )

        // Calculate alpha for expiring penalty coins
        val expiringAlpha =
                if (isExpiring) {
                        val timeLeft = Coin.PENALTY_COIN_LIFETIME_MS - coinAge
                        (timeLeft.toFloat() / 500f).coerceIn(0f, 1f)
                } else {
                        1f
                }

        // Combined alpha
        val finalAlpha = fadeInProgress * collectAlpha * expiringAlpha

        Box(
                modifier =
                        Modifier.size(100.dp).graphicsLayer {
                                translationY = hoverOffset
                                scaleX =
                                        collectScale *
                                                (if (isExpiring) (expiringAlpha + 0.5f) else 1f)
                                scaleY =
                                        collectScale *
                                                (if (isExpiring) (expiringAlpha + 0.5f) else 1f)
                                alpha = finalAlpha
                        },
                contentAlignment = Alignment.Center
        ) {
                Coin3D(
                        coin = coin,
                        skin = skin,
                        onTap = {
                                if (!isCollected) {
                                        isCollected = true
                                        onTap()
                                }
                        },
                        modifier = Modifier.fillMaxSize()
                )
        }
}
