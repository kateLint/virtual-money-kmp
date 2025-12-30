package com.keren.virtualmoney.ar.camera

import android.content.Context
import android.util.Log
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableException
import com.keren.virtualmoney.ar.data.Pose
import com.keren.virtualmoney.ar.math.Quaternion
import com.keren.virtualmoney.ar.math.Vector3D
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of CameraProvider using ARCore.
 *
 * This implementation:
 * - Uses ARCore for AR tracking when available
 * - Falls back to sensor-based tracking (accelerometer, gyroscope, magnetometer) when ARCore is unavailable
 * - Provides pose updates through a StateFlow
 * - Converts ARCore's coordinate system to our common Pose representation
 */
actual class CameraProvider(private val context: Context) {

    private var arSession: Session? = null
    private var sensorPoseTracker: SensorPoseTracker? = null
    private var isUsingAR = false

    private val _poseFlow = MutableStateFlow(Pose())
    actual val poseFlow: StateFlow<Pose> = _poseFlow.asStateFlow()

    /**
     * Start the AR or sensor tracking session.
     * Attempts to initialize ARCore first, falls back to sensors if unavailable.
     */
    actual fun startSession() {
        try {
            // Check if ARCore is supported and installed
            when (ArCoreApk.getInstance().checkAvailability(context)) {
                ArCoreApk.Availability.SUPPORTED_INSTALLED -> {
                    // ARCore is available, create session
                    arSession = Session(context).apply {
                        // Configure session
                        val config = Config(this).apply {
                            // Enable auto focus
                            focusMode = Config.FocusMode.AUTO
                            // Use WORLD_TRACKING mode for 6DOF tracking
                            updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                        }
                        configure(config)
                    }
                    isUsingAR = true
                    Log.i(TAG, "ARCore session started successfully")
                }
                else -> {
                    // ARCore not available, fall back to sensors
                    fallbackToSensors()
                }
            }
        } catch (e: UnavailableException) {
            Log.w(TAG, "ARCore unavailable: ${e.message}, falling back to sensors")
            fallbackToSensors()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start ARCore: ${e.message}", e)
            fallbackToSensors()
        }
    }

    /**
     * Stop the tracking session and release all resources.
     */
    actual fun stopSession() {
        try {
            arSession?.close()
            arSession = null

            sensorPoseTracker?.stop()
            sensorPoseTracker = null

            isUsingAR = false
            Log.i(TAG, "Tracking session stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping session: ${e.message}", e)
        }
    }

    /**
     * Update the current pose from ARCore or sensors.
     * Should be called every frame to get the latest tracking data.
     */
    fun updatePose() {
        if (isUsingAR) {
            updatePoseFromARCore()
        } else {
            updatePoseFromSensors()
        }
    }

    /**
     * Update pose from ARCore tracking.
     */
    private fun updatePoseFromARCore() {
        try {
            val session = arSession ?: return

            // Update the session to get the latest frame
            val frame = session.update()

            // Get camera pose from the frame
            val cameraPose = frame.camera.pose

            // Convert ARCore pose to our Pose representation
            // ARCore uses: translation (tx, ty, tz) and quaternion (qx, qy, qz, qw)
            val pose = Pose(
                position = Vector3D(
                    cameraPose.tx(),
                    cameraPose.ty(),
                    cameraPose.tz()
                ),
                rotation = Quaternion(
                    w = cameraPose.qw(),
                    x = cameraPose.qx(),
                    y = cameraPose.qy(),
                    z = cameraPose.qz()
                )
            )

            _poseFlow.value = pose
        } catch (e: Exception) {
            Log.e(TAG, "Error updating pose from ARCore: ${e.message}", e)
        }
    }

    /**
     * Update pose from sensor-based tracking.
     * Note: With SensorPoseTracker, pose updates are pushed via callback,
     * so this method doesn't need to do anything. The callback directly
     * updates the pose flow.
     */
    private fun updatePoseFromSensors() {
        // Sensor updates are handled via the onPoseUpdate callback
        // in SensorPoseTracker, which directly updates _poseFlow
    }

    /**
     * Check if AR is available on this device.
     * @return true if ARCore is supported and installed, false otherwise
     */
    actual fun isARAvailable(): Boolean {
        return try {
            ArCoreApk.getInstance().checkAvailability(context) ==
                ArCoreApk.Availability.SUPPORTED_INSTALLED
        } catch (e: Exception) {
            Log.e(TAG, "Error checking AR availability: ${e.message}", e)
            false
        }
    }

    /**
     * Check if currently using AR tracking.
     * @return true if AR is active, false if using sensor fallback
     */
    actual fun isARActive(): Boolean {
        return isUsingAR && arSession != null
    }

    /**
     * Fall back to sensor-based tracking when ARCore is unavailable.
     */
    private fun fallbackToSensors() {
        sensorPoseTracker = SensorPoseTracker(context) { pose ->
            // Update pose flow when sensor data changes
            _poseFlow.value = pose
        }
        sensorPoseTracker?.start()
        isUsingAR = false
        Log.i(TAG, "Using sensor-based tracking fallback")
    }

    companion object {
        private const val TAG = "CameraProvider"
    }
}
