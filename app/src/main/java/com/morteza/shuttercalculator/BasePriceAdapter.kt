package com.morteza.shuttercalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.morteza.shuttercalculator.utils.FormatUtils

class BasePriceAdapter(
    private var items: List<Pair<String, Long>>,
    private val onDelete: (title: String) -> Unit,
    private val onRename: (title: String) -> Unit,
    private val onEdit: (title: String) -> Unit
) : RecyclerView.Adapter<BasePriceAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvItemTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvItemPrice)
        val btnRename: ImageButton = itemView.findViewById(R.id.btnRenameItem)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditItem)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_base_price, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (title, price) = items[position]

        // نمایش نام گزینه
        holder.tvTitle.text = title

        // ✅ نمایش قیمت با فرمت تومان و جداکننده هزارگان (Long)
        holder.tvPrice.text = FormatUtils.formatToman(price)

        // رویدادها
        holder.btnRename.setOnClickListener { onRename(title) }
        holder.btnEdit.setOnClickListener { onEdit(title) }
        holder.btnDelete.setOnClickListener { onDelete(title) }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Pair<String, Long>>) {
        items = newItems
        notifyDataSetChanged()
    }
}
