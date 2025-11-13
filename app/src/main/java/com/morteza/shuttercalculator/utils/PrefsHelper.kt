package com.morteza.shuttercalculator.utils

import android.content.Context
import android.content.SharedPreferences

object PrefsHelper {
    private const val PREF_NAME = "shutter_prefs"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // --- primitives ---
    fun saveFloat(context: Context, key: String, value: Float) {
        prefs(context).edit().putFloat(key, value).apply()
    }

    fun getFloat(context: Context, key: String, def: Float = 0f): Float =
        prefs(context).getFloat(key, def)

    fun saveBool(context: Context, key: String, value: Boolean) {
        prefs(context).edit().putBoolean(key, value).apply()
    }

    fun getBool(context: Context, key: String, def: Boolean = false): Boolean =
        prefs(context).getBoolean(key, def)

    // --- options API (category + title) ---
    private fun optionKey(category: String, title: String): String = "${category}_$title"

    fun addOption(context: Context, category: String, title: String, value: Float) {
        prefs(context).edit().putFloat(optionKey(category, title), value).apply()
    }

    fun removeOption(context: Context, category: String, title: String) {
        prefs(context).edit().remove(optionKey(category, title)).apply()
    }

    fun optionExists(context: Context, category: String, title: String): Boolean {
        return prefs(context).contains(optionKey(category, title))
    }

    fun getSortedOptionList(context: Context, category: String): List<String> {
        val prefix = "${category}_"
        return prefs(context).all.keys
            .asSequence()
            .filter { it.startsWith(prefix) }
            .map { it.removePrefix(prefix) }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
            .toList()
    }

    // برگرداندن همهٔ گزینه‌ها با مقدارشان برای یک کتگوری (مثلاً extras)
    fun getAllExtraOptions(context: Context, category: String): Map<String, Float> {
        val prefix = "${category}_"
        val out = mutableMapOf<String, Float>()
        for ((key, value) in prefs(context).all) {
            if (key.startsWith(prefix) && value is Float) {
                val title = key.removePrefix(prefix)
                out[title] = value
            }
        }
        return out
    }

    // تغییر نام گزینه (برای BasePriceActivity)
    fun renameOption(context: Context, category: String, oldTitle: String, newTitle: String): Boolean {
        if (oldTitle == newTitle) return true
        val oldKey = optionKey(category, oldTitle)
        val newKey = optionKey(category, newTitle)

        if (!prefs(context).contains(oldKey)) return false
        if (prefs(context).contains(newKey)) return false

        val value = prefs(context).getFloat(oldKey, 0f)
        val ed = prefs(context).edit()
        ed.remove(oldKey)
        ed.putFloat(newKey, value)

        // انتقال متادیتا زمان (در صورت ذخیره شده)
        val metaOld = metaKey(category, oldTitle, "timestamp")
        val metaNew = metaKey(category, newTitle, "timestamp")
        if (prefs(context).contains(metaOld)) {
            val ts = prefs(context).getLong(metaOld, 0L)
            ed.remove(metaOld)
            ed.putLong(metaNew, ts)
        }
        ed.apply()
        return true
    }

    // --- metadata helpers (اختیاری برای زمان ذخیره) ---
    private fun metaKey(category: String, title: String, metaName: String): String =
        "${category}_${title}__meta_$metaName"

    fun saveMetaTimestamp(context: Context, category: String, title: String, ts: Long) {
        prefs(context).edit().putLong(metaKey(category, title, "timestamp"), ts).apply()
    }

    fun getMetaTimestamp(context: Context, category: String, title: String, def: Long = 0L): Long {
        return prefs(context).getLong(metaKey(category, title, "timestamp"), def)
    }
}
