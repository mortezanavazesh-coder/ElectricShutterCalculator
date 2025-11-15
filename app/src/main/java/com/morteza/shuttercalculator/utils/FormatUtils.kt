package com.morteza.shuttercalculator.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object FormatUtils {
    private val dfNoFraction by lazy {
        val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ','; decimalSeparator = '.' }
        DecimalFormat("#,###", symbols)
    }

    fun formatToman(value: Float): String {
        return try {
            dfNoFraction.format(value.toLong()) + " تومان"
        } catch (e: Exception) {
            "${value} تومان"
        }
    }

    fun formatTomanPlain(value: Float): String {
        return try { dfNoFraction.format(value.toLong()) } catch (e: Exception) { value.toString() }
    }

    fun parseTomanInput(input: String?): Float {
        if (input == null) return 0f
        val cleaned = input.replace(",", "").replace(" ", "").trim()
        if (cleaned.isEmpty()) return 0f
        return try { cleaned.toFloat() } catch (e: NumberFormatException) { 0f }
    }
}
