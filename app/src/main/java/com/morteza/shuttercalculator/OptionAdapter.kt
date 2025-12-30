package com.morteza.shuttercalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.morteza.shuttercalculator.utils.FormatUtils

class OptionAdapter(
    private var options: List<Pair<String, Float>>,
    private val onRename: (String) -> Unit,
    private val onEditPrice: (String) -> Unit,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<OptionAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.textOptionName)
        val tvPrice: TextView = itemView.findViewById(R.id.textOptionPrice)
        val btnRename: ImageButton = itemView.findViewById(R.id.buttonRenameOption)
        val btnEditPrice: ImageButton = itemView.findViewById(R.id.buttonEditPriceOption)
        val btnDelete: ImageButton = itemView.findViewById(R.id.buttonDeleteOption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_option, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (name, price) = options[position]

        // نمایش نام
        holder.tvName.text = name

        // ✅ نمایش قیمت با فرمت تومان
        holder.tvPrice.text = FormatUtils.formatToman(price)

        // رویدادها
        holder.btnRename.setOnClickListener { onRename(name) }
        holder.btnEditPrice.setOnClickListener { onEditPrice(name) }
        holder.btnDelete.setOnClickListener { onDelete(name) }
    }

    override fun getItemCount(): Int = options.size

    fun update(newOptions: List<Pair<String, Float>>) {
        options = newOptions
        notifyDataSetChanged()
    }
}
