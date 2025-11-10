package com.morteza.shuttercalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.morteza.shuttercalculator.utils.FormatUtils

data class SelectedItem(
    val name: String,
    var count: Int,
    var unitPrice: Float
)

class SelectedItemsAdapter(
    private val items: MutableList<SelectedItem>,
    private val onEdit: (pos: Int) -> Unit,
    private val onDelete: (pos: Int) -> Unit
) : RecyclerView.Adapter<SelectedItemsAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.selName)
        val textCount: TextView = view.findViewById(R.id.selCount)
        val textUnit: TextView = view.findViewById(R.id.selUnit)
        val textTotal: TextView = view.findViewById(R.id.selTotal)
        val btnEdit: ImageButton = view.findViewById(R.id.selEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.selDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_selected, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val it = items[position]
        holder.textName.text = it.name
        holder.textCount.text = it.count.toString()
        holder.textUnit.text = FormatUtils.formatToman(it.unitPrice)
        holder.textTotal.text = FormatUtils.formatToman((it.count * it.unitPrice))
        holder.btnEdit.setOnClickListener { onEdit(position) }
        holder.btnDelete.setOnClickListener { onDelete(position) }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<SelectedItem>) {
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
