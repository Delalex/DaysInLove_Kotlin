package com.example.daysinlove

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.collections.EmptyMap.get
import com.example.daysinlove.RuStoreUpdates as RuStoreUpdates

class CounterActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        @RequiresApi(Build.VERSION_CODES.R)
        fun hideSystemUI() {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window,
                window.decorView.findViewById(android.R.id.content)).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())

                // When the screen is swiped up at the bottom
                // of the application, the navigationBar shall
                // appear for some time
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        hideSystemUI()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?): BannerAdSize {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // ON READY


        // HIDE NAV BAR
        @RequiresApi(Build.VERSION_CODES.R)
        fun hideSystemUI() {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window,
                window.decorView.findViewById(android.R.id.content)).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())

                // When the screen is swiped up at the bottom
                // of the application, the navigationBar shall
                // appear for some time
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        hideSystemUI()

        // РАБОТА С ОБНОВЛЕНИЯМИ
        RuStoreUpdates.check_updates(this)

        // ЗАГРУЗКА РЕКЛАМЫ
        val adview = findViewById<ImageView>(R.id.ad_container_view)
        fun setAdSize(): BannerAdSize {
            // Calculate the width of the ad, taking into account the padding in the ad container.
            var adWidthPixels = findViewById<BannerAdView>(R.id.ad_container_view).width
            if (adWidthPixels == 0) {
                // If the ad hasn't been laid out, default to the full screen width
                adWidthPixels = resources.displayMetrics.widthPixels
            }
            val adWidth = (adWidthPixels / resources.displayMetrics.density).toInt()
            return BannerAdSize.stickySize(this, adWidth)
        }



        // list all widgets
        val labelDatesWidget = findViewById<TextView>(R.id.labelDates)
        val labelDaysTogetherWidget = findViewById<TextView>(R.id.labelDaysTogether)
        val calendarImageView = findViewById<ImageView>(R.id.calendar)
        val share_btn = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btnShare)

        // DATE WORK
        val sharedPreferences = getSharedPreferences("storage", MODE_PRIVATE)

        val startDay = sharedPreferences.getInt("day", 17) // default value is 0 if "day" is not found
        val startMonth = sharedPreferences.getInt("month", 10) // default value is 0 if "month" is not found
        val startYear = sharedPreferences.getInt("year", 2020) // default value is 0 if "year" is not found

        // ПОДСЧЕТ
        val date1 = LocalDate.of(startYear.toInt(), startMonth.toInt() + 1, startDay.toInt())
        val date2 = LocalDate.now()

        val days = ChronoUnit.DAYS.between(date1, date2).toInt()

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

        // SHARING
        share_btn.setOnClickListener {
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
}