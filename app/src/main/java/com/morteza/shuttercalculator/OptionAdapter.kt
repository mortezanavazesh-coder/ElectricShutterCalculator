package com.morteza.shuttercalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OptionAdapter(
    private var options: List<Pair<String, Float>>,
    private val onEdit: (String) -> Unit,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<OptionAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.textOptionName)
        val tvPrice: TextView = itemView.findViewById(R.id.textOptionPrice)
        val btnEdit: ImageButton = itemView.findViewById(R.id.buttonEditOption)
        val btnDelete: ImageButton = itemView.findViewById(R.id.buttonDeleteOption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_option, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (name, price) = options[position]
        holder.tvName.text = name
        holder.tvPrice.text = price.toString()

        holder.btnEdit.setOnClickListener { onEdit(name) }
        holder.btnDelete.setOnClickListener { onDelete(name) }
    }

    override fun getItemCount(): Int = options.size

    fun update(newOptions: List<Pair<String, Float>>) {
        options = newOptions
        notifyDataSetChanged()
    }
}
