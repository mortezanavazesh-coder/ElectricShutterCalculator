package com.morteza.shuttercalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportAdapter(
    private var reports: List<ReportModel>,
    private val onItemClick: ((ReportModel) -> Unit)? = null
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textCustomer: TextView = itemView.findViewById(R.id.textCustomer)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val textTotal: TextView = itemView.findViewById(R.id.textTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.textCustomer.text = "مشتری: ${report.customerName}"
        holder.textDate.text = "تاریخ: ${report.date}"
        holder.textTotal.text = "جمع کل: ${report.total}"

        // کلیک روی آیتم
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(report)
        }
    }

    override fun getItemCount(): Int = reports.size

    // بروزرسانی لیست گزارش‌ها
    fun updateReports(newReports: List<ReportModel>) {
        reports = newReports
        notifyDataSetChanged()
    }
}
