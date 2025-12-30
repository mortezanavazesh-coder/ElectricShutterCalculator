package com.morteza.shuttercalculator.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.morteza.shuttercalculator.ReportModel

object ReportStorage {

    private const val PREFS_NAME = "reports"
    private const val KEY_REPORTS = "all_reports"
    private const val TAG = "ReportStorage"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun generateId(): Long = System.currentTimeMillis()

    fun saveReport(context: Context, report: ReportModel) {
        try {
            val prefs = getPrefs(context)
            val gson = Gson()
            val type = object : TypeToken<MutableList<ReportModel>>() {}.type

            // لیست فعلی گزارش‌ها
            val currentJson = prefs.getString(KEY_REPORTS, null)
            val reports: MutableList<ReportModel> =
                if (currentJson != null) gson.fromJson(currentJson, type) else mutableListOf()

            // اضافه کردن گزارش جدید
            reports.add(report)

            // ذخیره دوباره لیست کامل
            prefs.edit().putString(KEY_REPORTS, gson.toJson(reports)).apply()
            Log.d(TAG, "Report saved: ${report.id}")
        } catch (e: Exception) {
            Log.e(TAG, "saveReport failed", e)
        }
    }

    fun loadReports(context: Context): List<ReportModel> {
        val prefs = getPrefs(context)
        val gson = Gson()
        val type = object : TypeToken<List<ReportModel>>() {}.type
        val json = prefs.getString(KEY_REPORTS, null)

        return if (json != null) {
            try {
                gson.fromJson<List<ReportModel>>(json, type)
                    .sortedByDescending { it.id }
            } catch (e: Exception) {
                Log.e(TAG, "loadReports failed", e)
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun deleteReport(context: Context, report: ReportModel) {
        try {
            val prefs = getPrefs(context)
            val gson = Gson()
            val type = object : TypeToken<MutableList<ReportModel>>() {}.type
            val json = prefs.getString(KEY_REPORTS, null)

            if (json != null) {
                val reports: MutableList<ReportModel> = gson.fromJson(json, type)
                reports.removeAll { it.id == report.id }
                prefs.edit().putString(KEY_REPORTS, gson.toJson(reports)).apply()
                Log.d(TAG, "Report deleted: ${report.id}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteReport failed", e)
        }
    }

    fun clearAll(context: Context) {
        try {
            getPrefs(context).edit().remove(KEY_REPORTS).apply()
            Log.d(TAG, "All reports cleared")
        } catch (e: Exception) {
            Log.e(TAG, "clearAll failed", e)
        }
    }
}
