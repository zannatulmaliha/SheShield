package com.example.sheshield.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheshield.repository.ContactsRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrackRouteViewModel : ViewModel() {

    // We use the existing repository your teammate created
    private val contactsRepository = ContactsRepository()

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

    fun notifyContacts(context: Context, location: LatLng?) {
        if (location == null) {
            Toast.makeText(context, "Waiting for location...", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _loadingState.value = true
            try {
                // 1. Fetch contacts using the existing repository
                val contacts = contactsRepository.getContacts()

                if (contacts.isEmpty()) {
                    Toast.makeText(context, "No trusted contacts found!", Toast.LENGTH_LONG).show()
                    _loadingState.value = false
                    return@launch
                }

                // 2. Create the Google Maps link
                val mapLink = "http://maps.google.com/?q=${location.latitude},${location.longitude}"
                val message = "I'm sharing my live route securely via SheShield. Track me here: $mapLink"

                // 3. Send SMS to each contact
                var sentCount = 0
                contacts.forEach { contact ->
                    if (contact.phone.isNotBlank()) {
                        val success = sendSMS(context, contact.phone, message)
                        if (success) sentCount++
                    }
                }

                // 4. Show result
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
