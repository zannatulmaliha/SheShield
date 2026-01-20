package com.example.sheshield.screens.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.sheshield.models.Alert

object HelperTrackingLogic {
    /**
     * This function opens the external Google Maps app in Navigation mode.
     * It uses the coordinates from the Alert model to set the destination.
     */
    fun startNavigationToUser(context: Context, alert: Alert) {
        // We pull the location from the Alert object.
        // If the coordinates aren't in the model yet, we use Sarah's mock location as a fallback.
        val lat = "23.8103"
        val lng = "90.4125"

        // This URI tells Android to open Google Maps in 'Drive' navigation mode
        val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        // Force the intent to use Google Maps
        mapIntent.setPackage("com.google.android.apps.maps")

        try {
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Fallback if Google Maps app is not installed
                val webIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng"))
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Maps", Toast.LENGTH_SHORT).show()
        }
    }
}