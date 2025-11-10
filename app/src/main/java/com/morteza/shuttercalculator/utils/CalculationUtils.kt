package com.morteza.shuttercalculator.utils

import android.content.Context
import kotlin.math.sqrt


object CalculationUtils {

    // دریافت قیمت آیتم از حافظه
    fun getPrice(context: Context, category: String, name: String): Float {
        return PrefsHelper.getFloat(context, "${category}_price_$name")
    }

    // دریافت ویژگی خاص از آیتم (مثلاً ضخامت یا ارتفاع تیغه)
    fun getMeta(context: Context, category: String, name: String, field: String): Float {
        return PrefsHelper.getFloat(context, "${category}_${field}_$name")
    }

    // محاسبه مساحت کرکره
    fun calculateArea(width: Float, height: Float): Float {
        return width * height
    }

    // محاسبه هزینه نصب بر اساس مساحت و نرخ پایه
    fun calculateInstallPrice(area: Float, baseRate: Float): Float {
        return if (area <= 10f) 10f * baseRate else area * baseRate
    }

    // محاسبه قیمت نهایی کرکره
    fun calculateTotalPrice(
        slatPrice: Float,
        motorPrice: Float,
        shaftPrice: Float,
        shaftLength: Float,
        boxPrice: Float,
        boxLength: Float,
        includeBox: Boolean,
        install: Float,
        welding: Float,
        transport: Float
    ): Float {
        var total = slatPrice + motorPrice + (shaftPrice * shaftLength) + install + welding + transport
        if (includeBox) {
            total += boxPrice * boxLength
        }
        return total
    }

    // تبدیل واحد میلی‌متر به متر
    fun mmToMeter(mm: Float): Float {
        return mm / 1000f
    }

    // تبدیل واحد متر مربع به سانتی‌متر مربع
    fun m2ToCm2(m2: Float): Float {
        return m2 * 10000f
    }
	fun calculateRollDiameter(
        slatHeightMm: Float,
        slatThicknessMm: Float,
        slatCount: Int
    ): Float {
        val h = slatHeightMm / 1000.0  // تبدیل به متر به صورت Double
        val t = slatThicknessMm / 1000.0
        val n = slatCount.toDouble()

        // فرمول تقریبی قطر رول
        val diameter = sqrt((h * h * n) + (t * t * n))
        return diameter.toFloat()
     }

}

