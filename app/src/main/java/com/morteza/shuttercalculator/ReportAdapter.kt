package com.morteza.shuttercalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class ReportAdapter(
    private val items: MutableList<ReportEntity>,
    private val onDelete: (ReportEntity) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textCustomerName)
        val dimensions: TextView = view.findViewById(R.id.textDimensions)
        val breakdown: TextView = view.findViewById(R.id.textBreakdown)
        val total: TextView = view.findViewById(R.id.textTotalPrice)
        val delete: Button = view.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(v)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.customerName
        holder.dimensions.text = "ابعاد: ${item.heightCm} x ${item.widthCm} cm"
        holder.breakdown.text = item.breakdown
        holder.total.text = "قیمت نهایی: ${formatToman(item.totalPriceToman)}"
        holder.delete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = items.size

    fun setAll(list: List<ReportEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    private fun formatToman(v: Long): String {
        val nf = NumberFormat.getInstance(Locale("fa"))
        return "${nf.format(v)} تومان"
    }
}
