package com.keren.virtualmoney.ar.math

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.acos

/**
 * Represents a quaternion for 3D rotations.
 * Quaternions provide a robust way to represent rotations without gimbal lock.
 *
 * @property w The scalar (real) component
 * @property x The x component of the vector part
 * @property y The y component of the vector part
 * @property z The z component of the vector part
 */
data class Quaternion(
    val w: Float,
    val x: Float,
    val y: Float,
    val z: Float
) {
    /**
     * Multiplies this quaternion by another quaternion.
     * Used to combine rotations: q1 * q2 means "apply q2, then q1".
     *
     * @param other The quaternion to multiply with
     * @return The product quaternion
     */
    operator fun times(other: Quaternion): Quaternion {
        return Quaternion(
            w = w * other.w - x * other.x - y * other.y - z * other.z,
            x = w * other.x + x * other.w + y * other.z - z * other.y,
            y = w * other.y - x * other.z + y * other.w + z * other.x,
            z = w * other.z + x * other.y - y * other.x + z * other.w
        )
    }

    /**
     * Rotates a vector by this quaternion using the formula: q * v * q⁻¹
     *
     * @param vector The vector to rotate
     * @return The rotated vector
     */
    operator fun times(vector: Vector3D): Vector3D {
        // Convert vector to pure quaternion (w=0)
        val vecQuat = Quaternion(0f, vector.x, vector.y, vector.z)

        // Perform q * v * q⁻¹
        val result = this * vecQuat * this.inverse()

        // Extract vector part
        return Vector3D(result.x, result.y, result.z)
    }

    /**
     * Calculates the inverse (conjugate for unit quaternions) of this quaternion.
     * The inverse undoes the rotation represented by this quaternion.
     *
     * @return The inverse quaternion
     */
    fun inverse(): Quaternion {
        // For unit quaternions, inverse = conjugate
        // For non-unit quaternions, we need to divide by the squared magnitude
        val lengthSq = w * w + x * x + y * y + z * z
        return Quaternion(w / lengthSq, -x / lengthSq, -y / lengthSq, -z / lengthSq)
    }

    /**
     * Normalizes this quaternion to unit length.
     * Unit quaternions represent valid rotations.
     *
     * @return A normalized quaternion
     */
    fun normalize(): Quaternion {
        val length = sqrt(w * w + x * x + y * y + z * z)
        return if (length > 0.0001f) {
            Quaternion(w / length, x / length, y / length, z / length)
        } else {
            IDENTITY
        }
    }

    companion object {
        /**
         * Identity quaternion representing no rotation.
         */
        val IDENTITY = Quaternion(1f, 0f, 0f, 0f)

        /**
         * Creates a quaternion from Euler angles (in radians).
         * Rotation order: Y (yaw) -> X (pitch) -> Z (roll)
         *
         * @param pitch Rotation around X axis (radians)
         * @param yaw Rotation around Y axis (radians)
         * @param roll Rotation around Z axis (radians)
         * @return A quaternion representing the rotation
         */
        fun fromEuler(pitch: Float, yaw: Float, roll: Float): Quaternion {
            // Calculate half angles
            val cy = cos(yaw * 0.5f)
            val sy = sin(yaw * 0.5f)
            val cp = cos(pitch * 0.5f)
            val sp = sin(pitch * 0.5f)
            val cr = cos(roll * 0.5f)
            val sr = sin(roll * 0.5f)

            // Combine rotations in YXZ order
            return Quaternion(
                w = cr * cp * cy + sr * sp * sy,
                x = cr * sp * cy + sr * cp * sy,
                y = cr * cp * sy - sr * sp * cy,
                z = sr * cp * cy - cr * sp * sy
            )
        }

        /**
         * Creates a quaternion from an axis and angle.
         *
         * @param axis The axis of rotation (should be normalized)
         * @param angle The angle of rotation in radians
         * @return A quaternion representing the rotation
         */
        fun fromAxisAngle(axis: Vector3D, angle: Float): Quaternion {
            val normalizedAxis = axis.normalize()
            val halfAngle = angle * 0.5f
            val s = sin(halfAngle)

            return Quaternion(
                w = cos(halfAngle),
                x = normalizedAxis.x * s,
                y = normalizedAxis.y * s,
                z = normalizedAxis.z * s
            )
        }

        /**
         * Performs spherical linear interpolation between two quaternions.
         * This produces smooth rotation interpolation.
         *
         * @param start The starting quaternion
         * @param end The ending quaternion
         * @param t Interpolation factor (0 = start, 1 = end)
         * @return The interpolated quaternion
         */
        fun slerp(start: Quaternion, end: Quaternion, t: Float): Quaternion {
            // Clamp t to [0, 1]
            val clampedT = t.coerceIn(0f, 1f)

            // Calculate dot product
            var dot = start.w * end.w + start.x * end.x + start.y * end.y + start.z * end.z

            // If the dot product is negative, negate one quaternion to take the shorter path
            val endAdjusted = if (dot < 0f) {
                dot = -dot
                Quaternion(-end.w, -end.x, -end.y, -end.z)
            } else {
                end
            }

            // If quaternions are very close, use linear interpolation
            if (dot > 0.9995f) {
                return Quaternion(
                    w = start.w + clampedT * (endAdjusted.w - start.w),
                    x = start.x + clampedT * (endAdjusted.x - start.x),
                    y = start.y + clampedT * (endAdjusted.y - start.y),
                    z = start.z + clampedT * (endAdjusted.z - start.z)
                ).normalize()
            }

            // Perform spherical linear interpolation
            val theta = acos(dot)
            val sinTheta = sin(theta)
            val scale0 = sin((1f - clampedT) * theta) / sinTheta
            val scale1 = sin(clampedT * theta) / sinTheta

            return Quaternion(
                w = start.w * scale0 + endAdjusted.w * scale1,
                x = start.x * scale0 + endAdjusted.x * scale1,
                y = start.y * scale0 + endAdjusted.y * scale1,
                z = start.z * scale0 + endAdjusted.z * scale1
            )
        }
    }
}
