package com.example.daysinlove

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

class ForegroundService : Service() {
    private val channelId = "DaysInLoveServiceChannel"
    private lateinit var notificationManager: NotificationManager
    private var daysCount = 0
    private var alarmManager: AlarmManager? = null
    private var wakeLock: PowerManager.WakeLock? = null

    // Двойной механизм перезапуска (AlarmManager + BroadcastReceiver)
    private var restartPendingIntent: PendingIntent? = null
    private var dailyUpdatePendingIntent: PendingIntent? = null
        private set // Запрещаем внешнее изменение

    private val handler = Handler(Looper.getMainLooper())
    private val notificationCheckRunnable = object : Runnable {
        override fun run() {
            ensureNotificationActive()
            handler.postDelayed(this, 5000) // Проверка каждые 5 секунд
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        acquireWakeLock()
        createNotificationChannel()
        setupDailyUpdate()
        startNotificationChecker()
        Log.d("ForegroundService", "Service onCreate()")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ForegroundService", "Service onStartCommand() with action: ${intent?.action}")
        when (intent?.action) {
            ACTION_START -> startForegroundService()
            ACTION_STOP -> stopForegroundService()
            ACTION_UPDATE -> updateDaysCount()
            else -> startForegroundService() // По умолчанию запускаем
        }
        return START_STICKY // Важно для автоматического перезапуска
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "DaysInLove::WakeLock"
        ).apply {
            acquire(10*60*1000L /*10 minutes*/)
        }
    }

    private fun startForegroundService() {
        updateDaysCount()
        startForeground(NOTIFICATION_ID, createNotification())
        setupAutoRestart()
    }

    private fun stopForegroundService() {
        stopNotificationChecker()
        releaseWakeLock()
        stopForeground(true)
        stopSelf()
        cancelAutoRestart()
        cancelDailyUpdate()
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
        dailyUpdatePendingIntent = PendingIntent.getService(
            this,
            DAILY_UPDATE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Устанавливаем ежедневное обновление
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Используем локальную копию для thread-safety
        val pi = dailyUpdatePendingIntent
        if (pi != null) {
            alarmManager?.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pi
            )
        }
    }

    private fun setupAutoRestart() {
        cancelAutoRestart()

        val restartIntent = Intent(this, RestartReceiver::class.java).apply {
            action = ACTION_RESTART
        }

        restartPendingIntent = PendingIntent.getBroadcast(
            this,
            RESTART_REQUEST_CODE,
            restartIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun cancelAutoRestart() {
        restartPendingIntent?.let {
            alarmManager?.cancel(it)
            restartPendingIntent = null
        }
    }

    private fun cancelDailyUpdate() {
        dailyUpdatePendingIntent?.let {
            alarmManager?.cancel(it)
            dailyUpdatePendingIntent = null
        }
    }

    private fun startNotificationChecker() {
        handler.post(notificationCheckRunnable)
    }

    private fun stopNotificationChecker() {
        handler.removeCallbacks(notificationCheckRunnable)
    }

    private fun ensureNotificationActive() {
        if (!isNotificationActive()) {
            Log.w("ForegroundService", "Notification was removed, restarting...")
            startForeground(NOTIFICATION_ID, createNotification())
        }
    }

    private fun isNotificationActive(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.activeNotifications.any { it.id == NOTIFICATION_ID }
        } else {
            // Для старых версий Android нет надежного способа проверить
            true
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ДниВЛюбви",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Показывает количество дней вместе"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("ДниВЛюбви")
            .setContentText("Дней вместе: $daysCount")
            .setSmallIcon(R.drawable.ic_heart)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN) // MIN чтобы не беспокоить пользователя
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setAutoCancel(false)
            .setDeleteIntent(createDeleteIntent())
            .build().apply {
                flags = flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_FOREGROUND_SERVICE
            }
    }

    private fun createDeleteIntent(): PendingIntent {
        val intent = Intent(this, ForegroundService::class.java).apply {
            action = ACTION_START
        }
        return PendingIntent.getService(
            this,
            DELETE_INTENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateNotification() {
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
            wakeLock = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ForegroundService", "Service onDestroy()")
        // Планируем перезапуск при уничтожении сервиса
        scheduleRestart()
        stopNotificationChecker()
        releaseWakeLock()
    }

    private fun scheduleRestart() {
        val restartIntent = Intent(this, RestartReceiver::class.java).apply {
            action = ACTION_RESTART
        }
        val restartPendingIntent = PendingIntent.getBroadcast(
            this,
            RESTART_REQUEST_CODE,
            restartIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1000,
            restartPendingIntent
        )
    }

    companion object {
        const val NOTIFICATION_ID = 12345 // Уникальный ID
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_UPDATE = "ACTION_UPDATE"
        const val ACTION_RESTART = "ACTION_RESTART"

        private const val RESTART_REQUEST_CODE = 1001
        private const val DAILY_UPDATE_REQUEST_CODE = 1002
        private const val DELETE_INTENT_REQUEST_CODE = 1003

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
            context.startService(intent) // Используем startService для гарантированного выполнения
        }
    }
}

class RestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ForegroundService.ACTION_RESTART) {
            Log.d("RestartReceiver", "Restarting service...")
            ForegroundService.startService(context)
        }
    }
}