package com.morteza.shuttercalculator.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {

    private val tomanFormat: NumberFormat = DecimalFormat("#,###").apply {
        maximumFractionDigits = 0
    }

    fun formatToman(value: Long): String {
        return try {
            "${tomanFormat.format(value)} تومان"
        } catch (e: Exception) {
            "0 تومان"
        }
    }

    fun formatToman(value: Float): String {
        return try {
            "${tomanFormat.format(value.toLong())} تومان"
        } catch (e: Exception) {
            "0 تومان"
        }
    }

    fun formatToman(value: Double): String {
        return try {
            "${tomanFormat.format(value.toLong())} تومان"
        } catch (e: Exception) {
            "0 تومان"
        }
    }

    fun formatTomanPlain(value: Long): String {
        return try {
            tomanFormat.format(value)
        } catch (e: Exception) {
            "0"
        }
    }

    fun formatTomanPlain(value: Float): String {
        return try {
            tomanFormat.format(value.toLong())
        } catch (e: Exception) {
            "0"
        }
    }

    fun formatTomanPlain(value: Double): String {
        return try {
            tomanFormat.format(value.toLong())
        } catch (e: Exception) {
            "0"
        }
    }

    // پاک‌سازی امن: حذف هر کاراکتر غیرعددی و تبدیل به Long
    fun parseTomanInput(input: String?): Long {
        if (input.isNullOrBlank()) return 0L
        return try {
            val cleaned = input
                .replace(",", "")
                .replace(" ", "")
                .replace("تومان", "", ignoreCase = true)
                .replace("[^\\d-]".toRegex(), "")
            cleaned.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun getTodayDate(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d/%02d/%02d", year, month, day)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.US)
        return sdf.format(Date(timestamp))
    }
}
