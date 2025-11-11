package com.morteza.shuttercalculator.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * ساده و آگاه از چرخه حیات ویو: از WeakReference استفاده می‌کند تا نشت حافظه نداشته باشیم.
 * فرمت‌شدن هزارگان برای نمایش؛ هنگام ذخیره همیشه متن را با FormatUtils.parseTomanInput بخوان.
 */
class ThousandSeparatorTextWatcher(editText: EditText) : TextWatcher {

    private val editRef: WeakReference<EditText> = WeakReference(editText)
    private var current = ""

    private val df = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        val edit = editRef.get() ?: return
        val str = s?.toString() ?: ""
        if (str == current) return

        // remove listener to avoid recursion
        edit.removeTextChangedListener(this)

        // clean input (remove non-digits)
        val cleaned = str.replace("\\s".toRegex(), "")
            .replace(",", "")
            .replace("٬", "")
            .replace(".", "")

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
            // move cursor to end
            edit.setSelection(formatted.length)
        } catch (e: Exception) {
            // fallback: set raw
            current = cleaned
            edit.setText(cleaned)
            edit.setSelection(cleaned.length)
        }

        edit.addTextChangedListener(this)
    }
}
