package com.example.daysinlove

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ForegroundService : Service() {
    private val channelId = "TimerForegroundServiceChannel"
    private var secondsCount = 0
    private var isRunning = false
    private lateinit var notificationManager: NotificationManager
    private var timerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_STOP -> stopTimer()
            else -> if (!isRunning) startTimer()
        }

        return START_STICKY
    }

    private fun startTimer() {
        if (isRunning) return

        isRunning = true
        secondsCount = 0

        // Запускаем корутину для отсчета времени
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (isRunning) {
                secondsCount++
                updateNotification()
                delay(1000)
            }
        }

        // Запускаем сервис в foreground режиме
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun stopTimer() {
        isRunning = false
        timerJob?.cancel()
        stopForeground(true)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Таймер сервис",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Показывает счетчик секунд"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("ДниВЛюбви")
            .setContentText("Прошло секунд: $secondsCount")
            .setSmallIcon(R.drawable.ic_heart)
            .setOnlyAlertOnce(true)
            .setOngoing(true) // Несмахиваемое
            .setShowWhen(false) // Скрыть время
            .setPriority(NotificationCompat.PRIORITY_MAX) // Максимальный приоритет
            // Можно добавить кнопку для остановки
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
        stopTimer()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, ForegroundService::class.java).apply {
            action = ACTION_START
        }

        // Перезапускаем сервис через 1 секунду после закрытия
        val restartServicePendingIntent = PendingIntent.getService(
            this,
            1,
            restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(
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

        fun startService(context: android.content.Context) {
            val intent = Intent(context, ForegroundService::class.java).apply {
                action = ACTION_START
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: android.content.Context) {
            val intent = Intent(context, ForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }
}