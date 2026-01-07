package com.example.chargingvinfast

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class ChargingScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun startCharging() {
        NotificationHelper.ensureChannels(context)
        NotificationHelper.showStatusNotification(context)

        val periodicRequest = PeriodicWorkRequestBuilder<ChargingWorker>(90, TimeUnit.MINUTES)
            .build()

        val alarmRequest = OneTimeWorkRequestBuilder<BeepWorker>()
            .setInitialDelay(5, TimeUnit.HOURS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            CHARGING_STATUS_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest,
        )

        workManager.enqueueUniqueWork(
            CHARGING_ALARM_WORK,
            ExistingWorkPolicy.REPLACE,
            alarmRequest,
        )
    }

    fun stopCharging() {
        workManager.cancelUniqueWork(CHARGING_STATUS_WORK)
        workManager.cancelUniqueWork(CHARGING_ALARM_WORK)
    }

    companion object {
        const val CHARGING_STATUS_WORK = "charging_status_periodic"
        const val CHARGING_ALARM_WORK = "charging_alarm_once"
    }
}
