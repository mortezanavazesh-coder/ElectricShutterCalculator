package com.morteza.shuttercalculator.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {

    // فرمت جداکننده هزارگان
    private val tomanFormat: NumberFormat = DecimalFormat("#,###").apply {
        maximumFractionDigits = 0
    }

    // فرمت کردن عدد Long به تومان با جداکننده هزار
    fun formatToman(value: Long): String {
        return try {
            "${tomanFormat.format(value)} تومان"
        } catch (e: Exception) {
            "0 تومان"
        }
    }

    // فرمت کردن عدد Float به تومان با جداکننده هزار
    fun formatToman(value: Float): String {
        return try {
            "${tomanFormat.format(value.toLong())} تومان"
        } catch (e: Exception) {
            "0 تومان"
        }
    }

    // فرمت کردن عدد Double به تومان با جداکننده هزار
    fun formatToman(value: Double): String {
        return try {
            "${tomanFormat.format(value.toLong())} تومان"
        } catch (e: Exception) {
            "0 تومان"
        }
    }

    // فرمت کردن عدد Long بدون متن "تومان"
    fun formatTomanPlain(value: Long): String {
        return try {
            tomanFormat.format(value)
        } catch (e: Exception) {
            "0"
        }
    }

    // فرمت کردن عدد Float بدون متن "تومان"
    fun formatTomanPlain(value: Float): String {
        return try {
            tomanFormat.format(value.toLong())
        } catch (e: Exception) {
            "0"
        }
    }

    // فرمت کردن عدد Double بدون متن "تومان"
    fun formatTomanPlain(value: Double): String {
        return try {
            tomanFormat.format(value.toLong())
        } catch (e: Exception) {
            "0"
        }
    }

    // پارس کردن ورودی کاربر (مثلاً "12,500") به Long
    fun parseTomanInput(input: String?): Long {
        if (input.isNullOrBlank()) return 0L
        return try {
            input.replace(",", "").toLong()
        } catch (e: Exception) {
            0L
        }
    }

    // گرفتن تاریخ امروز به صورت yyyy/MM/dd
    fun getTodayDate(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d/%02d/%02d", year, month, day)
    }

    // فرمت کردن timestamp (Long) به yyyy/MM/dd
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
