package com.keren.virtualmoney.ar.logic

import com.keren.virtualmoney.ar.core.ARCoin
import com.keren.virtualmoney.ar.core.Transform
import com.keren.virtualmoney.ar.core.Vector3
import com.keren.virtualmoney.ar.math.Quaternion
import com.keren.virtualmoney.ar.platform.ARPlatformContext
import com.keren.virtualmoney.ar.platform.createAnchor
import com.keren.virtualmoney.game.GameConfig
import com.keren.virtualmoney.platform.getCurrentTimeMillis
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Manages the logic for AR coins: spawning, lifecycle, and collection. This class contains the core
 * game logic in shared KMP code.
 */
class CoinManager(private val config: GameConfig) {
    private val activeCoins = mutableListOf<ARCoin>()
    private val coinPool = ArrayDeque<ARCoin>() // Object pooling (conceptually)

    // Config values from GameConfig
    private val maxCoins: Int
        get() = config.visibleCoinCap
    private val targetGoodCoins: Int
        get() = ((1 - config.badCoinRatio) * maxCoins).toInt()
    private val targetBadCoins: Int
        get() = (config.badCoinRatio * maxCoins).toInt()
    private val minSpawnDistance: Float
        get() = config.coinDistanceMin
    private val maxSpawnDistance: Float
        get() = config.coinDistanceMax
    private val coinLifetimeMs: Long
        get() = config.coinLifetimeMs

    // Respawn Queue
    private data class RespawnTask(val executeTime: Long)
    private val respawnQueue = mutableListOf<RespawnTask>()

    /** Initial spawn of coins around the user. */
    fun spawnInitialCoins(
            cameraPosition: Vector3,
            cameraRotation: Quaternion,
            context: ARPlatformContext
    ) {
        activeCoins.clear()
        respawnQueue.clear()

        // Spawn initial batch up to maxCoins
        repeat(maxCoins) { spawnRandomCoin(cameraPosition, cameraRotation, context) }
    }

    /**
     * Updates coin states (check lifetimes) and returns active coins. Should be called every frame.
     */
    fun update(cameraTransform: Transform, context: ARPlatformContext): List<ARCoin> {
        val currentTime = getCurrentTimeMillis()
        val iterator = activeCoins.iterator()

        // 1. Manage Active Coins
        while (iterator.hasNext()) {
            val coin = iterator.next()
            if (currentTime - coin.spawnTime > coin.lifetimeMs) {
                // Timeout - remove and detach anchor
                coin.anchor?.detach()
                iterator.remove()

                // Schedule respawn (delayed)
                scheduleRespawn(currentTime)
            } else {
                // Update position from anchor if available (handle AR drift)
                coin.anchor?.let { anchor ->
                    val (newPos, _) = anchor.getPose()
                    // Will be updated in separate loop to avoid concurrent mod
                }
            }
        }

        // Update positions separately
        for (i in activeCoins.indices) {
            val coin = activeCoins[i]
            coin.anchor?.let { anchor ->
                val (newPos, _) = anchor.getPose()
                activeCoins[i] = coin.copy(worldPosition = newPos)
            }
        }

        // 2. Process Respawn Queue
        val respawnIterator = respawnQueue.iterator()
        while (respawnIterator.hasNext()) {
            val task = respawnIterator.next()
            if (currentTime >= task.executeTime) {
                respawnIterator.remove()
                spawnRandomCoin(cameraTransform.position, cameraTransform.rotation, context)
            }
        }

        return activeCoins
    }

    /**
     * Handles coin collection.
     * @return true if a coin was collected, false otherwise.
     */
    fun onCoinCollected(coinId: String): Boolean {
        val coin = activeCoins.find { it.id == coinId } ?: return false

        // Remove and detach
        coin.anchor?.detach()
        activeCoins.remove(coin)

        // Schedule respawn (8-12s delay)
        scheduleRespawn(getCurrentTimeMillis())

        return true
    }

    fun getActiveCoins(): List<ARCoin> = activeCoins

    private fun scheduleRespawn(currentTime: Long) {
        // Respawn delay from config (default 500-1500ms instead of 8-12s)
        val delayMs = Random.nextLong(config.respawnDelayMinMs, config.respawnDelayMaxMs + 1)
        respawnQueue.add(RespawnTask(currentTime + delayMs))
    }

