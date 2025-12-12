package com.morteza.shuttercalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportAdapter(
    private var reports: MutableList<ReportModel>,
    private val onDeleteClick: ((ReportModel) -> Unit)? = null,
    private val onItemClick: ((ReportModel) -> Unit)? = null
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTitle: TextView = itemView.findViewById(R.id.textReportTitle)
        val textDate: TextView = itemView.findViewById(R.id.textReportDate)
        val textPrice: TextView = itemView.findViewById(R.id.textReportPrice)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDeleteReport)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.textTitle.text = report.customerName
        holder.textDate.text = "تاریخ: ${report.date}"
        holder.textPrice.text = "جمع کل: ${report.total}"

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(report)
        }

        holder.buttonDelete.setOnClickListener {
            onDeleteClick?.invoke(report)
        }
    }

    override fun getItemCount(): Int = reports.size

    fun updateReports(newReports: List<ReportModel>) {
        reports.clear()
        reports.addAll(newReports)
        notifyDataSetChanged()
    }
}
