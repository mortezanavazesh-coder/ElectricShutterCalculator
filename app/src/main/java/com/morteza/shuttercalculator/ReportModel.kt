package com.morteza.shuttercalculator

// مدل داده گزارش
data class ReportModel(
    val id: Long,                // شناسه یکتا
    val customerName: String,    // نام مشتری
    val date: String,            // تاریخ گزارش
    val height: Float,           // ارتفاع کرکره (cm)
    val width: Float,            // عرض کرکره (cm)
    val area: String,            // مساحت محاسبه‌شده (نمایش متنی)
    val blade: String,           // تیغه انتخابی
    val motor: String,           // موتور انتخابی
    val shaft: String,           // شفت انتخابی
    val box: String,             // قوطی انتخابی یا محاسبه نشده
    val install: String,         // هزینه نصب محاسبه‌شده
    val welding: String,         // هزینه جوشکاری
    val transport: String,       // هزینه حمل
    val extras: String,          // گزینه‌های اضافی
    val total: String            // جمع کل
)
