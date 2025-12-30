package com.keren.virtualmoney.ar.data

import com.keren.virtualmoney.game.Coin

/**
 * Represents a 3D coin projected onto the 2D screen.
 *
 * @property coin The original coin
 * @property screenX The X coordinate on screen (pixels)
 * @property screenY The Y coordinate on screen (pixels)
 * @property apparentScale The apparent scale factor based on distance (1.0 = base size)
 * @property distance The distance from camera to coin (meters)
 */
data class ProjectedCoin(
    val coin: Coin,
    val screenX: Float,
    val screenY: Float,
    val apparentScale: Float,
    val distance: Float
)
