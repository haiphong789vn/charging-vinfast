package com.example.chargingvinfast

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ChargingForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val notification = NotificationHelper.createForegroundNotification(this)
        startForeground(NotificationHelper.FOREGROUND_NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
