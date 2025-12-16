package com.morteza.shuttercalculator.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object PrefsHelper {

    private const val PREFS_NAME = "shutter_prefs"
    private const val TAG = "PrefsHelper"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ------------------ Float helpers ------------------
    fun saveFloat(context: Context, key: String, value: Float) {
        try {
            getPrefs(context).edit().putFloat(key, value).apply()
        } catch (e: Exception) {
            Log.e(TAG, "saveFloat failed for $key", e)
        }
    }

    fun getFloat(context: Context, key: String, defaultValue: Float = 0f): Float {
        return try {
            getPrefs(context).getFloat(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "getFloat failed for $key", e)
            defaultValue
        }
    }

    fun putFloat(context: Context, key: String, value: Float) {
        saveFloat(context, key, value)
    }

    // ------------------ Long helpers ------------------
    fun saveLong(context: Context, key: String, value: Long) {
        try {
            getPrefs(context).edit().putLong(key, value).apply()
        } catch (e: Exception) {
            Log.e(TAG, "saveLong failed for $key", e)
        }
    }

    fun getLong(context: Context, key: String, defaultValue: Long = 0L): Long {
        return try {
            getPrefs(context).getLong(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "getLong failed for $key", e)
            defaultValue
        }
    }

    fun putLong(context: Context, key: String, value: Long) {
        saveLong(context, key, value)
    }

    // ------------------ Boolean helpers ------------------
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

    fun containsKey(context: Context, key: String): Boolean {
        return try {
            getPrefs(context).contains(key)
        } catch (e: Exception) {
            Log.e(TAG, "containsKey failed for $key", e)
            false
        }
    }

    fun removeKey(context: Context, key: String) {
        try {
            getPrefs(context).edit().remove(key).apply()
        } catch (e: Exception) {
            Log.e(TAG, "removeKey failed for $key", e)
        }
    }

    // ------------------ Option management (Float) ------------------
    fun addOption(context: Context, category: String, name: String, price: Float) {
        try {
            val key = "${category}_price_$name"
            getPrefs(context).edit().putFloat(key, price).apply()
        } catch (e: Exception) {
            Log.e(TAG, "addOption(Float) failed for $category/$name", e)
        }
    }

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

    // ------------------ Option management (Long) ------------------
    fun addOption(context: Context, category: String, name: String, price: Long) {
        try {
            val key = "${category}_price_$name"
            getPrefs(context).edit().putLong(key, price).apply()
        } catch (e: Exception) {
            Log.e(TAG, "addOption(Long) failed for $category/$name", e)
        }
    }

    fun getOptionMapLong(context: Context, category: String): Map<String, Long> {
        return try {
            val prefs = getPrefs(context)
            prefs.all.keys
                .filter { it.startsWith("${category}_price_") }
                .mapNotNull { key ->
                    try {
                        val name = key.removePrefix("${category}_price_")
                        val value = prefs.getLong(key, 0L)
                        name to value
                    } catch (e: Exception) {
                        Log.w(TAG, "getOptionMapLong skip key $key", e)
                        null
                    }
                }.toMap()
        } catch (e: Exception) {
            Log.e(TAG, "getOptionMapLong failed for $category", e)
            emptyMap()
        }
    }

    // ------------------ Options list helpers ------------------
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
            getOptionList(context, category).toMutableList().apply {
                sortWith(String.CASE_INSENSITIVE_ORDER)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getSortedOptionList failed for $category", e)
            mutableListOf()
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
            // تلاش برای خواندن Long؛ اگر نبود، از Float بخوان
            val valueLong = prefs.getLong(oldKey, Long.MIN_VALUE)
            val editor = prefs.edit()
            editor.remove(oldKey).apply()
            if (valueLong != Long.MIN_VALUE) {
                editor.putLong(newKey, valueLong).apply()
            } else {
                val valueFloat = prefs.getFloat(oldKey, 0f)
                editor.putFloat(newKey, valueFloat).apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "renameOption failed for $category $oldName -> $newName", e)
        }
    }

    // ------------------ Extras helpers ------------------
    fun getAllExtraOptions(context: Context): Map<String, Float> {
        val persian = getOptionMap(context, "اضافات")
        if (persian.isNotEmpty()) return persian
        val english = getOptionMap(context, "extra")
        return english
    }

    fun getAllExtraOptionsLong(context: Context): Map<String, Long> {
        val persian = getOptionMapLong(context, "اضافات")
        if (persian.isNotEmpty()) return persian
        val english = getOptionMapLong(context, "extra")
        return english
    }

    // جدید: گزینه‌های اضافی همراه با وضعیت فعال بودن (checkbox state)
    fun getAllExtraOptionsWithEnabled(context: Context): Map<String, Pair<Float, Boolean>> {
        val prefs = getPrefs(context)
        val extras = getAllExtraOptions(context)
        val result = mutableMapOf<String, Pair<Float, Boolean>>()
        for ((name, price) in extras) {
            val enabled = prefs.getBoolean("extra_enabled_$name", false)
            result[name] = price to enabled
        }
        return result
    }

    fun getCheckboxOptions(context: Context): Map<String, Float> = getAllExtraOptions(context)

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

    // ------------------ Slat (تیغه) specs ------------------
    fun saveSlatSpecs(context: Context, name: String, width: Float, thickness: Float) {
        getPrefs(context).edit()
            .putFloat("تیغه_${name}_width", width)
            .putFloat("تیغه_${name}_thickness", thickness)
            .apply()
    }

    fun getSlatWidth(context: Context, name: String): Float =
        getPrefs(context).getFloat("تیغه_${name}_width", 0f)

    fun getSlatThickness(context: Context, name: String): Float =
        getPrefs(context).getFloat("تیغه_${name}_thickness", 0f)

    // ------------------ Shaft (شفت) specs ------------------
    fun saveShaftSpecs(context: Context, name: String, diameter: Float) {
        getPrefs(context).edit()
            .putFloat("شفت_${name}_diameter", diameter)
            .apply()
    }

    fun getShaftDiameter(context: Context, name: String): Float =
        getPrefs(context).getFloat("شفت_${name}_diameter", 0f)
}
