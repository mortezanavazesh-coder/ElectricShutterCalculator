package com.morteza.shuttercalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.morteza.shuttercalculator.utils.FormatUtils
import com.morteza.shuttercalculator.utils.PrefsHelper

class OptionRecyclerAdapter(
    private val category: String,
    private var items: MutableList<String>,
    private val onEdit: (name: String) -> Unit,
    private val onDeleteRequest: (name: String, onConfirmed: () -> Unit) -> Unit
) : RecyclerView.Adapter<OptionRecyclerAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.textOptionName)
        val buttonEdit: ImageButton = view.findViewById(R.id.buttonEditOption)
        val buttonDelete: ImageButton = view.findViewById(R.id.buttonDeleteOption)
        val textPrice: TextView = view.findViewById(R.id.textOptionPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_option, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val name = items[position]
        holder.textName.text = name
        val price = PrefsHelper.getFloat(holder.itemView.context, "${category}_price_$name")
        holder.textPrice.text = FormatUtils.formatToman(price)

        holder.buttonEdit.setOnClickListener {
            onEdit(name)
        }

        holder.buttonDelete.setOnClickListener {
            onDeleteRequest(name) {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    removeAt(pos)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeAt(pos: Int) {
        if (pos in 0 until items.size) {
            items.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }
}
