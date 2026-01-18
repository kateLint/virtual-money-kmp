package com.keren.virtualmoney.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.IntSize
import com.keren.virtualmoney.ar.data.ProjectedCoin
import com.keren.virtualmoney.theme.CoinSkin
import kotlin.math.roundToInt

/**
 * Renders AR coins using projected 2D coordinates. Similar to CoinOverlay but uses ProjectedCoin
 * instead of Coin.
 *
 * @param projectedCoins List of coins with 2D screen coordinates
 * @param screenSize Current screen dimensions
 * @param onCoinTapped Callback when a coin is tapped
 */
@Composable
fun ARCoinOverlay(
        projectedCoins: List<ProjectedCoin>,
        skin: CoinSkin, // Added skin parameter
        screenSize: IntSize,
        onCoinTapped: (String) -> Unit
) {
    Layout(
            content = {
                projectedCoins.forEach { projected ->
                    AnimatedCoin(
                            coin = projected.coin.copy(scale = projected.apparentScale),
                            skin = skin, // Passed skin to AnimatedCoin
                            onTap = { onCoinTapped(projected.coin.id) }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
    ) { measurables, constraints ->
        val placeables =
                measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val projected = projectedCoins[index]

                // Use projected screen coordinates directly
                placeable.place(
                        x = projected.screenX.roundToInt() - (placeable.width / 2),
                        y = projected.screenY.roundToInt() - (placeable.height / 2)
                )
            }
        }
    }
}
