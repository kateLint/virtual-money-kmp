package com.keren.virtualmoney.ar.data

import com.keren.virtualmoney.ar.math.Quaternion
import com.keren.virtualmoney.ar.math.Vector3D

/**
 * Represents a 3D pose combining position and rotation.
 * Used to track camera position and orientation in AR sessions.
 *
 * @property position The 3D position in space
 * @property rotation The rotation as a quaternion
 */
data class Pose(
    val position: Vector3D = Vector3D.ZERO,
    val rotation: Quaternion = Quaternion.IDENTITY
) {
    companion object {
        /**
         * Identity pose at origin with no rotation.
         */
        val IDENTITY = Pose()
    }
}
