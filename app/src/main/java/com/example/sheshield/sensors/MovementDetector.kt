package com.example.sheshield.sensors

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import kotlin.math.abs
import kotlin.math.sqrt

class MovementDetector(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    // Movement states
    private var lastAcceleration = 0f
    private var lastTimestamp = 0L
    private var isStationary = false
    private var stationaryStartTime = 0L
    private var stepCount = 0
    private var lastStepTime = 0L

    // Thresholds (can be customized)
    private val SPRINT_THRESHOLD = 15f // m/s² - high acceleration indicates running
    private val HALT_THRESHOLD = 1.5f // m/s² - very low acceleration
    private val STATIONARY_TIME_THRESHOLD = 30000L // 30 seconds
    private val GYRO_THRESHOLD = 2.0f // rad/s for unusual rotation

    // Callbacks
    var onSprintDetected: (() -> Unit)? = null
    var onSuddenHaltDetected: (() -> Unit)? = null
    var onProlongedStationary: (() -> Unit)? = null
    var onAbnormalMovement: ((type: String, confidence: Float) -> Unit)? = null

    fun startMonitoring() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        stepDetector?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopMonitoring() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                detectAbnormalMovement(event)
                detectStationary(event)
            }
            Sensor.TYPE_GYROSCOPE -> {
                detectUnusualRotation(event)
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                detectStepPattern(event)
            }
        }
    }

    private fun detectAbnormalMovement(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate acceleration magnitude
        val acceleration = sqrt(x * x + y * y + z * z)

        // Detect sudden sprint (rapid acceleration)
        if (acceleration > SPRINT_THRESHOLD && lastTimestamp > 0) {
            val timeDiff = System.currentTimeMillis() - lastTimestamp
            if (timeDiff < 1000) { // Within 1 second
                onSprintDetected?.invoke()
                onAbnormalMovement?.invoke("SPRINT", 0.85f)
            }
        }

        // Detect sudden halt
        if (lastAcceleration > 8f && acceleration < HALT_THRESHOLD) {
            val timeDiff = System.currentTimeMillis() - lastTimestamp
            if (timeDiff < 500) { // Within 0.5 seconds
                onSuddenHaltDetected?.invoke()
                onAbnormalMovement?.invoke("SUDDEN_HALT", 0.75f)
            }
        }

        lastAcceleration = acceleration
        lastTimestamp = System.currentTimeMillis()
    }

    private fun detectStationary(event: SensorEvent) {
        val acceleration = sqrt(
            event.values[0] * event.values[0] +
                    event.values[1] * event.values[1] +
                    event.values[2] * event.values[2]
        )

        if (acceleration < 1.2f) { // Very little movement
            if (!isStationary) {
                isStationary = true
                stationaryStartTime = System.currentTimeMillis()
            } else {
                val stationaryDuration = System.currentTimeMillis() - stationaryStartTime
                if (stationaryDuration > STATIONARY_TIME_THRESHOLD) {
                    onProlongedStationary?.invoke()
                    onAbnormalMovement?.invoke("PROLONGED_STATIONARY", 0.9f)
                }
            }
        } else {
            isStationary = false
            stationaryStartTime = 0L
        }
    }

    private fun detectUnusualRotation(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val rotationMagnitude = sqrt(x * x + y * y + z * z)

        if (rotationMagnitude > GYRO_THRESHOLD) {
            // Sudden spinning or falling motion
            onAbnormalMovement?.invoke("UNUSUAL_ROTATION", 0.7f)
        }
    }

    private fun detectStepPattern(event: SensorEvent) {
        stepCount++
        val currentTime = System.currentTimeMillis()

        // Detect running pattern (rapid steps)
        if (lastStepTime > 0) {
            val stepInterval = currentTime - lastStepTime
            if (stepInterval < 300) { // Less than 300ms between steps = running
                onAbnormalMovement?.invoke("RUNNING_PATTERN", 0.8f)
            }
        }

        lastStepTime = currentTime
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }
}