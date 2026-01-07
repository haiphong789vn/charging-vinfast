package com.example.chargingvinfast

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    private const val STATUS_CHANNEL_ID = "charging_status"
    private const val ALARM_CHANNEL_ID = "charging_alarm"
    const val FOREGROUND_CHANNEL_ID = "charging_foreground"
    const val STATUS_NOTIFICATION_ID = 101
    private const val ALARM_NOTIFICATION_ID = 201
    const val FOREGROUND_NOTIFICATION_ID = 301

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val statusChannel = NotificationChannel(
            STATUS_CHANNEL_ID,
            "Thông báo sạc",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Nhắc trạng thái sạc mỗi 1.5 giờ"
        }

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val alarmAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val alarmChannel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Chuông nhắc sạc",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Âm báo sau 5 giờ nếu chưa dừng"
            setSound(alarmSound, alarmAttributes)
            enableVibration(true)
        }

        val foregroundChannel = NotificationChannel(
            FOREGROUND_CHANNEL_ID,
            "Dịch vụ chạy nền",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Dịch vụ theo dõi sạc chạy ở nền"
        }

        manager.createNotificationChannel(statusChannel)
        manager.createNotificationChannel(alarmChannel)
        manager.createNotificationChannel(foregroundChannel)
    }

    fun showStatusNotification(context: Context) {
        val pendingIntent = pendingIntent(context)
        val builder = NotificationCompat.Builder(context, STATUS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_charging)
            .setContentTitle("Theo dõi sạc pin")
            .setContentText("Ứng dụng sẽ nhắc bạn mỗi 1.5 giờ")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        NotificationManagerCompat.from(context).notify(STATUS_NOTIFICATION_ID, builder.build())
    }

    fun showAlarmNotification(context: Context) {
        val pendingIntent = pendingIntent(context)
        val builder = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_charging)
            .setContentTitle("Hết 5 giờ sạc")
            .setContentText("Đã 5 giờ, hãy kiểm tra và dừng sạc nếu đã đủ")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setContentIntent(pendingIntent)

        NotificationManagerCompat.from(context).notify(ALARM_NOTIFICATION_ID, builder.build())
    }

    fun createForegroundNotification(context: Context): Notification {
        ensureChannels(context)
        val pendingIntent = pendingIntent(context)
        return NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_charging)
            .setContentTitle("Đang theo dõi sạc")
            .setContentText("Ứng dụng đang chạy ở nền để theo dõi sạc pin")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun cancelAllNotifications(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(STATUS_NOTIFICATION_ID)
        manager.cancel(ALARM_NOTIFICATION_ID)
        manager.cancel(FOREGROUND_NOTIFICATION_ID)
    }

    fun playAlarm(context: Context) {
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone = RingtoneManager.getRingtone(context, alarmUri) ?: return
        if (!ringtone.isPlaying) {
            ringtone.play()
        }
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
