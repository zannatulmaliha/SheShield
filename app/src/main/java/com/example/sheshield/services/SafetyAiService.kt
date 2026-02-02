package com.example.sheshield.services

import com.google.firebase.Firebase
import com.google.firebase.vertexai.vertexAI
import com.google.firebase.vertexai.type.content
import com.google.firebase.vertexai.type.generationConfig

class SafetyAiService {

    // 1. Initialize Vertex AI (Official 2026 stable SDK)

    private val vertexAI = Firebase.vertexAI(location = "global")

    // 2. Configure the model
    // Using "gemini-3-flash" for high-speed safety responses
    private val model = vertexAI.generativeModel(
        modelName = "gemini-3-flash",
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 200
        },
        systemInstruction = content {
            text("You are SheShield's AI Safety Companion. Provide calm, practical safety advice. " +
                    "If the user is in immediate danger, PRIORITIZE telling them to call 999 or use the app's SOS button. " +
                    "Keep responses concise and actionable.")
        }
    )

    // 3. Start a chat session
    private val chat = model.startChat()

    // 4. Function to send message
    suspend fun sendMessage(userMessage: String): String {
        return try {
            val response = chat.sendMessage(userMessage)
            response.text ?: "I'm having trouble thinking right now. Please try again."
        } catch (e: Exception) {
            e.printStackTrace()
            // This now captures the error properly without the 'MissingField' crash
            "Safety Check: I'm currently unavailable. If this is an emergency, please use the SOS button or call emergency services immediately."
        }
    }
}