package com.keren.virtualmoney.ar.camera

import com.keren.virtualmoney.ar.data.Pose
import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-specific camera and AR tracking provider.
 *
 * Platform implementations should:
 * - Use ARCore on Android when available
 * - Use ARKit on iOS when available
 * - Fall back to device sensors (accelerometer, gyroscope, magnetometer) when AR is unavailable
 * - Provide continuous pose updates through the poseFlow
 *
 * The sensor fallback enables basic tracking on devices without AR support,
 * though with reduced accuracy compared to full AR tracking.
 */
expect class CameraProvider {
    /**
     * Start the AR or sensor tracking session.
     * Should initialize AR framework if available, otherwise fall back to sensors.
     */
    fun startSession()

    /**
     * Stop the tracking session and release all resources.
     * Should properly clean up AR session or sensor listeners.
     */
    fun stopSession()

    /**
     * Reactive stream of pose updates.
     * Emits new Pose values as the device moves and rotates.
     */
    val poseFlow: StateFlow<Pose>

    /**
     * Check if AR is available on this device.
     * @return true if ARCore/ARKit is supported, false otherwise
     */
    fun isARAvailable(): Boolean

    /**
     * Check if currently using AR tracking.
     * @return true if AR is active, false if using sensor fallback
     */
    fun isARActive(): Boolean
}
