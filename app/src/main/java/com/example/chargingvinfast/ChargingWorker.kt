package com.example.chargingvinfast

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ChargingWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        NotificationHelper.ensureChannels(applicationContext)
        NotificationHelper.showStatusNotification(applicationContext)
        return Result.success()
    }
}
