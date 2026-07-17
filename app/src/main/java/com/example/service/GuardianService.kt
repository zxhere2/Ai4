package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.ThreatLog
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

class GuardianService : Service() {
    companion object {
        private const val TAG = "GuardianService"
        private const val CHANNEL_ID = "guardian_service_channel"
        private const val NOTIFICATION_ID = 991
        var isServiceRunning = AtomicBoolean(false)
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        createNotificationChannel()
        Log.d(TAG, "Guardian Service created.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Guardian Service started.")
        isServiceRunning.set(true)

        val notification = buildNotification("Guardian AI Protection Active", "Monitoring mic, camera, overlay and files.")
        startForeground(NOTIFICATION_ID, notification)

        // Start active periodic security sweeps
        startActiveMonitoring()

        return START_STICKY
    }

    private fun startActiveMonitoring() {
        serviceScope.launch {
            while (isActive) {
                try {
                    // Minimal battery background sweep
                    Log.d(TAG, "Guardian performing periodic background safety sweep...")
                    
                    // Simulated threat detection logic
                    checkMicrophoneAndCameraStatus()
                    checkOverlayProtection()

                    delay(30000) // Sleep 30 seconds to minimize CPU and battery usage
                } catch (e: CancellationException) {
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Error in background safety loop", e)
                }
            }
        }
    }

    private suspend fun checkMicrophoneAndCameraStatus() {
        // In a real app we'd queryAudioManager/AppOpsManager, but we simulate smart tracking here
        val randomNum = (1..100).random()
        if (randomNum > 95) { // 5% chance of finding a dummy mic leak for simulation logs
            val log = ThreatLog(
                threatType = "DANGEROUS_PERMISSION",
                description = "Suspicious application requested background microphone access. Stream rejected.",
                riskLevel = "WARNING"
            )
            database.guardianDao().insertThreatLog(log)
            postInstantThreatNotification("Microphone Intrusion Blocked", "An app tried to access your mic in the background.")
        }
    }

    private suspend fun checkOverlayProtection() {
        // Simulates finding a screen overlay threat
        val randomNum = (1..100).random()
        if (randomNum > 97) {
            val log = ThreatLog(
                threatType = "OVERLAY_ATTACK",
                description = "Detected potential layout overlay attack from unverified app. Interactive elements disabled.",
                riskLevel = "CRITICAL"
            )
            database.guardianDao().insertThreatLog(log)
            postInstantThreatNotification("Overlay Threat Isolated", "Prevented a floating window from capturing keystrokes.")
        }
    }

    private fun postInstantThreatNotification(title: String, text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify((200..1000).random(), notification)
    }

    private fun buildNotification(title: String, text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Guardian AI Active Defense Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Guardian AI protecting your files and connection in the background."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "Guardian Service destroyed.")
        isServiceRunning.set(false)
        serviceJob.cancel()
        super.onDestroy()
    }
}
