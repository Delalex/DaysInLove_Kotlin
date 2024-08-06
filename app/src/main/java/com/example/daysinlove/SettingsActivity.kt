package com.example.daysinlove

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.transition.Visibility
import java.security.AccessController.getContext

class MainActivity : AppCompatActivity() {

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val sharedPreferences = getSharedPreferences("storage", MODE_PRIVATE)
        val isAuthed = sharedPreferences.getBoolean("isReady", false)

        // SKIP SETTINGS
        if (isAuthed) {
            val intent = Intent(this@MainActivity, CounterActivity::class.java)
            startActivity(intent)
            finish()
        }

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

        // WIDGET LIST
        val done_btn = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btnDone)
        val calendarView = findViewById<CalendarView>(R.id.calendarView)

        // CALENDAR REWORK
        calendarView.setOnDateChangeListener{calendarView, year, month, dayOfMonth ->
            val msg = "Selected date: " + dayOfMonth.toString()

            val sharedPreferences = getSharedPreferences("storage", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("day", dayOfMonth)
            editor.putInt("month", month)
            editor.putInt("year", year)
            editor.apply()
        }

        // DONE BUTTON HANDLING
        done_btn.setOnClickListener {
            val sharedPreferences = getSharedPreferences("storage", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("isReady", true)
            editor.apply()

            val intent = Intent(this@MainActivity, CounterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}