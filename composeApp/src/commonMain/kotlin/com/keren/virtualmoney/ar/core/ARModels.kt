package com.keren.virtualmoney.ar.core

import com.keren.virtualmoney.ar.math.Quaternion
import com.keren.virtualmoney.ar.math.Vector3D
import com.keren.virtualmoney.ar.platform.ARPlatformAnchor

// Typealias to satisfy "Vector3" requirement while using existing Vector3D
typealias Vector3 = Vector3D

/** Represents a position and rotation in 3D space. */
data class Transform(val position: Vector3, val rotation: Quaternion)

/**
 * Represents a virtual coin in the AR world.
 *
 * @property id Unique identifier for the coin
 * @property worldPosition The fixed 3D position in the world
 * @property spawnTime Timestamp when the coin was created
 * @property lifetimeMs How long the coin should exist before disappearing
 * @property anchor Platform-specific AR anchor (optional)
 */
data class ARCoin(
        val id: String,
        val worldPosition: Vector3,
        val spawnTime: Long,
        val lifetimeMs: Long = 5000,
        val type: Int = 0, // Adding type to support different coin values/visuals
        val anchor: ARPlatformAnchor? = null
)