    private fun spawnRandomCoin(
            cameraPos: Vector3,
            cameraRot: Quaternion,
            context: ARPlatformContext
    ) {
        if (activeCoins.size >= maxCoins) return

        // Determine Type (Good vs Bad) based on current counts
        // 0 = Good (Hapoalim), 1 = Bad (Penalty)
        // We assume type is stored in ARCoin.type (which is Int currently)
        val currentBad = activeCoins.count { it.type == 1 }
        val currentGood = activeCoins.count { it.type == 0 }

        val wantBad = currentBad < targetBadCoins
        val wantGood = currentGood < targetGoodCoins

        val isBad =
                if (wantBad && wantGood) {
                    Random.nextBoolean() // Both needed, random
                } else if (wantBad) {
                    true // Only bad needed
                } else if (wantGood) {
                    false // Only good needed
                } else {
                    // Full on both? Shouldn't happen if checking size < MAX, but fallback to random
                    // allowed
                    Random.nextFloat() < 0.4f
                }

        val coinType = if (isBad) 1 else 0

        // Random distance: 3-5 meters
        val dist = Random.nextFloat() * (maxSpawnDistance - minSpawnDistance) + minSpawnDistance

        // Height: "floor to 2.5m ceiling"
        // Camera average height ~1.5m. Floor is at -1.5m relative, Ceiling at +1.0m.
        val heightOffset = Random.nextFloat() * 2.5f - 1.5f

        // Angle Calculation
        // If BAD coin, ensure NOT in front 90 degree cone
        // Forward vector comes from cameraRot
        // We calculate a random angle around Y
        var angleRad = Random.nextFloat() * 2 * PI.toFloat()

        if (isBad) {
            // "No bad coin ... in forward 90° view"
            // Let's interpret "Front" as Angle 0 relative to camera forward.
            // 90 deg view means +/- 45 degrees.
            // So unsafe region is [-PI/4, +PI/4].
            // We want angle in [PI/4, 2PI - PI/4] (approx).

            // Re-generate angle until outside unsafe cone?
            // Easier: Generate in [PI/4, 7*PI/4] range relative to forward.
            // Range width = 2PI - PI/2 = 3PI/2.
            // Offset start = PI/4.
            val safeRange = (3 * PI / 2).toFloat()
            val relativeAngle = Random.nextFloat() * safeRange + (PI / 4).toFloat()
            // Add to camera yaw
            // Extraction of yaw from Quaternion is complex, but we can just use
            // the rotation logic we have.
            // Actually, we are calculating "x" and "z" offset relative to camera orientation.
            // We need to apply camera rotation to the offset vector.
            // Simpler: Generate offset vector in local space, then rotate by cameraRot.

            // In local space, "Forward" is -Z (usually in AR/GL) or +Z?
            // Let's assume standard AR: -Z is forward.
            // Cone is +/- 45 deg around -Z.
            // We pick angle outside that.

            // However, our current math `x = dist * sin(angle); z = dist * cos(angle)`
            // creates a circle. This is inherently "around Y axis" but unaligned with Look Dir
            // unless we rotate.
            // The previous code had:
            // "val spawnPos = Vector3(cameraPos.x + x, ...)"
            // This adds absolute X/Z offsets. It ignored camera rotation!
            // This means coins spawned "Forward" (North), not "In front of player".
            // To respect "Forward cone", we MUST use camera rotation.
            // The fix was implemented in previous steps but logic remains here.

            // Fix:
            // 1. Generate local vector (x, 0, z) based on angle.
            // 2. Rotate this vector by `cameraRot`.
            // 3. Add to `cameraPos`.

            // For Bad Coins: Restrict local angle.
            // Local Forward (standard) is typically -Z.
            // Angle 0 = -Z?
            // Let's assume Angle 0 = +Z (South), PI = -Z (North/Forward).
            // Cone +/- 45 deg around -Z means Angle in [PI - PI/4, PI + PI/4].
            // We want outside that.

            // Actually, simplest is:
            val randomAngle =
                    if (isBad) {
                        // Avoid front 90 deg (+/- 45 deg)
                        // Shift by 180 (PI) to face back?
                        // Or just pick from [45, 315] degrees.
                        val range = (2 * PI - PI / 2).toFloat() // 270 degrees available
                        val start = (PI / 4).toFloat() // Start at 45 deg
                        start + Random.nextFloat() * range
                    } else {
                        Random.nextFloat() * 2 * PI.toFloat()
                    }

            angleRad = randomAngle
        }

        // Create local offset vector (flat on XZ) assuming Forward is -Z (Angle PI) or similar.
        // Standard trig: x = r*sin(theta), z = r*cos(theta).
        // If theta=0, x=0, z=r.
        // We want to rotate this by cameraRot.

        val localX = dist * sin(angleRad)
        val localZ = dist * cos(angleRad)
        val localPos = Vector3(localX, heightOffset, localZ)

        // Rotate localPos by cameraRot
        val rotatedPos = rotateVector(localPos, cameraRot)

        // World Pos
        val spawnPos =
                Vector3(
                        cameraPos.x + rotatedPos.x,
                        cameraPos.y + rotatedPos.y,
                        cameraPos.z + rotatedPos.z
                )

        // Create Anchor
        val anchor = createAnchor(spawnPos, context)

        val coin =
                ARCoin(
                        id = "coin_${getCurrentTimeMillis()}_${Random.nextInt()}",
                        worldPosition = spawnPos,
                        spawnTime = getCurrentTimeMillis(),
                        lifetimeMs = coinLifetimeMs,
                        anchor = anchor,
                        type = coinType
                )

        activeCoins.add(coin)
    }

    // Helper to rotate vector by quaternion
    private fun rotateVector(v: Vector3, q: Quaternion): Vector3 {
        // v' = q * v * q_inv
        // Standard formula
        val x = v.x
        val y = v.y
        val z = v.z

        val qx = q.x
        val qy = q.y
        val qz = q.z
        val qw = q.w

        val ix = qw * x + qy * z - qz * y
        val iy = qw * y + qz * x - qx * z
        val iz = qw * z + qx * y - qy * x
        val iw = -qx * x - qy * y - qz * z

        val finalX = ix * qw + iw * -qx + iy * -qz - iz * -qy
        val finalY = iy * qw + iw * -qy + iz * -qx - ix * -qz
        val finalZ = iz * qw + iw * -qz + ix * -qy - iy * -qx

        return Vector3(finalX, finalY, finalZ)
    }
}
