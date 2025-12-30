package com.keren.virtualmoney.ar.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuaternionTest {

    private fun assertQuaternionEquals(expected: Quaternion, actual: Quaternion, delta: Float = 0.001f) {
        assertEquals(expected.w, actual.w, delta, "w component mismatch")
        assertEquals(expected.x, actual.x, delta, "x component mismatch")
        assertEquals(expected.y, actual.y, delta, "y component mismatch")
        assertEquals(expected.z, actual.z, delta, "z component mismatch")
    }

    private fun assertVector3DEquals(expected: Vector3D, actual: Vector3D, delta: Float = 0.001f) {
        assertEquals(expected.x, actual.x, delta, "x component mismatch")
        assertEquals(expected.y, actual.y, delta, "y component mismatch")
        assertEquals(expected.z, actual.z, delta, "z component mismatch")
    }

    @Test
    fun testIdentityQuaternion() {
        val identity = Quaternion.IDENTITY

        // Identity quaternion should have w=1, x=0, y=0, z=0
        assertQuaternionEquals(Quaternion(1f, 0f, 0f, 0f), identity)

        // Rotating a vector by identity should return the same vector
        val vector = Vector3D(1f, 2f, 3f)
        val rotated = identity * vector
        assertVector3DEquals(vector, rotated)
    }

    @Test
    fun testFromEulerAngles() {
        // Test 90-degree rotation around Y axis
        val q = Quaternion.fromEuler(pitch = 0f, yaw = PI.toFloat() / 2, roll = 0f)

        // Verify the quaternion components for 90-degree Y rotation
        // For rotation of angle θ around Y: q = (cos(θ/2), 0, sin(θ/2), 0)
        val expectedW = 0.707f  // cos(45°)
        val expectedY = 0.707f  // sin(45°)
        assertQuaternionEquals(Quaternion(expectedW, 0f, expectedY, 0f), q, delta = 0.01f)
    }

    @Test
    fun testRotateVectorAroundYAxis() {
        // Create a 90-degree rotation around the Y axis
        val rotation = Quaternion.fromEuler(pitch = 0f, yaw = PI.toFloat() / 2, roll = 0f)

        // Rotate the forward vector (0, 0, 1) by 90° around Y
        // Should result in the right vector (1, 0, 0)
        val forward = Vector3D(0f, 0f, 1f)
        val rotated = rotation * forward

        assertVector3DEquals(Vector3D(1f, 0f, 0f), rotated, delta = 0.01f)
    }

    @Test
    fun testQuaternionInverse() {
        // Create a rotation
        val rotation = Quaternion.fromEuler(pitch = 0.5f, yaw = 1.0f, roll = 0.3f)
        val inverse = rotation.inverse()

        // Applying a rotation then its inverse should return identity
        val result = rotation * inverse

        // The result should be the identity quaternion
        assertQuaternionEquals(Quaternion.IDENTITY, result, delta = 0.01f)
    }

    @Test
    fun testQuaternionMultiplication() {
        // Create two rotations
        val rot1 = Quaternion.fromEuler(pitch = 0f, yaw = PI.toFloat() / 4, roll = 0f) // 45° around Y
        val rot2 = Quaternion.fromEuler(pitch = 0f, yaw = PI.toFloat() / 4, roll = 0f) // 45° around Y

        // Combining two 45° rotations should give a 90° rotation
        val combined = rot1 * rot2

        // Test by rotating forward vector
        val forward = Vector3D(0f, 0f, 1f)
        val rotated = combined * forward

        // Should be approximately (1, 0, 0)
        assertVector3DEquals(Vector3D(1f, 0f, 0f), rotated, delta = 0.01f)
    }

    @Test
    fun testNormalize() {
        // Create a non-unit quaternion
        val q = Quaternion(2f, 0f, 0f, 0f)
        val normalized = q.normalize()

        // Should be unit length (w²+x²+y²+z² = 1)
        val lengthSquared = normalized.w * normalized.w +
                           normalized.x * normalized.x +
                           normalized.y * normalized.y +
                           normalized.z * normalized.z
        assertEquals(1f, lengthSquared, 0.001f)

        // Should be identity
        assertQuaternionEquals(Quaternion.IDENTITY, normalized, delta = 0.01f)
    }

    @Test
    fun testFromAxisAngle() {
        // Create a 90-degree rotation around Y axis using axis-angle
        val rotation = Quaternion.fromAxisAngle(Vector3D.UP, PI.toFloat() / 2)

        // Rotate forward vector
        val forward = Vector3D(0f, 0f, 1f)
        val rotated = rotation * forward

        // Should result in right vector
        assertVector3DEquals(Vector3D(1f, 0f, 0f), rotated, delta = 0.01f)
    }

    @Test
    fun testSlerp() {
        // Create two rotations
        val start = Quaternion.IDENTITY
        val end = Quaternion.fromEuler(pitch = 0f, yaw = PI.toFloat() / 2, roll = 0f) // 90° around Y

        // Interpolate halfway
        val mid = Quaternion.slerp(start, end, 0.5f)

        // Should be approximately 45° rotation
        val forward = Vector3D(0f, 0f, 1f)
        val rotated = mid * forward

        // 45° rotation of (0,0,1) around Y should give approximately (0.707, 0, 0.707)
        val sqrt2over2 = sqrt(2f) / 2f
        assertVector3DEquals(Vector3D(sqrt2over2, 0f, sqrt2over2), rotated, delta = 0.01f)
    }

    @Test
    fun testSlerpEndpoints() {
        val start = Quaternion.IDENTITY
        val end = Quaternion.fromEuler(pitch = 0f, yaw = PI.toFloat() / 2, roll = 0f)

        // t=0 should return start
        val atStart = Quaternion.slerp(start, end, 0f)
        assertQuaternionEquals(start, atStart, delta = 0.01f)

        // t=1 should return end
        val atEnd = Quaternion.slerp(start, end, 1f)
        assertQuaternionEquals(end, atEnd, delta = 0.01f)
    }

    @Test
    fun testRotate180Degrees() {
        // 180-degree rotation around Y axis
        val rotation = Quaternion.fromEuler(pitch = 0f, yaw = PI.toFloat(), roll = 0f)

        // Rotate forward vector
        val forward = Vector3D(0f, 0f, 1f)
        val rotated = rotation * forward

        // Should result in backward vector (-0, 0, -1)
        assertVector3DEquals(Vector3D(0f, 0f, -1f), rotated, delta = 0.01f)
    }

    @Test
    fun testRotateAroundXAxis() {
        // 90-degree rotation around X axis
        val rotation = Quaternion.fromEuler(pitch = PI.toFloat() / 2, yaw = 0f, roll = 0f)

        // Rotate up vector (0, 1, 0)
        val up = Vector3D(0f, 1f, 0f)
        val rotated = rotation * up

        // Should result in forward vector (0, 0, 1)
        assertVector3DEquals(Vector3D(0f, 0f, 1f), rotated, delta = 0.01f)
    }

    @Test
    fun testRotateAroundZAxis() {
        // 90-degree rotation around Z axis
        val rotation = Quaternion.fromEuler(pitch = 0f, yaw = 0f, roll = PI.toFloat() / 2)

        // Rotate right vector (1, 0, 0)
        val right = Vector3D(1f, 0f, 0f)
        val rotated = rotation * right

        // Should result in up vector (0, 1, 0)
        assertVector3DEquals(Vector3D(0f, 1f, 0f), rotated, delta = 0.01f)
    }
}
