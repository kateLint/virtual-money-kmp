package com.keren.virtualmoney.ar.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.keren.virtualmoney.ar.data.Pose
import com.keren.virtualmoney.ar.math.Quaternion
import com.keren.virtualmoney.ar.math.Vector3D
import kotlin.math.sqrt

/**
 * Tracks device pose using sensor data (rotation vector sensor).
 * This is used as a fallback when ARCore is not available.
 *
 * The rotation vector sensor provides device orientation by fusing data from
 * accelerometer, gyroscope, and magnetometer. It outputs a rotation vector
 * that can be converted to a rotation matrix and then to a quaternion.
 *
 * Note: This implementation only tracks orientation (rotation), not position.
 * Position is kept at the origin (0, 0, 0) since sensors don't provide positional tracking.
 *
 * @property context Android context for accessing system services
 * @property onPoseUpdate Callback invoked when a new pose is available
 */
class SensorPoseTracker(
    private val context: Context,
    private val onPoseUpdate: (Pose) -> Unit
) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val rotationVectorSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private var isTracking = false

    /**
     * Start tracking device orientation using sensors.
     * Registers a listener for the rotation vector sensor.
     */
    fun start() {
        if (rotationVectorSensor == null) {
            Log.e(TAG, "Rotation vector sensor not available on this device")
            return
        }

        val success = sensorManager.registerListener(
            this,
            rotationVectorSensor,
            SensorManager.SENSOR_DELAY_GAME // Good balance between accuracy and battery
        )

        if (success) {
            isTracking = true
            Log.i(TAG, "Sensor pose tracking started")
        } else {
            Log.e(TAG, "Failed to register sensor listener")
        }
    }

    /**
     * Stop tracking and unregister sensor listeners.
     */
    fun stop() {
        if (isTracking) {
            sensorManager.unregisterListener(this)
            isTracking = false
            Log.i(TAG, "Sensor pose tracking stopped")
        }
    }

    /**
     * Called when sensor values change.
     * Converts rotation vector to quaternion and updates pose.
     *
     * @param event The sensor event containing rotation vector data
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) {
            return
        }

        try {
            // Convert rotation vector to rotation matrix
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            // Convert rotation matrix to quaternion
            val quaternion = rotationMatrixToQuaternion(rotationMatrix)

            // Create pose with orientation from sensors and position at origin
            val pose = Pose(
                position = Vector3D.ZERO, // Sensors don't provide position
                rotation = quaternion
            )

            // Notify callback with new pose
            onPoseUpdate(pose)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing sensor data: ${e.message}", e)
        }
    }

    /**
     * Called when sensor accuracy changes.
     * We log this for debugging but don't need to take action.
     *
     * @param sensor The sensor whose accuracy changed
     * @param accuracy The new accuracy value
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE ->
                Log.w(TAG, "Sensor accuracy: UNRELIABLE")
            SensorManager.SENSOR_STATUS_ACCURACY_LOW ->
                Log.w(TAG, "Sensor accuracy: LOW")
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM ->
                Log.d(TAG, "Sensor accuracy: MEDIUM")
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH ->
                Log.d(TAG, "Sensor accuracy: HIGH")
        }
    }

    /**
     * Converts a 3x3 rotation matrix to a quaternion.
     * Uses the trace-based algorithm with 4 cases for numerical stability.
     *
     * The rotation matrix is in row-major order:
     * [ m0  m1  m2 ]
     * [ m3  m4  m5 ]
     * [ m6  m7  m8 ]
     *
     * @param m The rotation matrix as a 9-element float array (row-major)
     * @return A normalized quaternion representing the same rotation
     */
    private fun rotationMatrixToQuaternion(m: FloatArray): Quaternion {
        // Calculate the trace of the matrix
        val trace = m[0] + m[4] + m[8]

        // Choose algorithm based on which component will be largest
        // This ensures numerical stability
        val w: Float
        val x: Float
        val y: Float
        val z: Float

        if (trace > 0) {
            // w is the largest component
            val s = sqrt(trace + 1.0f) * 2f // s = 4 * w
            w = s * 0.25f
            x = (m[7] - m[5]) / s
            y = (m[2] - m[6]) / s
            z = (m[3] - m[1]) / s
        } else if (m[0] > m[4] && m[0] > m[8]) {
            // x is the largest component
            val s = sqrt(1.0f + m[0] - m[4] - m[8]) * 2f // s = 4 * x
            w = (m[7] - m[5]) / s
            x = s * 0.25f
            y = (m[1] + m[3]) / s
            z = (m[2] + m[6]) / s
        } else if (m[4] > m[8]) {
            // y is the largest component
            val s = sqrt(1.0f + m[4] - m[0] - m[8]) * 2f // s = 4 * y
            w = (m[2] - m[6]) / s
            x = (m[1] + m[3]) / s
            y = s * 0.25f
            z = (m[5] + m[7]) / s
        } else {
            // z is the largest component
            val s = sqrt(1.0f + m[8] - m[0] - m[4]) * 2f // s = 4 * z
            w = (m[3] - m[1]) / s
            x = (m[2] + m[6]) / s
            y = (m[5] + m[7]) / s
            z = s * 0.25f
        }

        // Create and normalize the quaternion
        return Quaternion(w, x, y, z).normalize()
    }

    companion object {
        private const val TAG = "SensorPoseTracker"
    }
}
