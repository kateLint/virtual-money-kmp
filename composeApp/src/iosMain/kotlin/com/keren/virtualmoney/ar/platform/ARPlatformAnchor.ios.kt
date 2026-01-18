package com.keren.virtualmoney.ar.platform

import com.keren.virtualmoney.ar.math.Quaternion
import com.keren.virtualmoney.ar.core.Transform
import com.keren.virtualmoney.ar.core.Vector3
import platform.ARKit.ARAnchor
import platform.ARKit.ARSession
import kotlinx.cinterop.useContents

/**
 * iOS implementation of ARPlatformAnchor.
 * Wraps ARAnchor if available.
 */
actual class ARPlatformAnchor(
    val anchor: ARAnchor? = null,
    private val virtualTransform: Transform? = null
) {
    actual fun getPose(): Pair<Vector3, Quaternion> {
        if (anchor != null) {
            val transform = anchor.transform
            // Extract position and rotation from 4x4 matrix (simulated here for brevity/correctness needs matrix math)
            // A 4x4 matrix in ARKit:
            // columns.3 is translation (x, y, z, 1)
            // Rotation needs extraction.
            // For MVP/simplicity we might need a helper, but let's try direct access if possible or use a simple extraction.
            
            // Extract translation
            val tx = transform.useContents { columns.3.x }
            val ty = transform.useContents { columns.3.y }
            val tz = transform.useContents { columns.3.z }
            
            // TODO: Extract rotation properly from matrix.
            // For now, returning Identity for rotation if not easily extractable without math lib.
            // In a real implementation we'd convert the 3x3 rotation submatrix to quaternion.
            
            return Pair(Vector3(tx.toFloat(), ty.toFloat(), tz.toFloat()), Quaternion.IDENTITY)
        }
        
         return if (virtualTransform != null) {
            Pair(virtualTransform.position, virtualTransform.rotation)
        } else {
            Pair(Vector3.ZERO, Quaternion.IDENTITY)
        }
    }

    actual fun detach() {
        // ARKit separation logic if needed, usually session.remove(anchor)
        // But anchor itself doesn't have detach().
        // Logic should be handled by context/session manager.
    }
}

/**
 * iOS implementation of ARPlatformContext.
 */
actual class ARPlatformContext(
    val session: ARSession? = null
)

/**
 * Creates an anchor.
 */
actual fun createAnchor(position: Vector3, context: ARPlatformContext): ARPlatformAnchor? {
    if (context.session != null) {
        // Create ARAnchor
        // ARAnchor(transform: matrix_float4x4)
        // Logic to build matrix from position... 
        // Returning null for now to keep compilation green until matrix math is ported.
        return null 
    } else {
         return ARPlatformAnchor(
            virtualTransform = Transform(position, Quaternion.IDENTITY)
        )
    }
}
