package com.morteza.shuttercalculator.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object PrefsHelper {

    private const val PREFS_NAME = "shutter_prefs"
    private const val TAG = "PrefsHelper"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Basic float helpers
    fun saveFloat(context: Context, key: String, value: Float) {
        try {
            getPrefs(context).edit().putFloat(key, value).apply()
        } catch (e: Exception) {
            Log.e(TAG, "saveFloat failed for $key", e)
        }
    }

    fun getFloat(context: Context, key: String): Float {
        return try {
            getPrefs(context).getFloat(key, 0f)
        } catch (e: Exception) {
            Log.e(TAG, "getFloat failed for $key", e)
            0f
        }
    }

    // Boolean helpers
    fun saveBool(context: Context, key: String, value: Boolean) {
        try {
            getPrefs(context).edit().putBoolean(key, value).apply()
        } catch (e: Exception) {
            Log.e(TAG, "saveBool failed for $key", e)
        }
    }

    fun getBool(context: Context, key: String): Boolean {
        return try {
            getPrefs(context).getBoolean(key, false)
        } catch (e: Exception) {
            Log.e(TAG, "getBool failed for $key", e)
            false
        }
    }

    // Option management (options stored as "<category>_price_<name>" -> float)
    fun addOption(context: Context, category: String, name: String, price: Float) {
        try {
            val key = "${category}_price_$name"
            getPrefs(context).edit().putFloat(key, price).apply()
        } catch (e: Exception) {
            Log.e(TAG, "addOption failed for $category/$name", e)
        }
    }

    fun removeOption(context: Context, category: String, name: String) {
        try {
            getPrefs(context).edit().remove("${category}_price_$name").apply()
        } catch (e: Exception) {
            Log.e(TAG, "removeOption failed for $category/$name", e)
        }
    }

    fun renameOption(context: Context, category: String, oldName: String, newName: String) {
        try {
            val prefs = getPrefs(context)
            val oldKey = "${category}_price_$oldName"
            val newKey = "${category}_price_$newName"
            if (!prefs.contains(oldKey)) {
                Log.w(TAG, "renameOption: old key not found $oldKey")
                return
            }
            val value = prefs.getFloat(oldKey, 0f)
            prefs.edit().remove(oldKey).putFloat(newKey, value).apply()
        } catch (e: Exception) {
            Log.e(TAG, "renameOption failed for $category $oldName -> $newName", e)
        }
    }

    fun optionExists(context: Context, category: String, name: String): Boolean {
        return try {
            getOptionList(context, category).any { it.equals(name, ignoreCase = true) }
        } catch (e: Exception) {
            Log.e(TAG, "optionExists failed for $category/$name", e)
            false
        }
    }

    fun getOptionList(context: Context, category: String): List<String> {
        return try {
            getPrefs(context).all.keys
                .filter { it.startsWith("${category}_price_") }
                .map { it.removePrefix("${category}_price_") }
                .sortedWith(String.CASE_INSENSITIVE_ORDER)
        } catch (e: Exception) {
            Log.e(TAG, "getOptionList failed for $category", e)
            emptyList()
        }
    }

    fun getSortedOptionList(context: Context, category: String): MutableList<String> {
        return try {
            getOptionList(context, category).toMutableList().apply { sortWith(String.CASE_INSENSITIVE_ORDER) }
        } catch (e: Exception) {
            Log.e(TAG, "getSortedOptionList failed for $category", e)
            mutableListOf()
        }
    }

    // Return map of all options for a given category: name -> price
    fun getOptionMap(context: Context, category: String): Map<String, Float> {
        return try {
            val prefs = getPrefs(context)
            prefs.all.keys
                .filter { it.startsWith("${category}_price_") }
                .mapNotNull { key ->
                    try {
                        val name = key.removePrefix("${category}_price_")
                        val value = prefs.getFloat(key, 0f)
                        name to value
                    } catch (e: Exception) {
                        Log.w(TAG, "getOptionMap skip key $key", e)
                        null
                    }
                }.toMap()
        } catch (e: Exception) {
            Log.e(TAG, "getOptionMap failed for $category", e)
            emptyMap()
        }
    }

    // Extras specific helpers
    // Assumes extras are stored with category name "اضافی" (Persian) or "extra" (fallback).
    // This method returns a Map<String, Float> of extras.
    fun getAllExtraOptions(context: Context): Map<String, Float> {
        // try Persian category first then "extra"
        val persian = getOptionMap(context, "اضافی")
        if (persian.isNotEmpty()) return persian
        val english = getOptionMap(context, "extra")
        return english
    }

    // Alias for code that expected getCheckboxOptions
    fun getCheckboxOptions(context: Context): Map<String, Float> = getAllExtraOptions(context)

    // Convenience: clear all options of a category (useful for tests)
    fun clearCategory(context: Context, category: String) {
        try {
            val prefs = getPrefs(context)
            val editor = prefs.edit()
            prefs.all.keys
                .filter { it.startsWith("${category}_price_") }
                .forEach { editor.remove(it) }
            editor.apply()
        } catch (e: Exception) {
            Log.e(TAG, "clearCategory failed for $category", e)
        }
    }
}
