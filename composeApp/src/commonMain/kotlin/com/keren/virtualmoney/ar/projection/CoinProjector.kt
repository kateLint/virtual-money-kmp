package com.keren.virtualmoney.ar.projection

import androidx.compose.ui.unit.IntSize
import com.keren.virtualmoney.ar.data.Pose
import com.keren.virtualmoney.ar.data.ProjectedCoin
import com.keren.virtualmoney.ar.math.Vector3D
import com.keren.virtualmoney.game.Coin
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Projects 3D coins onto a 2D screen using perspective projection.
 *
 * This class transforms world-space coin positions to screen-space coordinates
 * based on the camera's pose, implementing proper perspective projection with
 * field-of-view and distance-based scaling.
 *
 * @property fov The camera's horizontal field of view in degrees
 * @property baseCoinSize The base coin size in meters (used for scaling)
 */
class CoinProjector(
    private val fov: Float = 60f,
    private val baseCoinSize: Float = 0.15f
) {
    /**
     * Projects a 3D coin to 2D screen coordinates.
     *
     * The projection pipeline:
     * 1. Transform from world space to camera space
     * 2. Cull coins behind the camera (z >= 0 in camera space)
     * 3. Apply perspective projection using focal length
     * 4. Cull coins outside the screen bounds
     * 5. Calculate distance-based apparent scale
     *
     * Camera space convention:
     * - +X is right
     * - +Y is up
     * - -Z is forward (camera looks down -Z axis)
     *
     * @param coin The coin to project
     * @param cameraPose The camera's current pose (position and orientation)
     * @param screenSize The screen dimensions (width x height) in pixels
     * @return ProjectedCoin if visible, null if behind camera or off-screen
     */
    fun project3DTo2D(
        coin: Coin,
        cameraPose: Pose,
        screenSize: IntSize
    ): ProjectedCoin? {
        // Ensure coin has 3D position
        val position3D = coin.position3D ?: return null

        // Step 1: Transform coin position from world space to camera space
        val cameraSpace = transformWorldToCamera(position3D, cameraPose)

        // Step 2: Cull coins behind the camera
        // In camera space, -Z is forward, so coins with z >= 0 are behind us
        if (cameraSpace.z >= 0f) {
            return null
        }

        // Step 3: Calculate perspective projection
        // depth is the absolute value of z (how far forward the coin is)
        val depth = -cameraSpace.z

        // Calculate focal length from FOV
        val fovRadians = fov * PI.toFloat() / 180f
        val focalLength = (screenSize.width / 2f) / tan(fovRadians / 2f)

        // Project to normalized device coordinates, then to screen space
        // Screen space: origin at top-left, +X right, +Y down
        val screenX = (cameraSpace.x / depth) * focalLength + (screenSize.width / 2f)
        val screenY = -(cameraSpace.y / depth) * focalLength + (screenSize.height / 2f)  // Flip Y

        // Step 4: Cull coins outside screen bounds
        if (screenX < 0 || screenX > screenSize.width || screenY < 0 || screenY > screenSize.height) {
            return null
        }

        // Step 5: Calculate distance and apparent scale
        val distance = sqrt(
            cameraSpace.x * cameraSpace.x +
            cameraSpace.y * cameraSpace.y +
            cameraSpace.z * cameraSpace.z
        )

        // Apparent scale decreases with distance (inverse relationship)
        // At 1 meter, scale = 1.0; at 2 meters, scale = 0.5, etc.
        val apparentScale = baseCoinSize / distance

        return ProjectedCoin(
            coin = coin,
            screenX = screenX,
            screenY = screenY,
            apparentScale = apparentScale,
            distance = distance
        )
    }

    /**
     * Transforms a point from world space to camera space.
     *
     * This applies the inverse of the camera's transformation:
     * 1. Translate by -cameraPosition
     * 2. Rotate by inverse(cameraOrientation)
     *
     * @param worldPosition The position in world space
     * @param cameraPose The camera's pose
     * @return The position in camera space
     */
    private fun transformWorldToCamera(worldPosition: Vector3D, cameraPose: Pose): Vector3D {
        // Translate: move world origin to camera position
        val translated = worldPosition - cameraPose.position

        // Rotate: apply inverse rotation
        val invRotation = cameraPose.rotation.inverse()
        return invRotation * translated
    }
}
