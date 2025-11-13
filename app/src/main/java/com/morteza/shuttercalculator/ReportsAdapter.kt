package com.morteza.shuttercalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportsAdapter(
    private var items: List<String>,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<ReportsAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvMeta: TextView = view.findViewById(R.id.tvMeta)
        private val btnDelete: Button = view.findViewById(R.id.btnDelete)

        fun bind(title: String) {
            tvTitle.text = title
            tvMeta.text = "" // میتوانی متادیتا (مثلاً تاریخ) را از PrefsHelper.getFloat بخوانی و اینجا نمایش دهی
            btnDelete.setOnClickListener { onDelete(title) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size

    fun update(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }
}
