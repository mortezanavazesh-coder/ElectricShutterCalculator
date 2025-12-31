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
        try { getPrefs(context).edit().putFloat(key, value).apply() }
        catch (e: Exception) { Log.e(TAG, "saveFloat failed for $key", e) }
    }

    fun getFloat(context: Context, key: String, defaultValue: Float = 0f): Float =
        try { getPrefs(context).getFloat(key, defaultValue) }
        catch (e: Exception) { Log.e(TAG, "getFloat failed for $key", e); defaultValue }

    // ------------------ Long helpers ------------------
    fun saveLong(context: Context, key: String, value: Long) {
        try { getPrefs(context).edit().putLong(key, value).apply() }
        catch (e: Exception) { Log.e(TAG, "saveLong failed for $key", e) }
    }

    fun getLong(context: Context, key: String, defaultValue: Long = 0L): Long =
        try { getPrefs(context).getLong(key, defaultValue) }
        catch (e: Exception) { Log.e(TAG, "getLong failed for $key", e); defaultValue }

    // Wrapper برای سازگاری با کدهای قدیمی
    fun putLong(context: Context, key: String, value: Long) = saveLong(context, key, value)

    // ------------------ Boolean helpers ------------------
    fun saveBool(context: Context, key: String, value: Boolean) {
        try { getPrefs(context).edit().putBoolean(key, value).apply() }
        catch (e: Exception) { Log.e(TAG, "saveBool failed for $key", e) }
    }

    fun getBool(context: Context, key: String): Boolean =
        try { getPrefs(context).getBoolean(key, false) }
        catch (e: Exception) { Log.e(TAG, "getBool failed for $key", e); false }

    // ------------------ Key helpers ------------------
    fun containsKey(context: Context, key: String): Boolean =
        try { getPrefs(context).contains(key) }
        catch (e: Exception) { Log.e(TAG, "containsKey failed for $key", e); false }

    fun removeKey(context: Context, key: String) {
        try { getPrefs(context).edit().remove(key).apply() }
        catch (e: Exception) { Log.e(TAG, "removeKey failed for $key", e) }
    }

    // ------------------ Option management ------------------
    fun addOption(context: Context, category: String, name: String, price: Float) {
        val key = "${category}_price_$name"
        saveFloat(context, key, price)
    }

    fun addOption(context: Context, category: String, name: String, price: Long) {
        val key = "${category}_price_$name"
        saveLong(context, key, price)
    }

    // مقاوم‌سازی برای جلوگیری از ClassCastException
    fun getOptionMap(context: Context, category: String): Map<String, Float> {
        val prefs = getPrefs(context)
        val result = mutableMapOf<String, Float>()
        for (key in prefs.all.keys) {
            if (key.startsWith("${category}_price_")) {
                val name = key.removePrefix("${category}_price_")
                val raw = prefs.all[key]
                val value = when (raw) {
                    is Float -> raw
                    is Long -> raw.toFloat()
                    is Int -> raw.toFloat()
                    else -> 0f
                }
                result[name] = value
            }
        }
        return result
    }

    fun getOptionMapLong(context: Context, category: String): Map<String, Long> {
        val prefs = getPrefs(context)
        val result = mutableMapOf<String, Long>()
        for (key in prefs.all.keys) {
            if (key.startsWith("${category}_price_")) {
                val name = key.removePrefix("${category}_price_")
                val raw = prefs.all[key]
                val value = when (raw) {
                    is Long -> raw
                    is Float -> raw.toLong()
                    is Int -> raw.toLong()
                    else -> 0L
                }
                result[name] = value
            }
        }
        return result
    }

    fun getOptionList(context: Context, category: String): List<String> =
        getPrefs(context).all.keys
            .filter { it.startsWith("${category}_price_") }
            .map { it.removePrefix("${category}_price_") }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)

    fun getSortedOptionList(context: Context, category: String): MutableList<String> =
        getOptionList(context, category).toMutableList().apply {
            sortWith(String.CASE_INSENSITIVE_ORDER)
        }

    fun optionExists(context: Context, category: String, name: String): Boolean =
        getOptionList(context, category).any { it.equals(name, ignoreCase = true) }

    fun removeOption(context: Context, category: String, name: String) {
        getPrefs(context).edit().remove("${category}_price_$name").apply()
    }

    fun renameOption(context: Context, category: String, oldName: String, newName: String) {
        val prefs = getPrefs(context)
        val oldKey = "${category}_price_$oldName"
        val newKey = "${category}_price_$newName"
        if (!prefs.contains(oldKey)) return
        val editor = prefs.edit()
        val raw = prefs.all[oldKey]
        editor.remove(oldKey).apply()
        when (raw) {
            is Long -> editor.putLong(newKey, raw).apply()
            is Float -> editor.putFloat(newKey, raw).apply()
            is Int -> editor.putLong(newKey, raw.toLong()).apply()
        }
    }

    // ------------------ Extras helpers ------------------
    fun getAllExtraOptions(context: Context): Map<String, Float> {
        val persian = getOptionMap(context, "اضافات")
        if (persian.isNotEmpty()) return persian
        return getOptionMap(context, "extra")
    }

    fun getAllExtraOptionsLong(context: Context): Map<String, Long> {
        val persian = getOptionMapLong(context, "اضافات")
        if (persian.isNotEmpty()) return persian
        return getOptionMapLong(context, "extra")
    }

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

    // ------------------ Compatibility / Convenience helpers ------------------

    data class SlatSpecs(val width: Float, val thickness: Float)
    data class ShaftSpecs(val diameter: Float)

    fun getSlatSpecs(context: Context, name: String): SlatSpecs? {
        val prefs = getPrefs(context)
        val keyWidth = "تیغه_${name}_width"
        val keyThickness = "تیغه_${name}_thickness"
        if (!prefs.contains(keyWidth) && !prefs.contains(keyThickness)) return null
        val width = try { prefs.getFloat(keyWidth, 0f) } catch (e: Exception) { Log.e(TAG, "getSlatSpecs width failed for $name", e); 0f }
        val thickness = try { prefs.getFloat(keyThickness, 0f) } catch (e: Exception) { Log.e(TAG, "getSlatSpecs thickness failed for $name", e); 0f }
        return SlatSpecs(width, thickness)
    }

    fun removeSlatSpecs(context: Context, name: String) {
        try {
            getPrefs(context).edit()
                .remove("تیغه_${name}_width")
                .remove("تیغه_${name}_thickness")
                .apply()
        } catch (e: Exception) { Log.e(TAG, "removeSlatSpecs failed for $name", e) }
    }

    fun getShaftSpecs(context: Context, name: String): ShaftSpecs? {
        val prefs = getPrefs(context)
        val key = "شفت_${name}_diameter"
        if (!prefs.contains(key)) return null
        val diameter = try { prefs.getFloat(key, 0f) } catch (e: Exception) { Log.e(TAG, "getShaftSpecs failed for $name", e); 0f }
        return ShaftSpecs(diameter)
    }

    fun removeShaftSpecs(context: Context, name: String) {
        try {
            getPrefs(context).edit()
                .remove("شفت_${name}_diameter")
                .apply()
        } catch (e: Exception) { Log.e(TAG, "removeShaftSpecs failed for $name", e) }
    }

    // Aliases to match other code expectations
    fun putSlatSpecs(context: Context, name: String, width: Float, thickness: Float) =
        saveSlatSpecs(context, name, width, thickness)

    fun putShaftSpecs(context: Context, name: String, diameter: Float) =
        saveShaftSpecs(context, name, diameter)
}
