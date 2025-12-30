package com.keren.virtualmoney.ar.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Vector3DTest {

    @Test
    fun testVectorAddition() {
        val v1 = Vector3D(1f, 2f, 3f)
        val v2 = Vector3D(4f, 5f, 6f)
        val result = v1 + v2

        assertEquals(5f, result.x, 0.0001f)
        assertEquals(7f, result.y, 0.0001f)
        assertEquals(9f, result.z, 0.0001f)
    }

    @Test
    fun testVectorSubtraction() {
        val v1 = Vector3D(4f, 5f, 6f)
        val v2 = Vector3D(1f, 2f, 3f)
        val result = v1 - v2

        assertEquals(3f, result.x, 0.0001f)
        assertEquals(3f, result.y, 0.0001f)
        assertEquals(3f, result.z, 0.0001f)
    }

    @Test
    fun testScalarMultiplication() {
        val v = Vector3D(1f, 2f, 3f)
        val result = v * 2f

        assertEquals(2f, result.x, 0.0001f)
        assertEquals(4f, result.y, 0.0001f)
        assertEquals(6f, result.z, 0.0001f)
    }

    @Test
    fun testScalarDivision() {
        val v = Vector3D(2f, 4f, 6f)
        val result = v / 2f

        assertEquals(1f, result.x, 0.0001f)
        assertEquals(2f, result.y, 0.0001f)
        assertEquals(3f, result.z, 0.0001f)
    }

    @Test
    fun testLength() {
        val v = Vector3D(3f, 4f, 0f)
        assertEquals(5f, v.length(), 0.0001f)
    }

    @Test
    fun testLengthSquared() {
        val v = Vector3D(3f, 4f, 0f)
        assertEquals(25f, v.lengthSquared(), 0.0001f)
    }

    @Test
    fun testNormalize() {
        val v = Vector3D(3f, 4f, 0f)
        val normalized = v.normalize()

        assertEquals(0.6f, normalized.x, 0.0001f)
        assertEquals(0.8f, normalized.y, 0.0001f)
        assertEquals(0f, normalized.z, 0.0001f)
        assertEquals(1f, normalized.length(), 0.0001f)
    }

    @Test
    fun testNormalizeZeroVector() {
        val v = Vector3D(0f, 0f, 0f)
        val normalized = v.normalize()

        assertEquals(Vector3D.ZERO, normalized)
    }

    @Test
    fun testDotProduct() {
        val v1 = Vector3D(1f, 2f, 3f)
        val v2 = Vector3D(4f, 5f, 6f)
        val result = v1.dot(v2)

        // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        assertEquals(32f, result, 0.0001f)
    }

    @Test
    fun testCrossProduct() {
        val v1 = Vector3D(1f, 0f, 0f)
        val v2 = Vector3D(0f, 1f, 0f)
        val result = v1.cross(v2)

        // i x j = k
        assertEquals(0f, result.x, 0.0001f)
        assertEquals(0f, result.y, 0.0001f)
        assertEquals(1f, result.z, 0.0001f)
    }

    @Test
    fun testCrossProductAnticommutative() {
        val v1 = Vector3D(1f, 2f, 3f)
        val v2 = Vector3D(4f, 5f, 6f)
        val cross1 = v1.cross(v2)
        val cross2 = v2.cross(v1)

        assertEquals(-cross1.x, cross2.x, 0.0001f)
        assertEquals(-cross1.y, cross2.y, 0.0001f)
        assertEquals(-cross1.z, cross2.z, 0.0001f)
    }

    @Test
    fun testConstants() {
        assertEquals(Vector3D(0f, 0f, 0f), Vector3D.ZERO)
        assertEquals(Vector3D(0f, 1f, 0f), Vector3D.UP)
        assertEquals(Vector3D(0f, 0f, 1f), Vector3D.FORWARD)
        assertEquals(Vector3D(1f, 0f, 0f), Vector3D.RIGHT)
    }

    @Test
    fun testUnaryMinus() {
        val v = Vector3D(1f, -2f, 3f)
        val result = -v

        assertEquals(-1f, result.x, 0.0001f)
        assertEquals(2f, result.y, 0.0001f)
        assertEquals(-3f, result.z, 0.0001f)
    }

    @Test
    fun testDistanceTo() {
        val v1 = Vector3D(0f, 0f, 0f)
        val v2 = Vector3D(3f, 4f, 0f)

        assertEquals(5f, v1.distanceTo(v2), 0.0001f)
        assertEquals(5f, v2.distanceTo(v1), 0.0001f)
    }

    @Test
    fun testOrthogonalityAfterCross() {
        val v1 = Vector3D(1f, 2f, 3f)
        val v2 = Vector3D(4f, 5f, 6f)
        val cross = v1.cross(v2)

        // Cross product should be orthogonal to both input vectors
        assertEquals(0f, cross.dot(v1), 0.0001f)
        assertEquals(0f, cross.dot(v2), 0.0001f)
    }
}
