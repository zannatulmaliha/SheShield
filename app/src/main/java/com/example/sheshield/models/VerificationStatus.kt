package com.example.sheshield.models

data class VerificationStatus(
    val livePhoto: Boolean = false,
    val nid: Boolean = false,
    val phone: Boolean = false,
    val bkash: Boolean = false
) {
    fun isFullyVerified(): Boolean {
        return livePhoto && nid && phone && bkash
    }
}
