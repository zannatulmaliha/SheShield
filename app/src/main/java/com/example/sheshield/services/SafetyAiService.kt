package com.example.sheshield.services

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SafetyAiService {

   
    private val aiService = Firebase.ai(backend = GenerativeBackend.googleAI())

    /**
     * ✅ MODEL CONFIGURATION
     * Using 'gemini-1.5-flash' is currently the most stable choice for the Free Tier.
     * Note: If 'gemini-3-flash-preview' gives 404 errors, switch back to 'gemini-1.5-flash'.
     */
    private val model = aiService.generativeModel(
        modelName = "gemini-3-flash-preview",
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1000
        },
        systemInstruction = content {
            text("You are SheShield's AI Safety Companion. Provide calm, practical safety advice. " +
                    "CRITICAL: If the user is in immediate danger, PRIORITIZE telling them to call 999 " +
                    "or use the app's SOS button. Keep responses concise and actionable.")
        }
    )

    // Persistent chat session to remember conversation history
    private val chat = model.startChat()

    /**
     * OPTION 1: Standard Message (Simple)
     * Use this if you just want a single block of text.
     */
    suspend fun sendMessage(userMessage: String): String {
        return try {
            val response = chat.sendMessage(userMessage)
            // Log the raw response to debug "Empty" messages
            Log.d("SafetyAiService", "Raw Response: ${response.text}")

            response.text ?: "I'm having trouble thinking right now. Please try again."
        } catch (e: Exception) {
            Log.e("SafetyAiService", "Error sending message", e)
            return "Safety Check: AI is temporarily offline (${e.localizedMessage}). If this is an emergency, please use the SOS button."
        }
    }

    /**
     * OPTION 2: Streaming Message (Recommended for "Offline" Fix)
     * Use this to show the user text as it arrives. This fixes the "Console vs SDK Gap"
     * because it keeps the connection alive even if it flickers.
     */
    fun sendMessageStream(userMessage: String): Flow<String> {
        return chat.sendMessageStream(userMessage)
            .map { chunk ->
                chunk.text ?: ""
            }
    }
}
