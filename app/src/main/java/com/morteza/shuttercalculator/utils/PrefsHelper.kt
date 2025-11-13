package com.morteza.shuttercalculator.utils

import android.content.Context
import android.content.SharedPreferences

object PrefsHelper {
    private const val PREF_NAME = "shutter_prefs"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

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

    fun addOption(context: Context, category: String, title: String, value: Float) {
        val key = "${category}_$title"
        prefs(context).edit().putFloat(key, value).apply()
    }

    fun removeOption(context: Context, category: String, title: String) {
        val key = "${category}_$title"
        prefs(context).edit().remove(key).apply()
    }

    fun optionExists(context: Context, category: String, title: String): Boolean {
        val key = "${category}_$title"
        return prefs(context).contains(key)
    }

    fun getSortedOptionList(context: Context, category: String): List<String> {
        val all = prefs(context).all
        return all.keys.filter { it.startsWith("${category}_") }
            .map { it.removePrefix("${category}_") }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
    }
}
