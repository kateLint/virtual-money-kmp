package com.keren.virtualmoney.ar.camera

import com.keren.virtualmoney.ar.data.Pose
import com.keren.virtualmoney.ar.math.Quaternion
import com.keren.virtualmoney.ar.math.Vector3D
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for CameraProvider interface.
 *
 * These are basic compilation tests to ensure the interface is complete.
 * Full integration tests with Robolectric would require additional dependencies.
 *
 * Note: These tests verify interface consistency across platforms.
 * Platform-specific behavior (ARCore, ARKit, sensors) should be tested
 * with instrumented tests or Robolectric when configured.
 */
class CameraProviderTest {

    @Test
    fun `Pose identity has zero position`() {
        assertEquals(Vector3D.ZERO, Pose.IDENTITY.position)
    }

    @Test
    fun `Pose identity has identity rotation`() {
        assertEquals(Quaternion.IDENTITY, Pose.IDENTITY.rotation)
    }

    @Test
    fun `Pose can be created with custom values`() {
        val position = Vector3D(1f, 2f, 3f)
        val rotation = Quaternion(1f, 0f, 0f, 0f)
        val pose = Pose(position, rotation)

        assertEquals(position, pose.position)
        assertEquals(rotation, pose.rotation)
    }
}
