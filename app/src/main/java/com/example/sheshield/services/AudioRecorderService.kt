package com.example.sheshield.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File

class AudioRecorderService : Service() {

    companion object {
        private const val TAG = "npm"
        private const val CHANNEL_ID = "audio_recording_channel"
        private const val NOTIF_ID = 201
        const val ACTION_STOP = "STOP_AUDIO_RECORDING"
    }

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🟢 [npm] Audio SERVICE CREATED")

        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Preparing audio recorder..."))
        Log.d(TAG, "🔔 [npm] Audio Notification channel created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == ACTION_STOP) {
            Log.d(TAG, "⏹️ [npm] STOP pressed in notification")
            stopSelf()
            return START_NOT_STICKY
        }

        Log.d(TAG, "▶️ [npm] Audio Service started")
        Thread { startRecording() }.start()

        return START_NOT_STICKY
    }

    private fun startRecording() {
        try {
//            val dir = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "BackgroundAudio")
//            if (!dir.exists()) dir.mkdirs()

            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "BackgroundAudios")
            if (!dir.exists()) dir.mkdirs()

            outputFile = File(dir, "audio_${System.currentTimeMillis()}.mp3")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile!!.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            updateNotification("🔴 Recording audio in progress")
            Log.d(TAG, "🎤 [npm] Recording audio: ${outputFile!!.absolutePath}")

        } catch (e: Exception) {
            Log.e(TAG, "❌ [npm] startRecording failed: ${e.message}", e)
            cleanup()
            stopSelf()
        }
    }

    private fun cleanup() {
        try {
            mediaRecorder?.apply {
                if (isRecording) stop()
                release()
            }
            mediaRecorder = null

            isRecording = false
            Log.d(TAG, "✅ [npm] Audio cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "❌ [npm] Audio cleanup error: ${e.message}")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "🔴 [npm] Audio onDestroy — stopping recording")
        cleanup()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background Audio Recording",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val stopIntent = Intent(this, AudioRecorderService::class.java).apply {
            action = ACTION_STOP
        }

        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Background Audio Recorder")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "STOP RECORDING",
                stopPendingIntent
            )
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, buildNotification(text))
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
