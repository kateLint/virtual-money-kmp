package com.keren.virtualmoney.ar.platform

import com.keren.virtualmoney.ar.core.Vector3
import com.keren.virtualmoney.ar.math.Quaternion

/**
 * Platform-agnostic wrapper for an AR anchor.
 *
 * Android: Wraps com.google.ar.core.Anchor iOS: Wraps ARAnchor
 */
expect class ARPlatformAnchor {
    fun getPose(): Pair<Vector3, Quaternion>
    fun detach()
}

/** Context required to create an anchor (e.g., AR Session or View). This is platform specific. */
expect class ARPlatformContext

/** Creates an anchor at the specified world position. */
expect fun createAnchor(position: Vector3, context: ARPlatformContext): ARPlatformAnchor?
