package com.example.chargingvinfast

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class ChargingForegroundService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var startedAtMillis: Long = 0L
    
    private val updateNotificationRunnable = object : Runnable {
        override fun run() {
            updateNotificationWithElapsedTime()
            // Update every minute
            handler.postDelayed(this, 60_000L)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // Get start time from shared preferences
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        startedAtMillis = prefs.getLong(KEY_STARTED_AT, System.currentTimeMillis())
        
        val elapsedText = NotificationHelper.formatElapsedTime(startedAtMillis)
        val notification = NotificationHelper.createForegroundNotification(this, elapsedText)
        startForeground(NotificationHelper.FOREGROUND_NOTIFICATION_ID, notification)
        
        // Start periodic updates
        handler.postDelayed(updateNotificationRunnable, 60_000L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Refresh start time in case it was updated
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        startedAtMillis = prefs.getLong(KEY_STARTED_AT, System.currentTimeMillis())
        updateNotificationWithElapsedTime()
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateNotificationRunnable)
        super.onDestroy()
    }
    
    private fun updateNotificationWithElapsedTime() {
        val elapsedText = NotificationHelper.formatElapsedTime(startedAtMillis)
        NotificationHelper.updateForegroundNotification(this, elapsedText)
    }

    companion object {
        private const val PREFS_NAME = "charging_prefs"
        private const val KEY_STARTED_AT = "started_at"
    }
}
