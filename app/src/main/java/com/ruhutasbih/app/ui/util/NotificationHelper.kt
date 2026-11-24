package com.ruhutasbih.app.ui.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ruhutasbih.app.MainActivity
import com.ruhutasbih.app.R
import com.ruhutasbih.app.data.worker.DailyReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationHelper {

    private const val CHANNEL_ID = "ruhutasbih_dhikr_reminders"
    private const val WORK_NAME = "ruhutasbih_daily_reminder_work"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Dhikr Reminders"
            val descriptionText = "Daily reminders for Dhikr and Tasbih count"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendTestNotification(context: Context) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📿 Ruhu Tasbih - Daily Dhikr Reminder")
            .setContentText("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ | Time for your daily Dhikr & Remembrance!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ سُبْحَانَ اللَّهِ الْعَظِيمِ\n\nMay Allah bless your day! Open Ruhu Tasbih to complete your daily Dhikr targets.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1002, notification)
    }

    fun scheduleMultipleReminders(context: Context, times: Set<String>) {
        cancelAllReminders(context)
        times.forEachIndexed { idx, timeStr ->
            scheduleSingleReminder(context, timeStr, idx)
        }
    }

    fun scheduleSingleReminder(context: Context, timeStr: String, reqId: Int) {
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        val initialDelay = target.timeInMillis - now.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "${WORK_NAME}_$reqId",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }

    fun cancelAllReminders(context: Context) {
        for (i in 0..10) {
            WorkManager.getInstance(context).cancelUniqueWork("${WORK_NAME}_$i")
        }
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}