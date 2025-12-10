package com.morteza.shuttercalculator.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.morteza.shuttercalculator.ReportModel

object ReportStorage {

    private const val PREFS_NAME = "reports_prefs"
    private const val KEY_REPORTS = "reports_list"

    private val gson = Gson()

    // ذخیره یا به‌روزرسانی یک گزارش (upsert بر اساس id)
    fun saveReport(context: Context, report: ReportModel) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val reports = loadReports(context).toMutableList()

        val index = reports.indexOfFirst { it.id == report.id }
        if (index >= 0) {
            reports[index] = report
        } else {
            reports.add(report)
        }

        prefs.edit().putString(KEY_REPORTS, gson.toJson(reports)).apply()
    }

    // بارگذاری همه گزارش‌ها (ایمن در برابر نال و JSON خراب)
    fun loadReports(context: Context): List<ReportModel> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_REPORTS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ReportModel>>() {}.type
            gson.fromJson<List<ReportModel>>(json, type) ?: emptyList()
        } catch (e: JsonSyntaxException) {
            // در صورت خراب بودن JSON، پاک‌سازی نرم و بازگشت لیست خالی
            emptyList()
        }
    }

    // حذف یک گزارش بر اساس id (ایمن‌تر از remove با برابری آبجکت)
    fun deleteReport(context: Context, report: ReportModel) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val reports = loadReports(context).toMutableList()
        val removed = reports.removeAll { it.id == report.id }
        if (removed) {
            prefs.edit().putString(KEY_REPORTS, gson.toJson(reports)).apply()
        }
    }

    // پاک کردن همه گزارش‌ها
    fun clearAllReports(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_REPORTS).apply()
    }

    // ایجاد یک id جدید پیشنهادی (اختیاری برای استفاده هنگام ساخت ReportModel)
    fun generateId(): Long = System.currentTimeMillis()
}
