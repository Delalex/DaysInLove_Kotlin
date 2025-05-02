package com.example.daysinlove

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date

class Funcer {
    companion object {
        // Add context parameter to the function
        fun getDaysTogether(context: Context): Int {
            val sharedPreferences = context.getSharedPreferences("storage", Context.MODE_PRIVATE)
            val startDay = sharedPreferences.getInt("day", 17)
            val startMonth = sharedPreferences.getInt("month", 9) // 0-11 (октябрь)
            val startYear = sharedPreferences.getInt("year", 2020)

            return try {
                // Правильный порядок: year, month, day
                val startDate = LocalDate.of(startYear, startMonth, startDay)
                val endDate = LocalDate.now()
                ChronoUnit.DAYS.between(startDate, endDate).toInt()
            } catch (e: DateTimeException) {
                // Если дата невалидна, возвращаем дни с даты по умолчанию
                val defaultDate = LocalDate.of(2020, 10, 17) // 17 октября 2020
                ChronoUnit.DAYS.between(defaultDate, LocalDate.now()).toInt()
            }
        }

        fun isBeautifulNumber(n: Int): Boolean {
            val str = n.toString()

            // Числа из повторяющихся 17 являются красивыми
            if (str.length % 2 == 0 && str.chunked(2).all { it == "17" }) return true

            // Числа состоящие из одной повторяющейся цифры - красивые
            if (str.all { it == str[0] }) return true

            // Числа симметричные типа 1881 - красивые
            if (str == str.reversed()) return true

            // Целые числа являются красивыми
            if (n % 10 == 0) return true

            // Остальные числа - не красивые
            return false
        }

        fun getDateString(): String {
            val data_format = SimpleDateFormat("dd.MM.yyyy")
            val currentDate = data_format.format(Date())
            return currentDate.toString()
        }
    }
}