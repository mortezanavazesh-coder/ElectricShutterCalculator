package com.morteza.shuttercalculator

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.BaseAdapter
import com.morteza.shuttercalculator.utils.FormatUtils
import com.morteza.shuttercalculator.utils.PrefsHelper

class OptionAdapter(
    private val context: Context,
    private val category: String,
    private val items: MutableList<String>,
    private val onEdit: (String) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): String = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_option, parent, false)
        try {
            val name = items[position]
            val price = PrefsHelper.getFloat(context, "${category}_price_$name")

            val textName = view.findViewById<TextView>(R.id.textOptionName)
            val buttonEdit = view.findViewById<Button>(R.id.buttonEditOption)
            val buttonDelete = view.findViewById<Button>(R.id.buttonDeleteOption)

            textName.text = "$name - ${FormatUtils.formatToman(price)}"

            buttonEdit.setOnClickListener {
                buttonEdit.isEnabled = false
                try {
                    onEdit(name)
                } catch (e: Exception) {
                    Log.e("OptionAdapter", "onEdit failed for $name", e)
                } finally {
                    buttonEdit.postDelayed({ buttonEdit.isEnabled = true }, 350)
                }
            }

            buttonDelete.setOnClickListener {
                buttonDelete.isEnabled = false
                try {
                    PrefsHelper.removeOption(context, category, name)
                    val idx = items.indexOf(name)
                    if (idx >= 0) {
                        items.removeAt(idx)
                        // notify on UI thread if possible
                        (context as? android.app.Activity)?.runOnUiThread {
                            notifyDataSetChanged()
                        } ?: notifyDataSetChanged()
                    } else {
                        // item not found: still notify to keep UI consistent
                        notifyDataSetChanged()
                    }
                } catch (e: Exception) {
                    Log.e("OptionAdapter", "delete failed for $name", e)
                } finally {
                    buttonDelete.postDelayed({ buttonDelete.isEnabled = true }, 350)
                }
            }
        } catch (e: Exception) {
            Log.e("OptionAdapter", "getView inner exception", e)
        }
        return view
    }

    // برای رفرش از بیرون
    fun updateItems(newItems: MutableList<String>) {
        items.clear()
        items.addAll(newItems)
        (context as? android.app.Activity)?.runOnUiThread {
            notifyDataSetChanged()
        } ?: notifyDataSetChanged()
    }
}
