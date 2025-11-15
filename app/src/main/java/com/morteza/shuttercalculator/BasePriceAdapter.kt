package com.morteza.shuttercalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.morteza.shuttercalculator.utils.FormatUtils

class BasePriceAdapter(
    private var items: List<Pair<String, Float>>,
    private val onDelete: (String) -> Unit,
    private val onRename: (String) -> Unit,
    private val onEdit: (String) -> Unit
) : RecyclerView.Adapter<BasePriceAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvPrice: TextView = view.findViewById(R.id.tvItemPrice)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteItem)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditItem)
        val btnRename: ImageButton = view.findViewById(R.id.btnRenameItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_base_price, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (title, price) = items[position]
        holder.tvTitle.text = title
        holder.tvPrice.text = FormatUtils.formatToman(price)
        holder.btnDelete.setOnClickListener { onDelete(title) }
        holder.btnEdit.setOnClickListener { onEdit(title) }
        holder.btnRename.setOnClickListener { onRename(title) }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Pair<String, Float>>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
