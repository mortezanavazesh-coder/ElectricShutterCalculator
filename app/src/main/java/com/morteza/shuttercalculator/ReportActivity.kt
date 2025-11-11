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
    private lateinit var buttonDeleteReport: Button

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
        buttonDeleteReport = findViewById(R.id.buttonDeleteReport)

        val reportId = intent.getLongExtra("report_id", -1L)
        if (reportId <= 0L) {
            finish()
            return
        }

        // load report from DB in IO dispatcher
        lifecycleScope.launch {
            val report = withContext(Dispatchers.IO) {
                db.reportDao().getById(reportId)
            }

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
                            withContext(Dispatchers.IO) {
                                db.reportDao().deleteById(report.id)
                            }
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
        textSlatInfo.text = "مشتری: ${report.customerName}"
        textMotorInfo.text = "ابعاد: ${report.heightCm} x ${report.widthCm} cm"
        // breakdown متن کامل را در یکی از فیلدها قرار می‌دهیم
        textShaftInfo.text = report.breakdown
        textTotalInfo.text = "قیمت نهایی: ${formatToman(report.totalPriceToman)}"

        // اگر خواستی جدا جدولی از قطعات داخل breakdown نمایش دهی، آنالیز رشته را اضافه کن
        // و متن‌های textBoxInfo, textInstallInfo, textWeldingInfo, textTransportInfo را نیز مقداردهی کن.
        textBoxInfo.text = "" // یا هر مقدار مناسب
        textInstallInfo.text = ""
        textWeldingInfo.text = ""
        textTransportInfo.text = ""
    }

    private fun formatToman(v: Long): String {
        return String.format("%,d تومان", v)
    }
}
