package com.morteza.shuttercalculator.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object FormatUtils {

    // format toman with thousand separators, e.g. 2300000 -> "2,300,000"
    fun formatToman(value: Float): String {
        val df = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
        return df.format(value.toDouble()) + " تومان"
    }

    // return plain grouped string suitable for EditText initial set (no currency suffix)
    fun formatTomanPlain(value: Float): String {
        val df = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
        return df.format(value.toDouble())
    }

    // parse input strings like "2,300,000" or "2.300.000" or "2300000" into float
    // handles common separators (comma, dot, arabic comma)
    fun parseTomanInput(input: String?): Float {
        if (input == null) return 0f
        var s = input.trim()
        if (s.isEmpty()) return 0f

        // remove non-digit except possible decimal separators.
        // In this app we treat both dot and comma as thousand separators (not decimal)
        // so remove them all. If you need decimal support, adjust logic.
        s = s.replace("\\s".toRegex(), "")
        s = s.replace(",", "")
        s = s.replace("٬", "") // arabic thousands sep
        s = s.replace(".", "")

        return try {
            s.toFloat()
        } catch (e: Exception) {
            0f
        }
    }
}
