package com.example.sheshield.models

data class Country(
    val code: String,
    val name: String,
    val flag: String,
    val dialCode: String
)

// Common countries list
val countries = listOf(
    Country("BD", "Bangladesh", "🇧🇩", "+880"),
    Country("US", "United States", "🇺🇸", "+1"),
    Country("GB", "United Kingdom", "🇬🇧", "+44"),
    Country("IN", "India", "🇮🇳", "+91"),
    Country("PK", "Pakistan", "🇵🇰", "+92"),
    Country("CA", "Canada", "🇨🇦", "+1"),
    Country("AU", "Australia", "🇦🇺", "+61"),
    Country("SA", "Saudi Arabia", "🇸🇦", "+966"),
    Country("AE", "UAE", "🇦🇪", "+971"),
    Country("MY", "Malaysia", "🇲🇾", "+60"),
    Country("SG", "Singapore", "🇸🇬", "+65"),
    Country("JP", "Japan", "🇯🇵", "+81"),
    Country("KR", "South Korea", "🇰🇷", "+82"),
    Country("CN", "China", "🇨🇳", "+86"),
    Country("RU", "Russia", "🇷🇺", "+7"),
    Country("DE", "Germany", "🇩🇪", "+49"),
    Country("FR", "France", "🇫🇷", "+33"),
    Country("IT", "Italy", "🇮🇹", "+39"),
    Country("ES", "Spain", "🇪🇸", "+34"),
    Country("BR", "Brazil", "🇧🇷", "+55"),
)

// Helper functions
fun formatPhoneInput(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    return when {
        digits.length <= 4 -> digits
        digits.length <= 7 -> "${digits.substring(0, 4)} ${digits.substring(4)}"
        else -> "${digits.substring(0, 4)} ${digits.substring(4, 7)} ${digits.substring(7).take(4)}"
    }
}