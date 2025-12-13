package com.morteza.shuttercalculator

import java.io.Serializable

data class ReportModel(
    val id: String,              // شناسه یکتا گزارش
    val customerName: String,    // نام مشتری
    val customerPhone: String?,  // شماره موبایل مشتری (اختیاری)
    val date: String,            // تاریخ ذخیره گزارش
    val height: Float,           // ارتفاع کرکره
    val width: Float,            // عرض کرکره
    val area: Float,             // مساحت (متر مربع)
    val blade: String,           // تیغه انتخابی
    val motor: String,           // موتور انتخابی
    val shaft: String,           // شفت انتخابی
    val box: String,             // قوطی انتخابی یا محاسبه نشده
    val install: Float,          // هزینه نصب
    val welding: Float,          // هزینه جوشکاری
    val transport: Float,        // هزینه حمل
    val extras: Float,           // هزینه گزینه‌های اضافی
    val total: Float             // جمع کل
) : Serializable   // ← اضافه شد تا بتوان در Intent پاس داد
