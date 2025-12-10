package com.morteza.shuttercalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.morteza.shuttercalculator.ReportModel


class ReportAdapter(
    private var reports: List<ReportModel>,
    private val onDelete: (ReportModel) -> Unit,
    private val onClick: (ReportModel) -> Unit
) : RecyclerView.Adapter<ReportAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.textReportTitle)
        val tvDate: TextView = itemView.findViewById(R.id.textReportDate)
        val tvPrice: TextView = itemView.findViewById(R.id.textReportPrice)
        val btnDelete: ImageButton = itemView.findViewById(R.id.buttonDeleteReport)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val report = reports[position]
        holder.tvTitle.text = report.customerName
        holder.tvDate.text = report.date
        holder.tvPrice.text = "جمع کل: ${report.total}"

        holder.itemView.setOnClickListener { onClick(report) }
        holder.btnDelete.setOnClickListener { onDelete(report) }
    }

    override fun getItemCount(): Int = reports.size

    fun update(newReports: List<ReportModel>) {
        reports = newReports
        notifyDataSetChanged()
    }
}

