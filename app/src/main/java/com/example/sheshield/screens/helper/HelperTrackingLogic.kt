package com.example.sheshield.screens.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.sheshield.models.Alert

object HelperTrackingLogic {
    /**
     * This function opens the external Google Maps app in Navigation mode.
     * It uses the real coordinates from the Alert model to set the destination.
     */
    fun startNavigationToUser(context: Context, alert: Alert) {
        // 1. Pull the REAL location from the Alert object
        val lat = alert.location.latitude
        val lng = alert.location.longitude

        // 2. Safety Check: If coordinates are 0.0, GPS likely failed to capture location
        if (lat == 0.0 && lng == 0.0) {
            Toast.makeText(context, "Location data missing for this alert", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Create the Google Maps Intent
        // "google.navigation:q=lat,lng" tells Maps to navigate to these coordinates
        // "&mode=d" forces driving mode (you can change to 'w' for walking if preferred)
        val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        // Force the intent to use Google Maps app specifically
        mapIntent.setPackage("com.google.android.apps.maps")

        try {
            // Check if Google Maps is installed
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // 4. Fallback: If Maps app is missing, open in Browser
                val webIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng"))
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Maps", Toast.LENGTH_SHORT).show()
        }
    }
}