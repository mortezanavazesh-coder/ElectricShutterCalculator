class ReportAdapter(
    private var reports: List<ReportModel>,
    private val onDelete: (ReportModel) -> Unit,
    private val onClick: (ReportModel) -> Unit
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
        val tvReportDate: TextView = view.findViewById(R.id.tvReportDate)
        val tvSummaryBlade: TextView = view.findViewById(R.id.tvSummaryBlade)
        val tvSummaryMotor: TextView = view.findViewById(R.id.tvSummaryMotor)
        val tvSummaryTotal: TextView = view.findViewById(R.id.tvSummaryTotal)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteReport)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.tvCustomerName.text = report.customerName
        holder.tvReportDate.text = "تاریخ: ${report.date}"
        holder.tvSummaryBlade.text = report.blade
        holder.tvSummaryMotor.text = report.motor
        holder.tvSummaryTotal.text = report.total

        holder.view.setOnClickListener { onClick(report) }
        holder.btnDelete.setOnClickListener { onDelete(report) }
    }

    override fun getItemCount(): Int = reports.size

    fun updateData(newReports: List<ReportModel>) {
        reports = newReports
        notifyDataSetChanged()
    }
}
