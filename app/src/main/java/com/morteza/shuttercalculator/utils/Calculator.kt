package com.morteza.shuttercalculator.utils

object Calculator {

    // تبدیل سانتی‌متر به متر اگر لازم باشد
    fun toMeter(value: Double, isCentimeter: Boolean): Double {
        return if (isCentimeter) value / 100 else value
    }

    // 1. محاسبه مساحت
    fun calculateArea(width: Double, height: Double): Double {
        return width * height
    }

    // 2. قیمت تیغه = مساحت * قیمت پایه تیغه
    fun calculateShutterPrice(area: Double, slatPrice: Double): Double {
        return area * slatPrice
    }

    // 3. قیمت شفت = عرض کرکره * قیمت پایه شفت
    fun calculateShaftPrice(width: Double, shaftPrice: Double): Double {
        return width * shaftPrice
    }

    // 4. قیمت قوطی = ((ارتفاع - 0.3) * 2) * قیمت پایه قوطی
    fun calculateBoxPrice(height: Double, boxPrice: Double, include: Boolean): Double {
        val adjustedHeight = if (height > 0.3) height - 0.3 else 0.0
        val boxLength = adjustedHeight * 2
        return if (include) boxLength * boxPrice else 0.0
    }

    // 5. قیمت نصب با شرط مساحت
    fun calculateInstallPrice(area: Double, basePrice: Double): Double {
        return if (area <= 10.0) basePrice * 10 else area * basePrice
    }

    // 6. مجموع قیمت‌های چک‌باکس‌های انتخابی
    fun calculateExtras(
        electricLock: Boolean, electricLockPrice: Double,
        manualLock: Boolean, manualLockPrice: Double,
        motorCover: Boolean, motorCoverPrice: Double
    ): Double {
        var extras = 0.0
        if (electricLock) extras += electricLockPrice
        if (manualLock) extras += manualLockPrice
        if (motorCover) extras += motorCoverPrice
        return extras
    }

    // 7. قیمت نهایی
    fun calculateFinalPrice(
        shutter: Double,
        motor: Double,
        shaft: Double,
        box: Double,
        install: Double,
        welding: Double,
        transport: Double,
        extras: Double
    ): Double {
        return shutter + motor + shaft + box + install + welding + transport + extras
    }

    // 8. محاسبه قطر رول (فرمول پیشنهادی)
    fun calculateRollDiameter(width: Double, height: Double): Double {
        val base = 15.0
        val factor = (width + height) / 2
        return base + factor * 2.5
    }
}
