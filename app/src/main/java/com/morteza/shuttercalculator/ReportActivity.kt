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

class ReportActivity : AppCompatActivity() {

    private lateinit var textSlatInfo: TextView
    private lateinit var textMotorInfo: TextView
    private lateinit var textShaftInfo: TextView
    private lateinit var textBoxInfo: TextView
    private lateinit var textInstallInfo: TextView
    private lateinit var textWeldingInfo: TextView
    private lateinit var textTransportInfo: TextView
    private lateinit var textTotalInfo: TextView
    private lateinit var buttonDelete: Button

    private val db by lazy { AppDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        textSlatInfo = findViewById(R.id.textSlatInfo)
        textMotorInfo = findViewById(R.id.textMotorInfo)
        textShaftInfo = findViewById(R.id.textShaftInfo)
        textBoxInfo = findViewById(R.id.textBoxInfo)
        textInstallInfo = findViewById(R.id.textInstallInfo)
        textWeldingInfo = findViewById(R.id.textWeldingInfo)
        textTransportInfo = findViewById(R.id.textTransportInfo)
        textTotalInfo = findViewById(R.id.textTotalInfo)
        buttonDelete = findViewById(R.id.buttonDeleteReport)

        val reportId = intent.getLongExtra("report_id", -1L)
        if (reportId <= 0L) {
            finish()
            return
        }

        lifecycleScope.launch {
            val report = withContext(Dispatchers.IO) {
                db.reportDao().allReportsFlow().firstOrNull { it.id == reportId }
            }
            if (report == null) {
                finish()
                return@launch
            }
            bindReportToViews(report)

            buttonDelete.setOnClickListener {
                AlertDialog.Builder(this@ReportActivity)
                    .setTitle("حذف گزارش")
                    .setMessage("آیا مطمئنی که می‌خواهی این گزارش حذف شود؟")
                    .setPositiveButton("حذف") { _, _ ->
                        lifecycleScope.launch {
                            db.reportDao().deleteById(report.id)
                            Toast.makeText(this@ReportActivity, "گزارش حذف شد", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    .setNegativeButton("انصراف", null)
                    .show()
            }
        }
    }

    private fun bindReportToViews(report: ReportEntity) {
        // نمایش ساده: نام، ابعاد، ریزمحاسبات، قیمت
        textSlatInfo.text = "مشتری: ${report.customerName}"
        textMotorInfo.text = "ابعاد: ${report.heightCm} x ${report.widthCm} cm"
        textShaftInfo.text = report.breakdown
        textTotalInfo.text = "قیمت نهایی: ${formatToman(report.totalPriceToman)}"
    }

    private fun formatToman(v: Long): String {
        return String.format("%,d تومان", v)
    }
}
