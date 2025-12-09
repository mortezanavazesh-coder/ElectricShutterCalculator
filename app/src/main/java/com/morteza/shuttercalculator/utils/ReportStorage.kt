package com.morteza.shuttercalculator.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.morteza.shuttercalculator.ReportModel

object ReportStorage {

    private const val PREFS_NAME = "reports_prefs"
    private const val KEY_REPORTS = "reports_list"

    private val gson = Gson()

    // ذخیره یک گزارش جدید
    fun saveReport(context: Context, report: ReportModel) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val reports = loadReports(context).toMutableList()
        reports.add(report)
        val json = gson.toJson(reports)
        prefs.edit().putString(KEY_REPORTS, json).apply()
    }

    // بارگذاری همه گزارش‌ها
    fun loadReports(context: Context): List<ReportModel> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_REPORTS, null) ?: return emptyList()
        val type = object : TypeToken<List<ReportModel>>() {}.type
        return gson.fromJson(json, type)
    }

    // حذف یک گزارش
    fun deleteReport(context: Context, report: ReportModel) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val reports = loadReports(context).toMutableList()
        reports.remove(report)
        val json = gson.toJson(reports)
        prefs.edit().putString(KEY_REPORTS, json).apply()
    }

    // پاک کردن همه گزارش‌ها (اختیاری)
    fun clearAllReports(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_REPORTS).apply()
    }
}
