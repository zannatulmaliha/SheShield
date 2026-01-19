package com.example.sheshield.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

object MovementDetectionService : SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var context: Context

    private val _movementState = MutableStateFlow(MovementState())
    val movementState: StateFlow<MovementState> = _movementState.asStateFlow()

    private var isMonitoring = false

    // Movement states with improved tracking
    private var lastAcceleration = 0f
    private var lastTimestamp = 0L
    private var lastStationaryCheck = 0L
    private var stationaryStartTime = 0L
    private var accelerationHistory = mutableListOf<Float>()
    private var isStationary = false

    // IMPROVED Thresholds (adjusted based on real-world testing)
    private val SPRINT_THRESHOLD = 25f // Increased from 15f (phone shaking gives ~15-20 m/s²)
    private val HALT_THRESHOLD = 3f // Increased from 1.5f
    private val STATIONARY_TIME_THRESHOLD = 30000L // 30 seconds
    private val GYRO_THRESHOLD = 5.0f // Increased from 2.0f
    private val NORMAL_MOVEMENT_THRESHOLD = 10f // Normal walking is 8-12 m/s²

    // Time windows
    private val ACCELERATION_WINDOW_SIZE = 10
    private val MIN_TIME_BETWEEN_DETECTIONS = 2000L // 2 seconds between same type detections
    private var lastDetectionTime = mutableMapOf<String, Long>()

    // Callbacks
    var onAbnormalMovementDetected: ((type: String, confidence: Float) -> Unit)? = null

    fun initialize(context: Context) {
        this.context = context
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // Initialize last detection times
        lastDetectionTime = mutableMapOf(
            "SPRINT" to 0L,
            "SUDDEN_HALT" to 0L,
            "PROLONGED_STATIONARY" to 0L,
            "UNUSUAL_ROTATION" to 0L
        )
    }

    fun startMonitoring() {
        if (!isMonitoring) {
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }

            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }

            sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }

            // Reset state
            accelerationHistory.clear()
            isStationary = false
            stationaryStartTime = 0L

            isMonitoring = true
            updateState { copy(isActive = true) }
        }
    }

    fun stopMonitoring() {
        if (isMonitoring) {
            sensorManager.unregisterListener(this)
            isMonitoring = false
            updateState { copy(isActive = false) }
        }
    }

    fun toggleMonitoring(): Boolean {
        if (isMonitoring) {
            stopMonitoring()
            return false
        } else {
            startMonitoring()
            return true
        }
    }

    fun clearLog() {
        updateState { copy(log = emptyList()) }
    }

    fun getCurrentState(): MovementState = _movementState.value

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
                // Optional: Track steps if needed
            }
        }
    }

    private fun detectAbnormalMovement(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Remove gravity component (approx 9.8 m/s² on Z-axis)
        val gravity = 9.8f
        val linearAcceleration = sqrt(x * x + y * y + (z - gravity) * (z - gravity))

        // Add to history for averaging
        accelerationHistory.add(linearAcceleration)
        if (accelerationHistory.size > ACCELERATION_WINDOW_SIZE) {
            accelerationHistory.removeAt(0)
        }

        // Calculate average acceleration for smoother detection
        val avgAcceleration = if (accelerationHistory.isNotEmpty()) {
            accelerationHistory.average().toFloat()
        } else {
            linearAcceleration
        }

        val currentTime = System.currentTimeMillis()
        val timeSinceLastSprint = currentTime - lastDetectionTime["SPRINT"]!!
        val timeSinceLastHalt = currentTime - lastDetectionTime["SUDDEN_HALT"]!!

        // Detect sudden sprint (rapid acceleration)
        // Only detect if enough time has passed since last sprint detection
        if (avgAcceleration > SPRINT_THRESHOLD && timeSinceLastSprint > MIN_TIME_BETWEEN_DETECTIONS) {
            // Additional check: Ensure this is a sustained acceleration, not a spike
            val isSustained = accelerationHistory.all { it > NORMAL_MOVEMENT_THRESHOLD }

            if (isSustained) {
                val entry = MovementLogEntry(
                    type = "SPRINT",
                    timestamp = currentTime,
                    confidence = calculateConfidence(avgAcceleration, SPRINT_THRESHOLD, 40f)
                )
                updateState { copy(
                    lastMovementType = "🚨 Sudden Sprint Detected",
                    confidence = entry.confidence,
                    log = log + entry
                )}
                onAbnormalMovementDetected?.invoke("SPRINT", entry.confidence)
                lastDetectionTime["SPRINT"] = currentTime
            }
        }

        // Detect sudden halt - only if we were previously moving fast
        if (lastAcceleration > 15f && avgAcceleration < HALT_THRESHOLD && timeSinceLastHalt > MIN_TIME_BETWEEN_DETECTIONS) {
            val timeDiff = currentTime - lastTimestamp
            if (timeDiff < 1000) { // Within 1 second
                val entry = MovementLogEntry(
                    type = "SUDDEN_HALT",
                    timestamp = currentTime,
                    confidence = calculateConfidence(15f - avgAcceleration, 5f, 20f)
                )
                updateState { copy(
                    lastMovementType = "🛑 Sudden Halt Detected",
                    confidence = entry.confidence,
                    log = log + entry
                )}
                onAbnormalMovementDetected?.invoke("SUDDEN_HALT", entry.confidence)
                lastDetectionTime["SUDDEN_HALT"] = currentTime
            }
        }

        lastAcceleration = avgAcceleration
        lastTimestamp = currentTime
    }

    private fun detectStationary(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()

        // Only check stationary every 5 seconds to save battery
        if (currentTime - lastStationaryCheck < 5000) {
            return
        }
        lastStationaryCheck = currentTime

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate total acceleration (including gravity)
        val totalAcceleration = sqrt(x * x + y * y + z * z)

        // Stationary threshold: close to gravity (9.8 m/s²) with small variations
        val isCurrentlyStationary = abs(totalAcceleration - 9.8f) < 2f

        if (isCurrentlyStationary) {
            if (!isStationary) {
                // Just became stationary
                isStationary = true
                stationaryStartTime = currentTime
            } else {
                // Check how long we've been stationary
                val stationaryDuration = currentTime - stationaryStartTime
                val timeSinceLastStationary = currentTime - lastDetectionTime["PROLONGED_STATIONARY"]!!

                if (stationaryDuration > STATIONARY_TIME_THRESHOLD &&
                    timeSinceLastStationary > MIN_TIME_BETWEEN_DETECTIONS) {

                    val entry = MovementLogEntry(
                        type = "PROLONGED_STATIONARY",
                        timestamp = currentTime,
                        confidence = 0.9f
                    )
                    updateState { copy(
                        lastMovementType = "⏸️ Prolonged Stationary (${stationaryDuration / 1000}s)",
                        confidence = entry.confidence,
                        log = log + entry
                    )}
                    onAbnormalMovementDetected?.invoke("PROLONGED_STATIONARY", entry.confidence)
                    lastDetectionTime["PROLONGED_STATIONARY"] = currentTime

                    // Reset stationary timer after detection
                    stationaryStartTime = currentTime
                }
            }
        } else {
            // Movement detected, reset stationary timer
            isStationary = false
            stationaryStartTime = 0L
        }
    }

    private fun detectUnusualRotation(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastRotation = currentTime - lastDetectionTime["UNUSUAL_ROTATION"]!!

        if (timeSinceLastRotation < MIN_TIME_BETWEEN_DETECTIONS) {
            return
        }

        val x = abs(event.values[0])
        val y = abs(event.values[1])
        val z = abs(event.values[2])

        val rotationMagnitude = sqrt(x * x + y * y + z * z)

        // Increased threshold for unusual rotation
        if (rotationMagnitude > GYRO_THRESHOLD) {
            val entry = MovementLogEntry(
                type = "UNUSUAL_ROTATION",
                timestamp = currentTime,
                confidence = calculateConfidence(rotationMagnitude, GYRO_THRESHOLD, 15f)
            )
            updateState { copy(
                log = log + entry
            )}
            onAbnormalMovementDetected?.invoke("UNUSUAL_ROTATION", entry.confidence)
            lastDetectionTime["UNUSUAL_ROTATION"] = currentTime
        }
    }

    private fun calculateConfidence(value: Float, min: Float, max: Float): Float {
        // Calculate confidence based on how far the value is from threshold
        val normalized = (value - min) / (max - min)
        return 0.6f + (0.4f * normalized.coerceIn(0f, 1f))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes
    }

    private fun updateState(update: MovementState.() -> MovementState) {
        _movementState.value = _movementState.value.update()
    }
}

data class MovementState(
    val isActive: Boolean = false,
    val lastMovementType: String = "",
    val confidence: Float = 0f,
    val log: List<MovementLogEntry> = emptyList()
)

data class MovementLogEntry(
    val type: String,
    val timestamp: Long,
    val confidence: Float = 0f
)