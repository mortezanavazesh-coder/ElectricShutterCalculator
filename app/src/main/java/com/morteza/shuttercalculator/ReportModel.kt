package com.morteza.shuttercalculator

import java.io.Serializable

data class ReportModel(
    val id: Long,
    val customerName: String,   // نام مشتری از ورودی گرفته می‌شود
    val date: String,           // تاریخ به صورت رشته قابل‌خواندن ذخیره می‌شود
    val height: Float,
    val width: Float,
    val area: Float,            // مساحت به صورت عددی
    val blade: String,
    val motor: String,
    val shaft: String,
    val box: String,
    val install: Float,         // هزینه نصب
    val welding: Float,         // هزینه جوشکاری
    val transport: Float,       // هزینه حمل
    val extras: Float,          // هزینه اضافات
    val total: Float            // جمع کل به صورت عددی
) : Serializable
