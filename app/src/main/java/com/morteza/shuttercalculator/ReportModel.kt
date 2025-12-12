package com.morteza.shuttercalculator

import java.io.Serializable

data class ReportModel(
    val id: Long,
    val customerName: String,
    val date: Long,            // ذخیره به صورت timestamp برای مرتب‌سازی بهتر
    val height: Float,
    val width: Float,
    val area: Float,           // مساحت به صورت عددی
    val blade: String,
    val motor: String,
    val shaft: String,
    val box: String,
    val install: Float,        // هزینه نصب
    val welding: Float,        // هزینه جوشکاری
    val transport: Float,      // هزینه حمل
    val extras: Float,         // هزینه اضافات
    val total: Float           // جمع کل به صورت عددی
) : Serializable
