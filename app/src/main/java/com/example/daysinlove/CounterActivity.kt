package com.example.daysinlove

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.daysinlove.databinding.ActivityMainBinding
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import android.Manifest
import com.example.daysinlove.RuStoreUpdates as RuStoreUpdates

class CounterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var bannerAd: BannerAdView? = null
    private val adSize: BannerAdSize
        get() {
            var adWidthPixels = binding.adContainerView.width
            if (adWidthPixels == 0) {
                // If the ad hasn't been laid out, default to the full screen width
                adWidthPixels = resources.displayMetrics.widthPixels
            }
            val adWidth = (adWidthPixels / resources.displayMetrics.density).toInt()
            return BannerAdSize.stickySize(this, adWidth)
        }

    override fun onStart() {
        super.onStart()
    }

    private fun setupForegroundService() {
        // В MainActivity.kt
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC), 1)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        // Запуск сервиса
        ForegroundService.startService(this)
    }

    override fun onResume() {
        super.onResume()
        @RequiresApi(Build.VERSION_CODES.R)
        fun hideSystemUI() {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window,
                window.decorView.findViewById(android.R.id.content)).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())

                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        hideSystemUI()
    }

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?): Unit {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupForegroundService()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.adContainerView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.adContainerView.viewTreeObserver.removeOnGlobalLayoutListener(this);
                bannerAd = loadBannerAd(adSize)
            }
        })
        // ON READY - just dildo commit
        // HIDE NAV BAR

        hideSystemUI()

        // РАБОТА С ОБНОВЛЕНИЯМИ
        RuStoreUpdates.checkForUpdates(this)

        // list all widgets
        val labelDatesWidget = binding.labelDates
        val labelDaysTogetherWidget = binding.labelDaysTogether
        val calendarImageView = binding.calendar
        val shareBtn = binding.btnShare

        // DATE WORK
        val sharedPreferences = getSharedPreferences("storage", MODE_PRIVATE)

        val startDay = sharedPreferences.getInt("day", 17) // default value is 0 if "day" is not found
        val startMonth = sharedPreferences.getInt("month", 10) // default value is 0 if "month" is not found
        val startYear = sharedPreferences.getInt("year", 2020) // default value is 0 if "year" is not found

        val days = Funcer.getDaysTogether(this)

        labelDatesWidget.text = String.format(
            "%02d.%02d.%d - %s",
            startDay,
            startMonth + 1,
            startYear,
            Funcer.getDateString()
        )
        labelDaysTogetherWidget.text = days.toString()

        // CHECK BEAUTINESS
        if (Funcer.isBeautifulNumber(days)) {
            val drawable = ContextCompat.getDrawable(this, R.drawable.calendar_cool)
            calendarImageView.setImageDrawable(drawable)
        }
        else {
            val drawable = ContextCompat.getDrawable(this, R.drawable.calendar)
            calendarImageView.setImageDrawable(drawable)
        }

        // SHARING
        shareBtn.setOnClickListener {
            val intent = Intent.createChooser(intent, "Поделиться через:")

            intent.action = Intent.ACTION_SEND
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "\uD83D\uDC99 Мы вместе вот уже " + days.toString() + " дней \uD83E\uDDE1"
            )
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Поделиться через:"))
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    // ЗАПУСК СЕРВИСА УВЕДОМЛЕНИЙ
    private fun startNotificationService() {
        val serviceIntent = Intent(this, ForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun loadBannerAd(adSize: BannerAdSize): BannerAdView {
        return binding.adContainerView.apply {
            setAdSize(adSize)
            setAdUnitId("R-M-13447466-1")
            setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() {
                    // If this callback occurs after the activity is destroyed, you
                    // must call destroy and return or you may get a memory leak.
                    // Note `isDestroyed` is a method on Activity.
                    if (isDestroyed) {
                        bannerAd?.destroy()
                        return
                    }
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                }

                override fun onAdClicked() {
                }

                override fun onLeftApplication() {
                }

                override fun onReturnedToApplication() {
                }

                override fun onImpression(impressionData: ImpressionData?) {
                }
            })
            loadAd(
                AdRequest.Builder()
                    // Methods in the AdRequest.Builder class can be used here to specify individual options settings.
                    .build()
            )
        }
    }

}