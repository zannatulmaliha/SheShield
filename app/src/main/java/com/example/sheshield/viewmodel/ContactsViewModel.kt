package com.example.sheshield.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheshield.models.Contact
import com.example.sheshield.repository.ContactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {
    // Create instance of Repository
    private val repository = ContactsRepository()

    // State 1: List of contacts (UI observes this)
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    // State 2: Loading indicator
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State 3: Error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Load contacts when ViewModel is created
    init {
        loadContacts()
    }

    /**
     * Load contacts from Firebase
     */
    fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val contactsList = repository.getContacts()
                _contacts.value = contactsList
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load contacts: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Add a new contact
     */
    fun addContact(name: String, phone: String) {
        // Basic validation
        if (name.isBlank() || phone.isBlank()) {
            _errorMessage.value = "Name and phone are required"
            return
        }
        if (phone.length !=14) { // Adjust based on country
            _errorMessage.value = "Please enter a valid phone number"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Create Contact object
                val newContact = Contact(
                    name = name.trim(),
                    phone = phone.trim()
                    // createdAt will be set automatically by Repository
                )

                // Call Repository to save to Firebase
                val success = repository.addContact(newContact)

                if (success) {
                    // Refresh the list
                    loadContacts()
                } else {
                    _errorMessage.value = "Failed to add contact"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a contact
     */
    // In ContactsViewModel.kt, change:
    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // NEW: Pass contact.id (Firestore document ID) instead of finding index
                val success = repository.deleteContact(contact.id)

                if (success) {
                    loadContacts() // Refresh the list
                } else {
                    _errorMessage.value = "Failed to delete contact"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    // In your ContactsViewModel class, add these properties and methods:

    private val _pendingCallNumber = MutableStateFlow<String?>(null)
    val pendingCallNumber: StateFlow<String?> = _pendingCallNumber.asStateFlow()

    fun setPendingCall(phoneNumber: String) {
        _pendingCallNumber.value = phoneNumber
    }

    fun clearPendingCall() {
        _pendingCallNumber.value = null
    }
}