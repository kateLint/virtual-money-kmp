package com.keren.virtualmoney.ar.platform

import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.keren.virtualmoney.ar.core.Transform
import com.keren.virtualmoney.ar.core.Vector3
import com.keren.virtualmoney.ar.math.Quaternion

/**
 * Android implementation of ARPlatformAnchor. Wraps ARCore Anchor if available, or uses a virtual
 * transform for sensor-only mode.
 */
actual class ARPlatformAnchor(
        val arAnchor: Anchor? = null,
        private val virtualTransform: Transform? = null
) {
    actual fun getPose(): Pair<Vector3, Quaternion> {
        if (arAnchor != null) {
            val pose = arAnchor.pose
            return Pair(
                    Vector3(pose.tx(), pose.ty(), pose.tz()),
                    Quaternion(pose.qw(), pose.qx(), pose.qy(), pose.qz())
            )
        }
        // Fallback or virtual anchor
        return if (virtualTransform != null) {
            Pair(virtualTransform.position, virtualTransform.rotation)
        } else {
            Pair(Vector3.ZERO, Quaternion.IDENTITY)
        }
    }

    actual fun detach() {
        arAnchor?.detach()
    }
}

/** Android implementation of ARPlatformContext. Holds the ARCore Session if active. */
actual class ARPlatformContext(val session: Session? = null)

/**
 * Creates an anchor. If Session is provided, uses ARCore. Else creates a virtual anchor (just holds
 * the coordinates).
 */
actual fun createAnchor(position: Vector3, context: ARPlatformContext): ARPlatformAnchor? {
    if (context.session != null) {
        return try {
            // Create pose (position, identity rotation)
            // ARCore Pose(float[] translation, float[] rotation)
            // Rotation is quaternion (x,y,z,w)
            val pose =
                    Pose(
                            floatArrayOf(position.x, position.y, position.z),
                            floatArrayOf(0f, 0f, 0f, 1f)
                    )
            val anchor = context.session.createAnchor(pose)
            ARPlatformAnchor(arAnchor = anchor)
        } catch (e: Exception) {
            null
        }
    } else {
        // Virtual anchor for sensor mode
        return ARPlatformAnchor(virtualTransform = Transform(position, Quaternion.IDENTITY))
    }
}
