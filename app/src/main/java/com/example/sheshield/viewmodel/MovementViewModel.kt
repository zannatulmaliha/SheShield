package com.example.sheshield.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheshield.services.MovementDetectionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovementViewModel : ViewModel() {
    private val _permissionState = MutableStateFlow(PermissionState())
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    val movementState = MovementDetectionService.movementState

    fun initialize(context: Context) {
        MovementDetectionService.initialize(context)
    }

    fun setAbnormalMovementCallback(callback: (type: String, confidence: Float) -> Unit) {
        MovementDetectionService.onAbnormalMovementDetected = callback
    }

    fun startMonitoring() {
        MovementDetectionService.startMonitoring()
    }

    fun stopMonitoring() {
        MovementDetectionService.stopMonitoring()
    }

    fun toggleMonitoring(): Boolean {
        return MovementDetectionService.toggleMonitoring()
    }

    fun clearLog() {
        MovementDetectionService.clearLog()
    }

    // New function to clear logs by time range
    fun clearLogsBefore(timestamp: Long) {
        viewModelScope.launch {
            // Get current state
            val currentState = MovementDetectionService.getCurrentState()
            // Filter out logs before the timestamp
            val filteredLogs = currentState.log.filter { it.timestamp >= timestamp }
            // Update service with filtered logs (you'll need to add this method to the service)
            // For now, we'll just clear all logs if implementing time-based filtering
            MovementDetectionService.clearLog()
        }
    }

    fun clearLogsByTypes(types: List<String>) {
        viewModelScope.launch {
            val currentState = MovementDetectionService.getCurrentState()
            val filteredLogs = currentState.log.filterNot { types.contains(it.type) }
            // Update service with filtered logs
            // You'll need to implement a method in MovementDetectionService to replace logs
        }
    }

    fun getCurrentState() = MovementDetectionService.getCurrentState()

    fun updatePermissionState(permission: String, isGranted: Boolean) {
        viewModelScope.launch {
            _permissionState.value = _permissionState.value.copy(
                activityRecognitionGranted = when (permission) {
                    android.Manifest.permission.ACTIVITY_RECOGNITION -> isGranted
                    else -> _permissionState.value.activityRecognitionGranted
                },
                bodySensorsGranted = when (permission) {
                    android.Manifest.permission.BODY_SENSORS -> isGranted
                    else -> _permissionState.value.bodySensorsGranted
                },
                locationGranted = when (permission) {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION -> isGranted
                    else -> _permissionState.value.locationGranted
                }
            )
        }
    }

    fun hasRequiredPermissions(): Boolean {
        val state = _permissionState.value
        return state.activityRecognitionGranted || state.bodySensorsGranted
    }
}

data class PermissionState(
    val activityRecognitionGranted: Boolean = false,
    val bodySensorsGranted: Boolean = false,
    val locationGranted: Boolean = false
) {
    val hasRequiredPermissions: Boolean
        get() = activityRecognitionGranted || bodySensorsGranted
}