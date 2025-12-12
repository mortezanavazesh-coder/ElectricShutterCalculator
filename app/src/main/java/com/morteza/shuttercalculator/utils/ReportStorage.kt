package com.morteza.shuttercalculator.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.morteza.shuttercalculator.ReportModel

object ReportStorage {

    private const val PREFS_NAME = "reports"
    private const val TAG = "ReportStorage"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun generateId(): Long = System.currentTimeMillis()

    fun saveReport(context: Context, report: ReportModel) {
        try {
            val prefs = getPrefs(context)
            val editor = prefs.edit()
            val key = "report_${report.id}"
            editor.putString(key, Gson().toJson(report))
            editor.apply()
            Log.d(TAG, "Report saved: ${report.id}")
        } catch (e: Exception) {
            Log.e(TAG, "saveReport failed", e)
        }
    }

    fun loadReports(context: Context): List<ReportModel> {
        val prefs = getPrefs(context)
        val gson = Gson()
        val list = mutableListOf<ReportModel>()
        for ((key, value) in prefs.all) {
            if (value is String && key.startsWith("report_")) {
                try {
                    val report = gson.fromJson(value, ReportModel::class.java)
                    list.add(report)
                } catch (e: Exception) {
                    Log.w(TAG, "skip invalid report $key", e)
                    // حذف گزارش خراب
                    prefs.edit().remove(key).apply()
                }
            }
        }
        return list.sortedByDescending { it.id }
    }

    fun deleteReport(context: Context, report: ReportModel) {
        try {
            val prefs = getPrefs(context)
            prefs.edit().remove("report_${report.id}").apply()
            Log.d(TAG, "Report deleted: ${report.id}")
        } catch (e: Exception) {
            Log.e(TAG, "deleteReport failed", e)
        }
    }

    fun clearAll(context: Context) {
        try {
            getPrefs(context).edit().clear().apply()
            Log.d(TAG, "All reports cleared")
        } catch (e: Exception) {
            Log.e(TAG, "clearAll failed", e)
        }
    }
}
