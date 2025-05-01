package com.example.daysinlove

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Calendar
import com.example.daysinlove.Funcer
import com.example.daysinlove.R

class ForegroundService : Service() {
    private val channelId = "DaysInLoveServiceChannel"
    private lateinit var notificationManager: NotificationManager
    private var daysCount = 0
    private var alarmManager: AlarmManager? = null
    private var dailyUpdatePendingIntent: PendingIntent? = null
        private set // Запрещаем внешнее изменение

    override fun onBind(intent: Intent?): IBinder? = null



    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        createNotificationChannel()
        setupDailyUpdate()
        Log.d("ForegroundService", "Service onCreate()")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ForegroundService", "Service onStartCommand()")
        when (intent?.action) {
            ACTION_START -> startService()
            ACTION_STOP -> stopService()
            ACTION_UPDATE -> updateDaysCount()
            else -> startService() // Всегда запускаем, если action не указан
        }
        return START_STICKY
    }

    private fun startService() {
        updateDaysCount()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun stopService() {
        stopForeground(true)
        stopSelf()
        cancelDailyUpdate()
    }

    private fun isRunning(): Boolean {
        return daysCount > 0
    }

    private fun updateDaysCount() {
        daysCount = Funcer.getDaysTogether(this)
        updateNotification()
    }

    private fun setupDailyUpdate() {
        // Отменяем предыдущий PendingIntent если он существует
        cancelDailyUpdate()

        val intent = Intent(this, ForegroundService::class.java).apply {
            action = ACTION_UPDATE
        }

        // Создаем новый PendingIntent
        val pendingIntent = PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Устанавливаем ежедневное обновление в 00:00
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        alarmManager?.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        // Сохраняем ссылку
        dailyUpdatePendingIntent = pendingIntent
    }

    private fun cancelDailyUpdate() {
        // Создаем локальную копию для thread-safety
        val pendingIntent = dailyUpdatePendingIntent
        if (pendingIntent != null) {
            alarmManager?.cancel(pendingIntent)
            dailyUpdatePendingIntent = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ДниВЛюбви",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Показывает количество дней вместе"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("ДниВЛюбви")
            .setContentText("Дней вместе: $daysCount")
            .setSmallIcon(R.drawable.ic_heart)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .addAction(
                R.drawable.ic_heart,
                "Остановить",
                PendingIntent.getService(
                    this,
                    0,
                    Intent(this, ForegroundService::class.java).apply {
                        action = ACTION_STOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelDailyUpdate()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, ForegroundService::class.java).apply {
            action = ACTION_START
        }

        val restartServicePendingIntent = PendingIntent.getService(
            this,
            1,
            restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )

        super.onTaskRemoved(rootIntent)
    }

    companion object {
        const val NOTIFICATION_ID = 123
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_UPDATE = "ACTION_UPDATE"

        fun startService(context: Context) {
            val intent = Intent(context, ForegroundService::class.java).apply {
                action = ACTION_START
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }
}