package com.morteza.shuttercalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportsAdapter(
    private var items: List<String>,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<ReportsAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvReportTitle)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteReport)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val title = items[position]
        holder.tvTitle.text = title
        holder.btnDelete.setOnClickListener { onDelete(title) }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<String>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
