package com.example.sheshield.services


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sheshield.MainActivity
import com.example.sheshield.R
import com.example.sheshield.SOS.SosViewModel
import com.example.sheshield.repository.ContactsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VoiceCommandService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionIntent: Intent? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var sosViewModel: SosViewModel
    private var isListening = false

    companion object {
        private const val CHANNEL_ID = "voice_command_channel"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "VoiceCommandService"

        // Voice trigger phrases
        private val SOS_TRIGGERS = listOf(
            "help me",
            "emergency",
            "sos",
            "i need help",
            "call for help",
            "danger",
            "save me",
            "rescue me"
        )

        fun start(context: Context) {
            val intent = Intent(context, VoiceCommandService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, VoiceCommandService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VoiceCommandService created")

        sosViewModel = SosViewModel(ContactsRepository())
        sosViewModel.initLocationClient(this)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        initializeSpeechRecognizer()
        startListening()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Command Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Listening for emergency voice commands"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SheShield Voice Protection Active")
            .setContentText("🎤 Listening for emergency commands...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun initializeSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.e(TAG, "Speech recognition not available on this device")
            stopSelf()
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Ready for speech")
                isListening = true
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Speech detected")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changed
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d(TAG, "Speech ended")
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                    else -> "Unknown error"
                }
                Log.e(TAG, "Recognition error: $errorMessage")

                isListening = false

                // Restart listening after error (except for permission errors)
                if (error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    serviceScope.launch {
                        delay(1000)
                        startListening()
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.let { processVoiceCommand(it) }

                isListening = false

                // Restart listening
                serviceScope.launch {
                    delay(500)
                    startListening()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.let {
                    Log.d(TAG, "Partial results: $it")
                    // Check for emergency phrases in partial results for faster response
                    processVoiceCommand(it)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        if (isListening) {
            Log.d(TAG, "Already listening, skipping...")
            return
        }

        try {
            speechRecognizer?.startListening(recognitionIntent)
            Log.d(TAG, "Started listening for voice commands")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)

            // Retry after delay
            serviceScope.launch {
                delay(2000)
                startListening()
            }
        }
    }

    private fun processVoiceCommand(matches: List<String>) {
        Log.d(TAG, "Processing voice matches: $matches")

        for (match in matches) {
            val lowerMatch = match.lowercase().trim()

            // Check if any SOS trigger phrase is detected
            val isSosTrigger = SOS_TRIGGERS.any { trigger ->
                lowerMatch.contains(trigger)
            }

            if (isSosTrigger) {
                Log.d(TAG, "🚨 SOS TRIGGER DETECTED: $match")
                triggerSosAlert()
                return // Stop processing once SOS is triggered
            }
        }
    }

    private fun triggerSosAlert() {
        // Update notification to show SOS is being sent
        val notificationManager = getSystemService(NotificationManager::class.java)
        val alertNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🚨 SENDING SOS ALERT")
            .setContentText("Emergency alert is being sent to your contacts...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        notificationManager.notify(NOTIFICATION_ID, alertNotification)

        // Send SOS alert
        serviceScope.launch {
            try {
                Log.d(TAG, "Triggering SOS alert from voice command")
                sosViewModel.sendSosAlert(this@VoiceCommandService)

                // Show success notification
                delay(2000)
                val successNotification = NotificationCompat.Builder(this@VoiceCommandService, CHANNEL_ID)
                    .setContentTitle("✅ SOS Alert Sent")
                    .setContentText("Emergency contacts have been notified")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(NOTIFICATION_ID + 1, successNotification)

                // Restore listening notification
                delay(3000)
                notificationManager.notify(NOTIFICATION_ID, createNotification())

            } catch (e: Exception) {
                Log.e(TAG, "Error sending SOS alert", e)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        return START_STICKY // Restart service if killed
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")

        speechRecognizer?.destroy()
        speechRecognizer = null
        serviceScope.cancel()
    }
}