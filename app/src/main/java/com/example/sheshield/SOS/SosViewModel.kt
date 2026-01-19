package com.example.sheshield.SOS

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheshield.repository.ContactsRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

enum class SosState {
    IDLE,
    COUNTDOWN,
    SENT,           // Triggers sending
    SENDING,        // Actually sending
    SENT_SUCCESS,   // Success shown
    CANCELLED
}

class SosViewModel(
    private val contactsRepository: ContactsRepository = ContactsRepository(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) : ViewModel() {

    private val _sosState = MutableStateFlow(SosState.IDLE)
    val sosState: StateFlow<SosState> = _sosState

    private val _alertMessage = MutableStateFlow<String?>(null)
    val alertMessage: StateFlow<String?> = _alertMessage

    private var sosJob: Job? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Initialize location client
     */
    fun initLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    /**
     * Start SOS countdown
     */
    fun startSos(seconds: Int = 5) {
        if (_sosState.value == SosState.COUNTDOWN) return

        _sosState.value = SosState.COUNTDOWN

        sosJob = viewModelScope.launch {
            delay(seconds * 1000L)

            if (_sosState.value == SosState.COUNTDOWN) {
                _sosState.value = SosState.SENT // Trigger sending
            }
        }
    }

    /**
     * Cancel SOS
     */
    fun cancelSos() {
        sosJob?.cancel()
        _sosState.value = SosState.CANCELLED

        // Reset to IDLE after 2 seconds
        viewModelScope.launch {
            delay(2000)
            _sosState.value = SosState.IDLE
        }
    }

    /**
     * MAIN FUNCTION: Send SOS Alert via SMS, Email, and Push Notifications
     */
    fun sendSosAlert(context: Context) {
        viewModelScope.launch {
            try {
                // Show sending state
                _sosState.value = SosState.SENDING
                _alertMessage.value = "📤 Sending alerts..."

                // 1. Get contacts first
                val contacts = contactsRepository.getContacts()

                if (contacts.isEmpty()) {
                    _alertMessage.value = "⚠️ No emergency contacts found. Please add contacts first."
                    delay(3000)
                    resetState()
                    return@launch
                }

                // 2. Get user location
                val location = getLocation(context)
                val locationString = if (location != null) {
                    "https://www.google.com/maps?q=${location.latitude},${location.longitude}"
                } else {
                    "Location unavailable - GPS disabled or no permission"
                }

                // 3. Get user info
                val currentUser = auth.currentUser
                val userName = currentUser?.displayName ?: currentUser?.email ?: "A SheShield user"

                // 4. Create timestamp
                val timestamp = java.text.SimpleDateFormat(
                    "MMM dd, yyyy HH:mm",
                    java.util.Locale.getDefault()
                ).format(java.util.Date())

                // 5. Create SMS message (keep it short for SMS)
                val smsMessage = """
🚨 EMERGENCY - ${userName} needs help!
Time: $timestamp
Location: $locationString
Sent via SheShield
                """.trimIndent()

                // 6. Send SMS to all contacts IMMEDIATELY
                var smsCount = 0
                var smsFailed = 0

                Log.d("SosViewModel", "Starting to send SMS to ${contacts.size} contacts")

                contacts.forEach { contact ->
                    if (contact.phone.isNotBlank()) {
                        val success = sendSMS(context, contact.phone, smsMessage)
                        if (success) {
                            smsCount++
                            Log.d("SosViewModel", "SMS sent successfully to ${contact.name}: ${contact.phone}")
                        } else {
                            smsFailed++
                            Log.e("SosViewModel", "SMS FAILED to ${contact.name}: ${contact.phone}")
                        }
                    }
                }

                // 7. Send Push Notifications (if contacts have app)
                var pushCount = 0
                contacts.forEach { contact ->
                    if (contact.fcmToken != null) {
                        try {
                            sendPushNotification(contact.fcmToken, userName, locationString)
                            pushCount++
                        } catch (e: Exception) {
                            Log.e("SosViewModel", "Push failed for ${contact.name}", e)
                        }
                    }
                }

                // 8. Send Emails in background
                try {
                    sendEmailAlerts(contacts, userName, locationString, timestamp)
                } catch (e: Exception) {
                    Log.e("SosViewModel", "Email sending failed", e)
                }

                // 9. Save to Firestore
                saveSosToFirestore(contacts, locationString, userName)

                // 10. Show detailed success message
                val message = buildString {
                    append("✅ SOS ALERT SENT!\n\n")
                    append("📱 SMS: $smsCount sent")
                    if (smsFailed > 0) append(", $smsFailed failed")
                    append("\n")
                    if (pushCount > 0) append("🔔 Push Notifications: $pushCount sent\n")
                    append("📧 Emails: Queued for ${contacts.filter { it.email.isNotBlank() }.size} contacts\n")
                    append("\n✓ Emergency contacts have been notified")
                }

                _alertMessage.value = message
                _sosState.value = SosState.SENT_SUCCESS

                // 11. Auto-dismiss after 5 seconds
                delay(5000)
                resetState()

            } catch (e: Exception) {
                Log.e("SosViewModel", "Error sending SOS", e)
                _alertMessage.value = "❌ Error sending alert: ${e.message}"
                delay(3000)
                resetState()
            }
        }
    }

    /**
     * Send SMS - CRITICAL FUNCTION
     */
    private fun sendSMS(context: Context, phoneNumber: String, message: String): Boolean {
        return try {
            // Check permission
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.e("SosViewModel", "SMS permission not granted!")
                return false
            }

            Log.d("SosViewModel", "Attempting to send SMS to: $phoneNumber")

            // Get SmsManager
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            // Split message if too long
            val parts = smsManager.divideMessage(message)

            if (parts.size > 1) {
                Log.d("SosViewModel", "Sending multi-part SMS (${parts.size} parts)")
                smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    parts,
                    null,
                    null
                )
            } else {
                Log.d("SosViewModel", "Sending single SMS")
                smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
                )
            }

            Log.d("SosViewModel", "SMS sent successfully to $phoneNumber")
            true

        } catch (e: SecurityException) {
            Log.e("SosViewModel", "SecurityException: SMS permission denied", e)
            false
        } catch (e: Exception) {
            Log.e("SosViewModel", "Exception sending SMS to $phoneNumber", e)
            false
        }
    }

    /**
     * Send Push Notification via FCM
     */
    private suspend fun sendPushNotification(fcmToken: String, userName: String, location: String) {
        try {
            val data = hashMapOf(
                "token" to fcmToken,
                "title" to "🚨 EMERGENCY ALERT",
                "body" to "$userName needs immediate help!",
                "senderName" to userName,
                "location" to location
            )

            functions
                .getHttpsCallable("sendSOSNotification")
                .call(data)
                .await()

            Log.d("SosViewModel", "Push notification sent successfully")
        } catch (e: Exception) {
            Log.e("SosViewModel", "Push notification failed", e)
        }
    }

    /**
     * Send Email Alerts via Firebase Cloud Function
     */
    private suspend fun sendEmailAlerts(
        contacts: List<com.example.sheshield.models.Contact>,
        userName: String,
        location: String,
        timestamp: String
    ) {
        try {
            val contactsWithEmail = contacts.filter { it.email.isNotBlank() }

            if (contactsWithEmail.isEmpty()) {
                Log.d("SosViewModel", "No contacts with email addresses")
                return
            }

            val emailData = hashMapOf(
                "userName" to userName,
                "location" to location,
                "timestamp" to timestamp,
                "contacts" to contactsWithEmail.map {
                    mapOf("name" to it.name, "email" to it.email)
                }
            )

            functions
                .getHttpsCallable("sendSOSEmails")
                .call(emailData)
                .await()

            Log.d("SosViewModel", "Email function called successfully")
        } catch (e: Exception) {
            Log.e("SosViewModel", "Email sending failed", e)
        }
    }

    /**
     * Get current location - WITH FALLBACK
     */
    private suspend fun getLocation(context: Context): Location? {
        // Check both FINE and COARSE location permissions
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            Log.w("SosViewModel", "No location permission granted")
            return null
        }

        return try {
            // First try: Get last known location
            var location = fusedLocationClient?.lastLocation?.await()

            if (location != null) {
                Log.d("SosViewModel", "Location obtained from cache: ${location.latitude}, ${location.longitude}")
                return location
            }

            // Second try: Request fresh location update
            Log.d("SosViewModel", "Last location was null, requesting fresh location...")

            location = withTimeoutOrNull(5000) {
                suspendCancellableCoroutine { continuation ->
                    val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
                        priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                        interval = 0
                        fastestInterval = 0
                        numUpdates = 1
                    }

                    val callback = object : com.google.android.gms.location.LocationCallback() {
                        override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                            val freshLocation = result.lastLocation
                            if (freshLocation != null) {
                                Log.d("SosViewModel", "Fresh location obtained: ${freshLocation.latitude}, ${freshLocation.longitude}")
                                continuation.resume(freshLocation)
                            } else {
                                continuation.resume(null)
                            }
                            fusedLocationClient?.removeLocationUpdates(this)
                        }
                    }

                    fusedLocationClient?.requestLocationUpdates(
                        locationRequest,
                        callback,
                        android.os.Looper.getMainLooper()
                    )
                }
            }

            if (location == null) {
                Log.w("SosViewModel", "Could not get location (GPS may be off or no signal)")
            }

            location
        } catch (e: Exception) {
            Log.e("SosViewModel", "Location fetch failed: ${e.message}", e)
            null
        }
    }

    /**
     * Save SOS alert to Firestore - FIXED for Helper Dashboard
     */
    private suspend fun saveSosToFirestore(
        contacts: List<com.example.sheshield.models.Contact>,
        locationString: String,
        userName: String
    ) {
        val userId = auth.currentUser?.uid ?: return

        // 1. Prepare data for the HELPER DASHBOARD (Public)
        // IMPORTANT: status must be "active" (lowercase) to match the HelperService
        val globalAlertData = hashMapOf(
            "userName" to userName,
            "description" to "SOS Alert triggered! Location: $locationString",
            "riskLevel" to "high",
            "status" to "active",     // <--- LOWERCASE "active" is critical!
            "timestamp" to System.currentTimeMillis(),
            "senderId" to userId,
            "locationString" to locationString
        )

        // 2. Prepare data for User's History (Private)
        val personalSosData = hashMapOf(
            "status" to "ACTIVE",
            "userName" to userName,
            "location" to locationString,
            "createdAt" to FieldValue.serverTimestamp(),
            "contacts" to contacts.map {
                mapOf("name" to it.name, "phone" to it.phone, "email" to it.email)
            }
        )

        try {
            // A. Save to Global Alerts so Helpers can see it
            firestore.collection("alerts")
                .add(globalAlertData)

            // B. Save to Private History
            firestore.collection("users")
                .document(userId)
                .collection("sos_alerts")
                .add(personalSosData)

            Log.d("SosViewModel", "✅ Alert sent to Helpers and History")
        } catch (e: Exception) {
            Log.e("SosViewModel", "❌ Firestore save failed", e)
        }
    }

    /**
     * Reset to idle state
     */
    private fun resetState() {
        _sosState.value = SosState.IDLE
        _alertMessage.value = null
    }

    fun setErrorMessage(message: String) {
        _alertMessage.value = message
        viewModelScope.launch {
            delay(3000)
            _alertMessage.value = null
        }
    }

    fun clearAlertMessage() {
        _alertMessage.value = null
    }
}