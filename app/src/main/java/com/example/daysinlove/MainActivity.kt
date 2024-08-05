package com.example.daysinlove

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
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

        // date work
        val daysBetween = Funcer.getDays()

        labelDatesWidget.text = "17.10.2020 - " + Funcer.getDateString()
        labelDaysTogetherWidget.text = daysBetween.toString()

        // CHECK BEAUTINESS
        if (Funcer.isBeautifulNumber(daysBetween)) {
            val drawable = ContextCompat.getDrawable(this, R.drawable.calendar_cool)
            calendarImageView.setImageDrawable(drawable)
        }

        // SHARING
        share_btn.setOnClickListener {
            val intent = Intent.createChooser(intent, "Поделиться через:")

            intent.action = Intent.ACTION_SEND
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "\uD83D\uDC99 Мы вместе вот уже " + daysBetween.toString() + " дней \uD83E\uDDE1"
            )
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Поделиться через:"))
        }
    }
}