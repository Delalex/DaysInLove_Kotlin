package com.example.daysinlove

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Date
import android.os.Vibrator
import android.os.VibratorManager

class Funcer {
    companion object {
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
        @RequiresApi(Build.VERSION_CODES.O)
        fun getDays(): Int {
            val data_format = SimpleDateFormat("dd.MM.yyyy")
            val currentDate = data_format.format(Date())
            val from = LocalDate.of(2020, 10, 17)
            val to = LocalDate.now()
            val daysBetween = ChronoUnit.DAYS.between(from, to)
            return daysBetween.toInt()
        }
        fun getDateString(): String {
            val data_format = SimpleDateFormat("dd.MM.yyyy")
            val currentDate = data_format.format(Date())
            return currentDate.toString()
        }
    }
}