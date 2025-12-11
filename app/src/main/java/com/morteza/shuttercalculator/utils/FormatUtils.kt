package com.morteza.shuttercalculator.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object FormatUtils {

    private val tomanFormat: NumberFormat = DecimalFormat("#,###")

    // فرمت کردن عدد به تومان با جداکننده هزار
    fun formatToman(value: Float): String {
        return try {
            "${tomanFormat.format(value)} تومان"
        } catch (e: Exception) {
            "0 تومان"
        }
    }

    // فرمت کردن عدد به تومان بدون متن "تومان"
    fun formatTomanPlain(value: Float): String {
        return try {
            tomanFormat.format(value)
        } catch (e: Exception) {
            "0"
        }
    }

    // پارس کردن ورودی کاربر (مثلاً "12,500") به Float
    fun parseTomanInput(input: String?): Float {
        if (input.isNullOrBlank()) return 0f
        return try {
            input.replace(",", "").toFloat()
        } catch (e: Exception) {
            0f
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
}

