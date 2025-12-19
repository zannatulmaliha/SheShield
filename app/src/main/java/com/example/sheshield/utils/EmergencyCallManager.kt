package com.example.sheshield.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat

object EmergencyCallManager {

    private const val TAG = "EmergencyCallManager"

    /**
     * Makes DIRECT emergency call without showing dialer
     * @param context The application context
     * @param phoneNumber The phone number to call (with country code, e.g., "+8801982624881")
     * @return Boolean indicating if the call was initiated successfully
     */
    fun makeEmergencyCall(context: Context, phoneNumber: String): Boolean {
        return try {
            Log.d(TAG, "Attempting emergency call to: $phoneNumber")

            // Ensure proper formatting
            val formattedNumber = formatPhoneNumber(phoneNumber)

            // Create CALL intent (not DIAL) - This makes direct call
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$formattedNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            // Verify a phone app can handle this
            val canMakeCall = callIntent.resolveActivity(context.packageManager) != null

            if (canMakeCall) {
                Log.d(TAG, "Starting DIRECT emergency call to: $formattedNumber")
                ContextCompat.startActivity(context, callIntent, null)
                true
            } else {
                Log.e(TAG, "No phone app found to handle call")
                // Fallback: Open dialer with number pre-filled
                openDialerFallback(context, formattedNumber)
                false
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "CALL_PHONE permission denied: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Emergency call failed: ${e.message}")
            false
        }
    }

    /**
     * Fallback method - opens dialer with number pre-filled
     */
    private fun openDialerFallback(context: Context, phoneNumber: String): Boolean {
        return try {
            Log.d(TAG, "Falling back to dialer for: $phoneNumber")
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (dialIntent.resolveActivity(context.packageManager) != null) {
                ContextCompat.startActivity(context, dialIntent, null)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Dialer fallback also failed: ${e.message}")
            false
        }
    }

    /**
     * Formats phone number to ensure proper syntax
     */
    private fun formatPhoneNumber(phoneNumber: String): String {
        var formatted = phoneNumber.trim()

        // Remove any spaces or dashes
        formatted = formatted.replace(" ", "")
        formatted = formatted.replace("-", "")

        // Ensure it starts with +
        if (!formatted.startsWith("+")) {
            formatted = "+$formatted"
        }

        return formatted
    }
}