package com.keren.virtualmoney.ar.math

import kotlin.math.sqrt

/**
 * Represents a 3D vector with x, y, z components.
 * Used for AR positioning, direction calculations, and 3D transformations.
 *
 * @property x The x-component of the vector
 * @property y The y-component of the vector
 * @property z The z-component of the vector
 */
data class Vector3D(
    val x: Float,
    val y: Float,
    val z: Float
) {
    /**
     * Adds two vectors component-wise.
     */
    operator fun plus(other: Vector3D): Vector3D {
        return Vector3D(x + other.x, y + other.y, z + other.z)
    }

    /**
     * Subtracts another vector from this vector component-wise.
     */
    operator fun minus(other: Vector3D): Vector3D {
        return Vector3D(x - other.x, y - other.y, z - other.z)
    }

    /**
     * Multiplies this vector by a scalar value.
     */
    operator fun times(scalar: Float): Vector3D {
        return Vector3D(x * scalar, y * scalar, z * scalar)
    }

    /**
     * Divides this vector by a scalar value.
     */
    operator fun div(scalar: Float): Vector3D {
        return Vector3D(x / scalar, y / scalar, z / scalar)
    }

    /**
     * Negates this vector (flips the direction).
     */
    operator fun unaryMinus(): Vector3D {
        return Vector3D(-x, -y, -z)
    }

    /**
     * Calculates the squared length (magnitude) of this vector.
     * Useful when you need to compare lengths without the expensive sqrt operation.
     *
     * @return The squared length of the vector
     */
    fun lengthSquared(): Float {
        return x * x + y * y + z * z
    }

    /**
     * Calculates the length (magnitude) of this vector.
     *
     * @return The length of the vector
     */
    fun length(): Float {
        return sqrt(lengthSquared())
    }

    /**
     * Returns a normalized (unit length) version of this vector.
     * If the vector has zero length, returns the zero vector.
     *
     * @return A vector with the same direction but length of 1, or ZERO if this vector has zero length
     */
    fun normalize(): Vector3D {
        val len = length()
        return if (len > 0.0001f) {
            this / len
        } else {
            ZERO
        }
    }

    /**
     * Calculates the dot product with another vector.
     * The dot product is useful for:
     * - Finding the angle between vectors
     * - Projecting one vector onto another
     * - Determining if vectors are perpendicular (dot product = 0)
     *
     * @param other The other vector
     * @return The dot product of the two vectors
     */
    fun dot(other: Vector3D): Float {
        return x * other.x + y * other.y + z * other.z
    }

    /**
     * Calculates the cross product with another vector.
     * The cross product produces a vector that is perpendicular to both input vectors.
     * Useful for:
     * - Finding surface normals
     * - Determining the orientation of a plane
     * - Calculating torque
     *
     * @param other The other vector
     * @return A vector perpendicular to both this vector and the other vector
     */
    fun cross(other: Vector3D): Vector3D {
        return Vector3D(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    /**
     * Calculates the distance from this vector to another vector.
     *
     * @param other The other vector
     * @return The distance between the two vectors
     */
    fun distanceTo(other: Vector3D): Float {
        return (this - other).length()
    }

    companion object {
        /**
         * Zero vector (0, 0, 0)
         */
        val ZERO = Vector3D(0f, 0f, 0f)

        /**
         * Up vector (0, 1, 0) - typically represents the Y-axis
         */
        val UP = Vector3D(0f, 1f, 0f)

        /**
         * Forward vector (0, 0, 1) - typically represents the Z-axis
         */
        val FORWARD = Vector3D(0f, 0f, 1f)

        /**
         * Right vector (1, 0, 0) - typically represents the X-axis
         */
        val RIGHT = Vector3D(1f, 0f, 0f)
    }
}
