package com.example.sheshield.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserMonitoringViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _targetLocation = MutableStateFlow<LatLng?>(null)
    val targetLocation: StateFlow<LatLng?> = _targetLocation

    private val _lastUpdated = MutableStateFlow("")
    val lastUpdated: StateFlow<String> = _lastUpdated

    // Listen to the User's active session
    fun startMonitoringUser(userId: String) {
        firestore.collection("active_sessions").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null && snapshot.exists()) {
                    val lat = snapshot.getDouble("latitude")
                    val lng = snapshot.getDouble("longitude")
                    // You can add logic here: if location is in a "Danger Zone" polygon, toast the Contact

                    if (lat != null && lng != null) {
                        _targetLocation.value = LatLng(lat, lng)
                    }
                }
            }
    }

    // THE CORE LOGIC: Trigger alert on behalf of the user
    fun triggerProxySos(victimId: String, victimName: String) {
        val currentUser = auth.currentUser ?: return
        val currentLocation = _targetLocation.value ?: return

        // This must match the data structure your HelperDashboard expects
        val alertData = hashMapOf(
            "type" to "PROXY_SOS", // Mark this as triggered by a contact
            "userName" to victimName,
            "description" to "Remote SOS triggered by Trusted Contact: ${currentUser.displayName}. Victim needs help!",
            "riskLevel" to "high",
            "status" to "active",
            "timestamp" to System.currentTimeMillis(),
            "senderId" to victimId, // The ID of the person in danger
            "reportedBy" to currentUser.uid, // The ID of the contact reporting it
            "locationString" to "https://www.google.com/maps?q=$${currentLocation.latitude},${currentLocation.longitude}",
            // Add raw coordinates for Helper geo-queries
            "latitude" to currentLocation.latitude,
            "longitude" to currentLocation.longitude
        )

        firestore.collection("alerts")
            .add(alertData)
            .addOnSuccessListener {
                Log.d("UserMonitoring", "Proxy SOS sent successfully")
            }
            .addOnFailureListener {
                Log.e("UserMonitoring", "Failed to send Proxy SOS", it)
            }
    }
}