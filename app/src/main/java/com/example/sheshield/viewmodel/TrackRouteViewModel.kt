package com.example.sheshield.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheshield.repository.ContactsRepository
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrackRouteViewModel : ViewModel() {

    // Dependencies
    private val contactsRepository = ContactsRepository()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // State
    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

    /**
     * Uploads the user's current location to Firestore in real-time.
     * Call this whenever the location updates (e.g., from your LocationCallback).
     * This allows Trusted Contacts to view the user on the "UserMonitoringScreen".
     */
    fun uploadLiveLocation(location: LatLng) {
        val userId = auth.currentUser?.uid ?: return
        val userName = auth.currentUser?.displayName ?: "User"

        val sessionData = hashMapOf(
            "userName" to userName,
            "isLive" to true,
            "lastUpdated" to System.currentTimeMillis(),
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "status" to "safe" // Default status. Can be updated to "danger" or "missed_check_in"
        )

        // Save to a public-ish document that Trusted Contacts can read
        firestore.collection("active_sessions").document(userId)
            .set(sessionData, SetOptions.merge())
            .addOnFailureListener { e ->
                Log.e("TrackRouteVM", "Failed to upload live location", e)
            }
    }

    /**
     * Stops sharing location by updating the Firestore flag.
     * Call this when the user exits the screen or stops tracking.
     */
    fun stopSharing() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("active_sessions").document(userId)
            .update("isLive", false)
            .addOnSuccessListener {
                Log.d("TrackRouteVM", "Stopped sharing location")
            }
    }

    /**
     * Sends an SMS with a Google Maps link to all trusted contacts.
     */
    fun notifyContacts(context: Context, location: LatLng?) {
        if (location == null) {
            Toast.makeText(context, "Waiting for location...", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _loadingState.value = true
            try {
                // 1. Fetch contacts
                val contacts = contactsRepository.getContacts()

                if (contacts.isEmpty()) {
                    Toast.makeText(context, "No trusted contacts found!", Toast.LENGTH_LONG).show()
                    _loadingState.value = false
                    return@launch
                }

                // 2. Create the Google Maps link
                // Note: Fixed URL format to be standard Google Maps link
                // ✅ NEW (Standard Google Maps Universal Link)
                val mapLink = "https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
                val message = "I'm sharing my live route securely via SheShield. Track me here: $mapLink"

                // 3. Send SMS to each contact
                var sentCount = 0
                contacts.forEach { contact ->
                    if (contact.phone.isNotBlank()) {
                        val success = sendSMS(context, contact.phone, message)
                        if (success) sentCount++
                    }
                }

                // 4. Also ensure Firestore is updated immediately when notifying
                uploadLiveLocation(location)

                // 5. Show result
                if (sentCount > 0) {
                    Toast.makeText(context, "Sent location to $sentCount contacts!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to send SMS. Check permissions/signal.", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _loadingState.value = false
            }
        }
    }

    private fun sendSMS(context: Context, phoneNumber: String, message: String): Boolean {
        return try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
                return false
            }

            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
