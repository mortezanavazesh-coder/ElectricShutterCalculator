package com.morteza.shuttercalculator

import java.io.Serializable

// گزینه‌های اضافی انتخاب‌شده
data class ExtraOption(
    val name: String,        // نام گزینه
    val basePrice: Float     // قیمت پایه گزینه
) : Serializable

// مدل گزارش کامل
data class ReportModel(
    val id: String,                  // شناسه یکتا گزارش
    val customerName: String,        // نام مشتری
    val customerPhone: String?,      // شماره موبایل (اختیاری)
    val date: String,                // تاریخ شمسی ثبت گزارش

    // قطعات انتخابی + قیمت پایه
    val bladeName: String,
    val bladeBasePrice: Float,
    val motorName: String,
    val motorBasePrice: Float,
    val shaftName: String,
    val shaftBasePrice: Float,
    val boxName: String,
    val boxBasePrice: Float,

    // قیمت پایه هزینه‌ها
    val installBasePrice: Float,
    val weldingBasePrice: Float,
    val transportBasePrice: Float,

    // گزینه‌های اضافی انتخاب‌شده
    val extrasSelected: List<ExtraOption>,

    // ریز محاسبات
    val bladeTotal: Float,
    val motorTotal: Float,
    val shaftTotal: Float,
    val boxTotal: Float,
    val installTotal: Float,
    val weldingTotal: Float,
    val transportTotal: Float,
    val extrasTotal: Float,

    // جمع کل
    val total: Float
) : Serializable
