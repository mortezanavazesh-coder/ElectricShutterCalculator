package com.morteza.shuttercalculator.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object PrefsHelper {

    private const val PREFS_NAME = "shutter_prefs"
    private const val TAG = "PrefsHelper"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- Key helpers (encode/decode names to be safe as keys) ---
    private fun encodeName(name: String): String =
        try {
            URLEncoder.encode(name, StandardCharsets.UTF_8.name())
        } catch (e: Exception) {
            Log.w(TAG, "encodeName fallback for: $name", e)
            name.replace("\\s+".toRegex(), "_")
        }

    private fun decodeName(encoded: String): String =
        try {
            URLDecoder.decode(encoded, StandardCharsets.UTF_8.name())
        } catch (e: Exception) {
            Log.w(TAG, "decodeName fallback for: $encoded", e)
            encoded.replace("_", " ")
        }

    private fun optionKey(category: String, name: String): String =
        "${category}_price_${encodeName(name)}"

    private fun metaKey(category: String, name: String, meta: String = "meta"): String =
        "${category}_${meta}_${encodeName(name)}"

    // --- Basic float helpers ---
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

    // --- Boolean helpers ---
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

    // --- Option management (option stored as "<category>_price_<encodedName>" -> float) ---
    // addOption: overloads to support price optional (for reports we use 0f)
    fun addOption(context: Context, category: String, name: String) {
        addOption(context, category, name, 0f)
    }

    fun addOption(context: Context, category: String, name: String, price: Float) {
        try {
            val key = optionKey(category, name)
            getPrefs(context).edit().putFloat(key, price).apply()
        } catch (e: Exception) {
            Log.e(TAG, "addOption failed for $category/$name", e)
        }
    }

    fun removeOption(context: Context, category: String, name: String) {
        try {
            val prefs = getPrefs(context)
            val key = optionKey(category, name)
            if (prefs.contains(key)) {
                prefs.edit().remove(key).apply()
            } else {
                // attempt for unencoded legacy keys (backwards compatibility)
                val legacyKey = "${category}_price_$name"
                if (prefs.contains(legacyKey)) prefs.edit().remove(legacyKey).apply()
            }
            // remove meta if any
            removeMetaIfExists(context, category, name)
        } catch (e: Exception) {
            Log.e(TAG, "removeOption failed for $category/$name", e)
        }
    }

    fun renameOption(context: Context, category: String, oldName: String, newName: String) {
        try {
            val prefs = getPrefs(context)
            val oldKey = optionKey(category, oldName)
            val newKey = optionKey(category, newName)

            val editor = prefs.edit()
            if (prefs.contains(oldKey)) {
                val value = prefs.getFloat(oldKey, 0f)
                editor.remove(oldKey).putFloat(newKey, value).apply()
            } else {
                // fallback to legacy key if present
                val legacyOld = "${category}_price_$oldName"
                if (prefs.contains(legacyOld)) {
                    val value = prefs.getFloat(legacyOld, 0f)
                    editor.remove(legacyOld).putFloat(newKey, value).apply()
                } else {
                    Log.w(TAG, "renameOption: old key not found for $oldName")
                    return
                }
            }

            // migrate meta if exists
            val oldMetaKey = metaKey(category, oldName)
            val newMetaKey = metaKey(category, newName)
            if (prefs.contains(oldMetaKey)) {
                val metaVal = prefs.getFloat(oldMetaKey, 0f)
                prefs.edit().remove(oldMetaKey).putFloat(newMetaKey, metaVal).apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "renameOption failed for $category $oldName -> $newName", e)
        }
    }

    fun optionExists(context: Context, category: String, name: String): Boolean {
        return try {
            val key = optionKey(category, name)
            val prefs = getPrefs(context)
            if (prefs.contains(key)) return true
            // case-insensitive check across all keys (for backward compatibility)
            val nameLower = name.lowercase()
            prefs.all.keys.any { it.startsWith("${category}_price_") && decodeName(it.removePrefix("${category}_price_")).lowercase() == nameLower }
        } catch (e: Exception) {
            Log.e(TAG, "optionExists failed for $category/$name", e)
            false
        }
    }

    fun getOptionList(context: Context, category: String): List<String> {
        return try {
            val prefs = getPrefs(context)
            prefs.all.keys
                .filter { it.startsWith("${category}_price_") }
                .map { key ->
                    val encoded = key.removePrefix("${category}_price_")
                    decodeName(encoded)
                }
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

    fun getOptionMap(context: Context, category: String): Map<String, Float> {
        return try {
            val prefs = getPrefs(context)
            prefs.all.keys
                .filter { it.startsWith("${category}_price_") }
                .mapNotNull { key ->
                    try {
                        val encoded = key.removePrefix("${category}_price_")
                        val name = decodeName(encoded)
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

    // --- Extras specific helpers (compatibility) ---
    fun getAllExtraOptions(context: Context): Map<String, Float> {
        val persian = getOptionMap(context, "اضافی")
        if (persian.isNotEmpty()) return persian
        return getOptionMap(context, "extra")
    }

    fun getCheckboxOptions(context: Context): Map<String, Float> = getAllExtraOptions(context)

    // --- Meta helpers (timestamp or other small numeric meta) ---
    fun saveMetaTimestamp(context: Context, category: String, name: String, timestampMillis: Long) {
        try {
            saveFloat(context, metaKey(category, name), timestampMillis.toFloat())
        } catch (e: Exception) {
            Log.e(TAG, "saveMetaTimestamp failed for $category/$name", e)
        }
    }

    fun getMetaTimestamp(context: Context, category: String, name: String): Long {
        return try {
            getFloat(context, metaKey(category, name)).toLong()
        } catch (e: Exception) {
            Log.e(TAG, "getMetaTimestamp failed for $category/$name", e)
            0L
        }
    }

    private fun removeMetaIfExists(context: Context, category: String, name: String) {
        try {
            val prefs = getPrefs(context)
            val mKey = metaKey(category, name)
            if (prefs.contains(mKey)) prefs.edit().remove(mKey).apply()
            // also try legacy meta key patterns
            val legacy = "${category}_meta_$name"
            if (prefs.contains(legacy)) prefs.edit().remove(legacy).apply()
        } catch (e: Exception) {
            Log.e(TAG, "removeMetaIfExists failed for $category/$name", e)
        }
    }

    // Convenience: clear all options of a category (useful for tests)
    fun clearCategory(context: Context, category: String) {
        try {
            val prefs = getPrefs(context)
            val editor = prefs.edit()
            prefs.all.keys
                .filter { it.startsWith("${category}_price_") || it.startsWith("${category}_meta_") }
                .forEach { editor.remove(it) }
            editor.apply()
        } catch (e: Exception) {
            Log.e(TAG, "clearCategory failed for $category", e)
        }
    }
}
