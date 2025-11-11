package com.morteza.shuttercalculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class ReportActivity : AppCompatActivity() {

    private lateinit var textCustomer: TextView
    private lateinit var textDimensions: TextView
    private lateinit var textBreakdown: TextView
    private lateinit var textTotal: TextView
    private lateinit var buttonDeleteReport: Button
    private val db by lazy { AppDatabase.getInstance(this) }
    private val dao by lazy { db.reportDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        textCustomer = findViewById(R.id.textSlatInfo)
        textDimensions = findViewById(R.id.textMotorInfo)
        textBreakdown = findViewById(R.id.textShaftInfo)
        textTotal = findViewById(R.id.textTotalInfo)
        buttonDeleteReport = findViewById(R.id.buttonDeleteReport)

        val reportId = intent.getLongExtra("report_id", -1L)
        if (reportId <= 0L) {
            finish()
            return
        }

        lifecycleScope.launch {
            val report = withContext(Dispatchers.IO) { dao.getById(reportId) }
            if (report == null) {
                Toast.makeText(this@ReportActivity, "گزارش پیدا نشد", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            bindReportToViews(report)

            buttonDeleteReport.setOnClickListener {
                AlertDialog.Builder(this@ReportActivity)
                    .setTitle("حذف گزارش")
                    .setMessage("آیا مطمئن هستید که می‌خواهید این گزارش را حذف کنید؟")
                    .setPositiveButton("حذف") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                val deleted = withContext(Dispatchers.IO) { dao.deleteById(report.id) }
                                if (deleted > 0) {
                                    Toast.makeText(this@ReportActivity, "گزارش حذف شد", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(this@ReportActivity, "حذف ناموفق بود", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(this@ReportActivity, "خطا هنگام حذف: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .setNegativeButton("انصراف", null)
                    .show()
            }
        }
    }

    private fun bindReportToViews(report: ReportEntity) {
        textCustomer.text = "مشتری: ${report.customerName}"
        textDimensions.text = "ابعاد: ${formatDecimal(report.heightCm)} x ${formatDecimal(report.widthCm)} cm"
        textBreakdown.text = report.breakdown
        textTotal.text = formatToman(report.totalPriceToman)
    }

    private fun formatToman(v: Long): String {
        val nf = NumberFormat.getInstance(Locale("fa"))
        return "${nf.format(v)} تومان"
    }

    private fun formatDecimal(v: Double): String {
        return if (v == v.toLong().toDouble()) String.format(Locale.getDefault(), "%d", v.toLong())
        else String.format(Locale.getDefault(), "%.2f", v)
    }
}
