package com.morteza.shuttercalculator

import java.io.Serializable

data class ReportModel(
    val id: Long,               // شناسه یکتا گزارش
    val customerName: String,   // نام مشتری
    val date: String,           // تاریخ گزارش
    val height: Float,          // ارتفاع کرکره
    val width: Float,           // عرض کرکره
    val area: String,           // مساحت محاسبه‌شده (مثلاً "12 متر مربع")
    val blade: String,          // تیغه انتخابی
    val motor: String,          // موتور انتخابی
    val shaft: String,          // شفت انتخابی
    val box: String,            // قوطی انتخابی
    val install: String,        // هزینه نصب
    val welding: String,        // هزینه جوشکاری
    val transport: String,      // هزینه حمل
    val extras: String,         // گزینه‌های اضافی
    val total: String           // جمع کل هزینه‌ها
) : Serializable
