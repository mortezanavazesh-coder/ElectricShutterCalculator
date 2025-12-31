package com.morteza.shuttercalculator.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {

    // استفاده از نمادهای فارسی/عربی برای جداکننده‌ها
    private val decimalSymbols: DecimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale("fa")).apply {
        groupingSeparator = '٬' // U+066C Arabic Thousands Separator (commonly used in Persian)
        decimalSeparator = '٫'  // U+066B Arabic Decimal Separator
    }

    private val tomanFormat: NumberFormat = DecimalFormat("#,###", decimalSymbols).apply {
        maximumFractionDigits = 0
        isGroupingUsed = true
    }

    // تبدیل ارقام لاتین به ارقام فارسی
    private fun toPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val c = when (ch) {
                '0' -> '\u06F0'
                '1' -> '\u06F1'
                '2' -> '\u06F2'
                '3' -> '\u06F3'
                '4' -> '\u06F4'
                '5' -> '\u06F5'
                '6' -> '\u06F6'
                '7' -> '\u06F7'
                '8' -> '\u06F8'
                '9' -> '\u06F9'
                ',' -> '٬' // تبدیل کاما به جداکنندهٔ هزارگان فارسی
                '.' -> '٫' // تبدیل نقطه به جداکنندهٔ اعشار فارسی
                else -> ch
            }
            sb.append(c)
        }
        return sb.toString()
    }

    // تبدیل ارقام فارسی/عربی به لاتین (برای پارس ورودی)
    private fun normalizeDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val c = when (ch) {
                in '\u06F0'..'\u06F9' -> ('0' + (ch - '\u06F0')) // Persian digits
                in '\u0660'..'\u0669' -> ('0' + (ch - '\u0660')) // Arabic-Indic digits
                '\u200C', '\u00A0' -> ' ' // ZWNJ or NBSP -> normal space
                '٬' -> ',' // Persian thousands separator -> comma for parsing
                '٫' -> '.' // Persian decimal separator -> dot for parsing
                else -> ch
            }
            sb.append(c)
        }
        return sb.toString()
    }

    // فرمت با واحد تومان و ارقام فارسی
    fun formatToman(value: Long): String {
        return try {
            val formatted = tomanFormat.format(value)
            "${toPersianDigits(formatted)} تومان"
        } catch (e: Exception) {
            "۰ تومان"
        }
    }

    fun formatToman(value: Float): String {
        return try {
            val formatted = tomanFormat.format(value.toLong())
            "${toPersianDigits(formatted)} تومان"
        } catch (e: Exception) {
            "۰ تومان"
        }
    }

    fun formatToman(value: Double): String {
        return try {
            val formatted = tomanFormat.format(value.toLong())
            "${toPersianDigits(formatted)} تومان"
        } catch (e: Exception) {
            "۰ تومان"
        }
    }

    // فرمت بدون واحد (برای EditText) با ارقام فارسی
    fun formatTomanPlain(value: Long): String {
        return try {
            toPersianDigits(tomanFormat.format(value))
        } catch (e: Exception) {
            "۰"
        }
    }

    fun formatTomanPlain(value: Float): String {
        return try {
            toPersianDigits(tomanFormat.format(value.toLong()))
        } catch (e: Exception) {
            "۰"
        }
    }

    fun formatTomanPlain(value: Double): String {
        return try {
            toPersianDigits(tomanFormat.format(value.toLong()))
        } catch (e: Exception) {
            "۰"
        }
    }

    // پاک‌سازی امن: تبدیل ارقام فارسی/عربی به لاتین، حذف واحد و کاراکترهای غیرعددی، سپس تبدیل به Long
    fun parseTomanInput(input: String?): Long {
        if (input.isNullOrBlank()) return 0L
        return try {
            val normalized = normalizeDigits(input)
            val cleaned = normalized
                .replace("تومان", "", ignoreCase = true)
                .replace("ت", "", ignoreCase = true)
                .replace(" ", "")
                .replace("\u200C", "") // ZWNJ
                .replace("[^\\d-\\.]".toRegex(), "") // اجازه می‌دهیم نقطه برای اعشار (در صورت نیاز) و منفی
            if (cleaned.isBlank() || cleaned == "-" || cleaned == ".") return 0L
            // اگر اعشار وجود داشت، آن را به Long تبدیل می‌کنیم (تومان معمولاً عدد صحیح)
            val asDouble = cleaned.toDoubleOrNull() ?: 0.0
            asDouble.toLong()
        } catch (e: Exception) {
            0L
        }
    }

    // تاریخ امروز به فرمت yyyy/MM/dd
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
