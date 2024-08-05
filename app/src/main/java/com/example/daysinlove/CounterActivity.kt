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
import androidx.core.view.WindowInsetsCompat
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CounterActivity : AppCompatActivity() {
    override fun onBackPressed() {
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // ON READY
        // list all widgets
        val labelDatesWidget = findViewById<TextView>(R.id.labelDates)
        val labelDaysTogetherWidget = findViewById<TextView>(R.id.labelDaysTogether)
        val calendarImageView = findViewById<ImageView>(R.id.calendar)
        val share_btn = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btnShare)

        // DATE WORK
        val sharedPreferences = getSharedPreferences("storage", MODE_PRIVATE)

        val start_day = sharedPreferences.getInt("day", 0) // default value is 0 if "day" is not found
        val start_month = sharedPreferences.getInt("month", 0) // default value is 0 if "month" is not found
        val start_year = sharedPreferences.getInt("year", 0) // default value is 0 if "year" is not found

        val from = LocalDate.of(start_year.toInt(), start_month.toInt() + 1, start_day.toInt())
        val to = LocalDate.now()
        val dayInterwal = ChronoUnit.DAYS.between(from, to)
        val days = dayInterwal.toInt()

        labelDatesWidget.text = start_day.toString().padStart(2, '0') + '.' + (start_month + 1).toString().padStart(2, '0') + '.' + start_year.toString() + " - " + Funcer.getDateString()
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