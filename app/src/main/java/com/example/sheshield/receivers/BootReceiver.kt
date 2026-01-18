package com.example.sheshield.receivers


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sheshield.services.VoiceCommandService

/**
 * Starts voice command service on device boot
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted, starting voice service")

            // Check if voice protection is enabled in preferences
            val prefs = context.getSharedPreferences("sheshield_prefs", Context.MODE_PRIVATE)
            val isVoiceEnabled = prefs.getBoolean("voice_protection_enabled", false)

            if (isVoiceEnabled) {
                VoiceCommandService.start(context)
            }
        }
    }
}