package com.morteza.shuttercalculator

import java.io.Serializable

// گزینه‌های اضافی انتخاب‌شده
data class ExtraOption(
    val name: String,        // نام گزینه
    val basePrice: Long      // قیمت پایه گزینه
) : Serializable

// مدل گزارش کامل
data class ReportModel(
    val id: String,                  // شناسه یکتا گزارش
    val customerName: String,        // نام مشتری
    val customerPhone: String?,      // شماره موبایل (اختیاری)
    val date: String,                // تاریخ شمسی ثبت گزارش

    // قطعات انتخابی + قیمت پایه
    val bladeName: String,
    val bladeBasePrice: Long,
    val motorName: String,
    val motorBasePrice: Long,
    val shaftName: String,
    val shaftBasePrice: Long,
    val boxName: String,
    val boxBasePrice: Long,

    // قیمت پایه هزینه‌ها
    val installBasePrice: Long,
    val weldingBasePrice: Long,
    val transportBasePrice: Long,

    // گزینه‌های اضافی انتخاب‌شده
    val extrasSelected: List<ExtraOption>,

    // ریز محاسبات
    val bladeTotal: Long,
    val motorTotal: Long,
    val shaftTotal: Long,
    val boxTotal: Long,
    val installTotal: Long,
    val weldingTotal: Long,
    val transportTotal: Long,
    val extrasTotal: Long,

    // جمع کل
    val total: Long
) : Serializable
