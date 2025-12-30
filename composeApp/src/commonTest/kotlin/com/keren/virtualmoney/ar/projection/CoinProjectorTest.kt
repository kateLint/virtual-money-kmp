package com.keren.virtualmoney.ar.projection

import androidx.compose.ui.unit.IntSize
import com.keren.virtualmoney.ar.data.Pose
import com.keren.virtualmoney.ar.math.Quaternion
import com.keren.virtualmoney.ar.math.Vector3D
import com.keren.virtualmoney.game.Coin
import com.keren.virtualmoney.game.CoinType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CoinProjectorTest {

    @Test
    fun testCoinDirectlyInFrontProjectsToScreenCenter() {
        // Given: 1920x1080 screen, camera at origin facing -Z
        val screenSize = IntSize(1920, 1080)
        val projector = CoinProjector(
            fov = 60f,
            baseCoinSize = 0.15f
        )

        // Camera at origin, no rotation (facing -Z by default)
        val cameraPose = Pose(
            position = Vector3D.ZERO,
            rotation = Quaternion.IDENTITY
        )

        // Coin directly in front of camera at 2 meters
        val coin = Coin(
            id = "test-coin",
            x = 0.5f,  // Fallback 2D position
            y = 0.5f,
            position3D = Vector3D(0f, 0f, -2f),  // -Z is forward
            type = CoinType.BANK_HAPOALIM
        )

        // When: Project the coin
        val projected = projector.project3DTo2D(
            coin = coin,
            cameraPose = cameraPose,
            screenSize = screenSize
        )

        // Then: Should project to center of screen
        assertNotNull(projected, "Coin in front of camera should be visible")
        assertEquals(coin, projected.coin)
        assertEquals(screenSize.width / 2f, projected.screenX, 1f, "X should be at screen center")
        assertEquals(screenSize.height / 2f, projected.screenY, 1f, "Y should be at screen center")
        assertEquals(2f, projected.distance, 0.01f, "Distance should be 2 meters")
        assertTrue(projected.apparentScale > 0f, "Apparent scale should be positive")
    }

    @Test
    fun testCoinBehindCameraReturnsNull() {
        // Given: Screen and projector setup
        val screenSize = IntSize(1920, 1080)
        val projector = CoinProjector(
            fov = 60f,
            baseCoinSize = 0.15f
        )

        // Camera at origin
        val cameraPose = Pose(
            position = Vector3D.ZERO,
            rotation = Quaternion.IDENTITY
        )

        // Coin BEHIND the camera (positive Z)
        val coin = Coin(
            id = "behind-coin",
            x = 0.5f,
            y = 0.5f,
            position3D = Vector3D(0f, 0f, 2f),  // Positive Z is behind
            type = CoinType.BANK_HAPOALIM
        )

        // When: Project the coin
        val projected = projector.project3DTo2D(
            coin = coin,
            cameraPose = cameraPose,
            screenSize = screenSize
        )

        // Then: Should return null (not visible)
        assertNull(projected, "Coin behind camera should not be visible")
    }

    @Test
    fun testCloserCoinsHaveLargerApparentScale() {
        // Given: Projector setup
        val screenSize = IntSize(1920, 1080)
        val projector = CoinProjector(
            fov = 60f,
            baseCoinSize = 0.15f
        )

        // Camera at origin
        val cameraPose = Pose(
            position = Vector3D.ZERO,
            rotation = Quaternion.IDENTITY
        )

        // Near coin at 1 meter
        val nearCoin = Coin(
            id = "near-coin",
            x = 0.5f,
            y = 0.5f,
            position3D = Vector3D(0f, 0f, -1f),
            type = CoinType.BANK_HAPOALIM
        )

        // Far coin at 5 meters
        val farCoin = Coin(
            id = "far-coin",
            x = 0.5f,
            y = 0.5f,
            position3D = Vector3D(0f, 0f, -5f),
            type = CoinType.BANK_HAPOALIM
        )

        // When: Project both coins
        val nearProjected = projector.project3DTo2D(
            coin = nearCoin,
            cameraPose = cameraPose,
            screenSize = screenSize
        )

        val farProjected = projector.project3DTo2D(
            coin = farCoin,
            cameraPose = cameraPose,
            screenSize = screenSize
        )

        // Then: Near coin should have larger apparent scale
        assertNotNull(nearProjected, "Near coin should be visible")
        assertNotNull(farProjected, "Far coin should be visible")
        assertTrue(
            nearProjected.apparentScale > farProjected.apparentScale,
            "Closer coin should have larger apparent scale. " +
                "Near: ${nearProjected.apparentScale}, Far: ${farProjected.apparentScale}"
        )

        // Verify distance calculations
        assertEquals(1f, nearProjected.distance, 0.01f)
        assertEquals(5f, farProjected.distance, 0.01f)
    }

    @Test
    fun testCoinToTheRightProjectsToRightSideOfScreen() {
        // Given: Projector setup
        val screenSize = IntSize(1920, 1080)
        val projector = CoinProjector(
            fov = 60f,
            baseCoinSize = 0.15f
        )

        // Camera at origin
        val cameraPose = Pose(
            position = Vector3D.ZERO,
            rotation = Quaternion.IDENTITY
        )

        // Coin to the right and in front
        val coin = Coin(
            id = "right-coin",
            x = 0.5f,
            y = 0.5f,
            position3D = Vector3D(1f, 0f, -2f),  // 1m right, 2m forward
            type = CoinType.BANK_HAPOALIM
        )

        // When: Project the coin
        val projected = projector.project3DTo2D(
            coin = coin,
            cameraPose = cameraPose,
            screenSize = screenSize
        )

        // Then: Should project to right side of screen
        assertNotNull(projected, "Coin should be visible")
        assertTrue(
            projected.screenX > screenSize.width / 2f,
            "Coin to the right should project to right half of screen. X: ${projected.screenX}"
        )
    }

    @Test
    fun testCoinAboveCameraProjectsToTopOfScreen() {
        // Given: Projector setup
        val screenSize = IntSize(1920, 1080)
        val projector = CoinProjector(
            fov = 60f,
            baseCoinSize = 0.15f
        )

        // Camera at origin
        val cameraPose = Pose(
            position = Vector3D.ZERO,
            rotation = Quaternion.IDENTITY
        )

        // Coin above and in front (smaller height to stay on screen)
        val coin = Coin(
            id = "above-coin",
            x = 0.5f,
            y = 0.5f,
            position3D = Vector3D(0f, 0.3f, -2f),  // 0.3m up, 2m forward
            type = CoinType.BANK_HAPOALIM
        )

        // When: Project the coin
        val projected = projector.project3DTo2D(
            coin = coin,
            cameraPose = cameraPose,
            screenSize = screenSize
        )

        // Then: Should project to top half of screen
        assertNotNull(projected, "Coin should be visible")
        assertTrue(
            projected.screenY < screenSize.height / 2f,
            "Coin above should project to top half of screen (lower Y). Y: ${projected.screenY}"
        )
    }

    @Test
    fun testCoinOffScreenReturnsNull() {
        // Given: Projector setup with narrow FOV
        val screenSize = IntSize(1920, 1080)
        val projector = CoinProjector(
            fov = 30f,  // Narrow FOV
            baseCoinSize = 0.15f
        )

        // Camera at origin
        val cameraPose = Pose(
            position = Vector3D.ZERO,
            rotation = Quaternion.IDENTITY
        )

        // Coin far to the side (likely off-screen)
        val coin = Coin(
            id = "offscreen-coin",
            x = 0.5f,
            y = 0.5f,
            position3D = Vector3D(5f, 0f, -1f),  // 5m right, 1m forward
            type = CoinType.BANK_HAPOALIM
        )

        // When: Project the coin
        val projected = projector.project3DTo2D(
            coin = coin,
            cameraPose = cameraPose,
            screenSize = screenSize
        )

        // Then: Should return null (off-screen)
        assertNull(projected, "Coin far off-screen should not be visible")
    }

    @Test
    fun testCoinWithoutPosition3DReturnsNull() {
        // Given: Projector setup
        val screenSize = IntSize(1920, 1080)
        val projector = CoinProjector()

        // Camera at origin
        val cameraPose = Pose.IDENTITY

        // Coin without 3D position (2D mode coin)
        val coin = Coin(
            id = "2d-coin",
            x = 0.5f,
            y = 0.5f,
            position3D = null,  // No 3D position
            type = CoinType.BANK_HAPOALIM
        )

        // When: Project the coin
        val projected = projector.project3DTo2D(
            coin = coin,
            cameraPose = cameraPose,
            screenSize = screenSize
        )

        // Then: Should return null (no 3D position)
        assertNull(projected, "Coin without 3D position should not be projected")
    }
}
