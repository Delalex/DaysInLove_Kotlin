package com.example.daysinlove

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.CalendarView
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.time.DateTimeException
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: android.content.SharedPreferences
    private var isAuthed: Boolean = false
    private var dateIsPicked: Boolean = false
    private lateinit var doneBtn: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var calendarView: CalendarView
    private lateinit var editTextDate: EditText

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    private fun isValidDate(day: Int, month: Int, year: Int): Boolean {
        return try {
            // Проверка на корректность даты
            if (day < 1 || month < 1 || year < 1) return false

            // Проверка на високосный год для февраля
            if (month == 2) {
                val isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
                return day <= if (isLeapYear) 29 else 28
            }

            // Проверка для месяцев с 30 днями
            if (month in listOf(4, 6, 9, 11) && day > 30) return false

            // Все остальные месяцы имеют максимум 31 день
            day <= 31
        } catch (e: Exception) {
            false
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

    private fun setEditTextDateListener() {
        editTextDate.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var deletingHyphen = false
            private var hyphenStart = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (isFormatting) return

                // Запоминаем позицию точки при удалении
                if (count == 1 && after == 0 && s?.getOrNull(start) == '.') {
                    deletingHyphen = true
                    hyphenStart = start
                } else {
                    deletingHyphen = false
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isFormatting) return

                // При удалении точки удаляем и предыдущий символ
                if (deletingHyphen && hyphenStart > 0) {
                    editTextDate.text?.delete(hyphenStart - 1, hyphenStart)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true

                s?.let {
                    val text = it.toString().replace(".", "")
                    val length = text.length

                    if (length > 0) {
                        val formatted = buildString {
                            append(text.substring(0, min(2, length)))
                            if (length > 2) append(".").append(text.substring(2, min(4, length)))
                            if (length > 4) append(".").append(text.substring(4, min(8, length)))
                        }

                        it.replace(0, it.length, formatted)
                        editTextDate.setSelection(it.length)
                    }
                }

                isFormatting = false

                // Добавьте этот вызов в конец метода:
                if (s?.length == 10) { // Проверяем полный формат dd.MM.yyyy
                    parseAndSetCalendarDate(s.toString())
                }
            }
        })
    }

    private fun setCalendarListener() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            try {
                val dateStr = "%02d.%02d.%d".format(dayOfMonth, month + 1, year)
                editTextDate.setText(dateStr)
                // Добавьте этот вызов:
                parseAndSetCalendarDate(dateStr)
                dateIsPicked = true
            } catch (e: DateTimeException) {
                dateIsPicked = false
                editTextDate.error = "Некорректная дата"
            }
        }
    }

    private fun setDoneBtnListener() {
        doneBtn.setOnClickListener {
            if (dateIsPicked) {
                sharedPreferences.edit().apply {
                    putBoolean("isReady", true)
                    apply()
                }

                startActivity(Intent(this@MainActivity, CounterActivity::class.java))
                finish()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseAndSetCalendarDate(dateStr: String) {
        try {
            // Проверяем формат строки
            if (!dateStr.matches(Regex("""\d{2}\.\d{2}\.\d{4}"""))) {
                editTextDate.error = "Формат: дд.мм.гггг"
                dateIsPicked = false
                return
            }

            val parts = dateStr.split(".")
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()

            // Проверка валидности даты
            when {
                day !in 1..31 -> editTextDate.error = "День должен быть 1-31"
                month !in 1..12 -> editTextDate.error = "Месяц должен быть 1-12"
                year < 1900 -> editTextDate.error = "Год должен быть ≥ 1900"
                !isValidDate(day, month, year) -> editTextDate.error = "Несуществующая дата"
                else -> {
                    Calendar.getInstance().apply {
                        set(year, month - 1, day)
                        calendarView.date = timeInMillis
                    }
                    sharedPreferences.edit().apply {
                        putInt("day", day)
                        putInt("month", month) // month уже в формате 0-11
                        putInt("year", year)
                        apply()
                    }
                    dateIsPicked = true
                    editTextDate.error = null
                    return
                }
            }

            dateIsPicked = false
        } catch (e: Exception) {
            dateIsPicked = false
            editTextDate.error = "Неверный формат даты"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_startup)

        // Инициализация view-элементов
        doneBtn = findViewById(R.id.btnDone)
        calendarView = findViewById(R.id.calendarView)
        editTextDate = findViewById(R.id.editTextDate)
        sharedPreferences = getSharedPreferences("storage", MODE_PRIVATE)
        isAuthed = sharedPreferences.getBoolean("isReady", false)

        if (isAuthed) {
            startActivity(Intent(this, CounterActivity::class.java))
            finish()
            return
        }

        hideSystemUI()
        setEditTextDateListener()
        setCalendarListener()
        setDoneBtnListener()
    }
}