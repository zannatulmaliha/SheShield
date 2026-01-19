package com.example.sheshield.screens.helper


import com.example.sheshield.models.Alert
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object HelperAlertService {
    private val db = FirebaseFirestore.getInstance()

    /**
     * This function connects to Firebase and listens for any NEW alerts.
     * When a user clicks SOS, this function automatically detects it.
     */
    fun getRealtimeAlerts(): Flow<List<Alert>> = callbackFlow {
        // We look into the "alerts" collection, sorted by the newest first
        val query = db.collection("alerts")
            .whereEqualTo("status", "active") // Only show alerts that haven't been resolved
            //.orderBy("timestamp", Query.Direction.DESCENDING)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // If there's an error (like no internet), stop the listener
                close(error)
                return@addSnapshotListener
            }

            // Convert the Firebase data into our List of Alert objects
            val alertList = snapshot?.toObjects(Alert::class.java) ?: emptyList()

            // Send the list to the UI
            trySend(alertList)
        }

        // This ensures the listener stops when the user leaves the dashboard
        // to save battery and data.
        awaitClose { subscription.remove() }
    }
}