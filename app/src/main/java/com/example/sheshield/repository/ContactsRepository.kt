package com.example.sheshield.repository

import android.util.Log
import com.example.sheshield.models.Contact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp

class ContactsRepository {
    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = Firebase.firestore

    // Get current user ID (returns null if not logged in)
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Get all trusted contacts for current user
     */
    suspend fun getContacts(): List<Contact> {
        val userId = getCurrentUserId() ?: return emptyList()

        return try {
            Log.d("CONTACTS_REPO", "📂 Fetching contacts from: users/$userId/trustedContacts")

            // NEW PATH: users/{userId}/trustedContacts
            val querySnapshot = db.collection("users")
                .document(userId)
                .collection("trustedContacts")
                .get()
                .await()

            val contacts = querySnapshot.documents.map { document ->
                Contact(
                    name = document.getString("name") ?: "",
                    phone = document.getString("phone") ?: "",
                    createdAt = convertToTimestamp(document.get("createdAt")),
                    id = document.id  // Firestore auto-generated document ID
                )
            }

            Log.d("CONTACTS_REPO", "✅ Found ${contacts.size} contacts")
            contacts
        } catch (e: Exception) {
            Log.e("CONTACTS_REPO", "❌ Error fetching contacts", e)
            emptyList()
        }
    }

    // Helper function to convert any Firebase timestamp to Timestamp
    private fun convertToTimestamp(value: Any?): Timestamp {
        return when (value) {
            is Timestamp -> value
            is Map<*, *> -> {
                val seconds = (value["seconds"] as? Long) ?: 0L
                val nanoseconds = (value["nanoseconds"] as? Long)?.toInt() ?: 0
                Timestamp(seconds, nanoseconds)
            }
            else -> Timestamp.now()
        }
    }

    /**
     * Add a new contact
     */
    suspend fun addContact(contact: Contact): Boolean {
        val userId = getCurrentUserId() ?: return false

        return try {
            Log.d("CONTACTS_REPO", "➕ Adding contact for user: $userId")

            // NEW PATH: users/{userId}/trustedContacts
            val contactData = hashMapOf(
                "name" to contact.name,
                "phone" to contact.phone,
                "createdAt" to FieldValue.serverTimestamp(),
                "userId" to userId  // Store user ID for reference
            )

            // Add new document to subcollection
            db.collection("users")
                .document(userId)
                .collection("trustedContacts")
                .add(contactData)
                .await()

            Log.d("CONTACTS_REPO", "✅ Contact added successfully")
            true
        } catch (e: Exception) {
            Log.e("CONTACTS_REPO", "❌ Error adding contact: ${e.message}")
            false
        }
    }

    /**
     * Delete a contact by document ID
     */
    suspend fun deleteContact(contactId: String): Boolean {
        val userId = getCurrentUserId() ?: return false

        return try {
            Log.d("CONTACTS_REPO", "🗑️ Deleting contact: $contactId for user: $userId")

            // NEW PATH: users/{userId}/trustedContacts/{contactId}
            db.collection("users")
                .document(userId)
                .collection("trustedContacts")
                .document(contactId)
                .delete()
                .await()

            Log.d("CONTACTS_REPO", "✅ Contact deleted successfully")
            true
        } catch (e: Exception) {
            Log.e("CONTACTS_REPO", "❌ Error deleting contact: ${e.message}")
            false
        }
    }
}
