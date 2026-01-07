package com.example.chargingvinfast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            // Check if charging was running before reboot
            val prefs = context.getSharedPreferences("charging_prefs", Context.MODE_PRIVATE)
            val wasRunning = prefs.getBoolean("is_running", false)
            
            if (wasRunning) {
                // Restart the foreground service
                val serviceIntent = Intent(context, ChargingForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                
                // Re-schedule the workers
                val scheduler = ChargingScheduler(context)
                scheduler.startCharging()
            }
        }
    }
}
