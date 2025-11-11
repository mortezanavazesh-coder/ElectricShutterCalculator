package com.morteza.shuttercalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportAdapter(
    private val items: MutableList<ReportEntity>,
    private val onDelete: (ReportEntity) -> Unit,
    private val onItemClick: (ReportEntity) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    private val dateFmt = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    inner class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textCustomerName)
        val date: TextView = view.findViewById(R.id.textReportDate)
        val dimensions: TextView = view.findViewById(R.id.textDimensions)
        val breakdownPreview: TextView = view.findViewById(R.id.textBreakdownPreview)
        val total: TextView = view.findViewById(R.id.textTotalPrice)
        val deleteBtn: Button = view.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(v)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.customerName
        holder.date.text = formatDate(item.createdAt)
        holder.dimensions.text = "${formatDecimal(item.heightCm)} x ${formatDecimal(item.widthCm)} cm"
        holder.breakdownPreview.text = trimBreakdown(item.breakdown)
        holder.total.text = formatToman(item.totalPriceToman)
        holder.deleteBtn.setOnClickListener { onDelete(item) }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun setAll(list: List<ReportEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    private fun formatDate(ts: Long): String {
        return try {
            dateFmt.format(Date(ts))
        } catch (e: Exception) {
            ""
        }
    }

    private fun trimBreakdown(s: String?): String {
        if (s.isNullOrBlank()) return ""
        // show first 200 chars cleanly
        val trimmed = s.trim()
        return if (trimmed.length <= 200) trimmed else trimmed.substring(0, 197).trimEnd() + "..."
    }

    private fun formatDecimal(v: Double): String {
        val l = if (v == v.toLong().toDouble()) String.format(Locale.getDefault(), "%d", v.toLong())
                else String.format(Locale.getDefault(), "%.2f", v)
        return l
    }

    private fun formatToman(value: Long): String {
        return String.format("%,d تومان", value)
    }
}
