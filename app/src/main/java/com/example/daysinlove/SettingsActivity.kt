package com.example.daysinlove

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.CalendarView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.security.AccessController.getContext

class MainActivity : AppCompatActivity() {
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

            Toast.makeText(this, "Selected date: " + msg, Toast.LENGTH_SHORT).show()
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