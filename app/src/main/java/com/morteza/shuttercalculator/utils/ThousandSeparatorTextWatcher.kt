package com.morteza.shuttercalculator.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * TextWatcher برای فرمت هزارگان روی EditText
 * از WeakReference استفاده می‌کند تا نشت حافظه نداشته باشیم.
 * برای ذخیره همیشه متن را با FormatUtils.parseTomanInput بخوان.
 */
class ThousandSeparatorTextWatcher(editText: EditText) : TextWatcher {

    private val editRef: WeakReference<EditText> = WeakReference(editText)
    private var current = ""

    // جداکننده هزار با کاما (US) یا فارسی (IR)
    private val df = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        val edit = editRef.get() ?: return
        val str = s?.toString() ?: ""
        if (str == current) return

        edit.removeTextChangedListener(this)

        // حذف فاصله و جداکننده‌های قبلی
        val cleaned = str.replace("\\s".toRegex(), "")
            .replace(",", "")
            .replace("٬", "")

        if (cleaned.isEmpty()) {
            current = ""
            edit.setText("")
            edit.setSelection(0)
            edit.addTextChangedListener(this)
            return
        }

        try {
            val value = cleaned.toLong()
            val formatted = df.format(value)
            current = formatted
            edit.setText(formatted)
            edit.setSelection(formatted.length) // مکان‌نما آخر متن
        } catch (e: Exception) {
            current = cleaned
            edit.setText(cleaned)
            edit.setSelection(cleaned.length)
        }

        edit.addTextChangedListener(this)
    }
}
