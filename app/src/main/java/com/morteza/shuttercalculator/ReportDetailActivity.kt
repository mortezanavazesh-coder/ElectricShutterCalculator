package com.morteza.shuttercalculator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class ReportDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_REPORT = "extra_report"

        fun newIntent(context: Context, report: ReportModel): Intent {
            return Intent(context, ReportDetailActivity::class.java).apply {
                putExtra(EXTRA_REPORT, report)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        val report = intent.getSerializableExtra(EXTRA_REPORT) as? ReportModel
        if (report != null) {
            findViewById<TextView>(R.id.tvCustomerName).text = "نام مشتری: ${report.customerName}"
            findViewById<TextView>(R.id.tvReportDate).text = "تاریخ: ${report.date}"
            findViewById<TextView>(R.id.tvHeight).text = "ارتفاع: ${report.height} cm"
            findViewById<TextView>(R.id.tvWidth).text = "عرض: ${report.width} cm"
            findViewById<TextView>(R.id.tvArea).text = report.area
            findViewById<TextView>(R.id.tvBlade).text = report.blade
            findViewById<TextView>(R.id.tvMotor).text = report.motor
            findViewById<TextView>(R.id.tvShaft).text = report.shaft
            findViewById<TextView>(R.id.tvBox).text = report.box
            findViewById<TextView>(R.id.tvInstall).text = "نصب: ${report.install}"
            findViewById<TextView>(R.id.tvWelding).text = "جوشکاری: ${report.welding}"
            findViewById<TextView>(R.id.tvTransport).text = "کرایه حمل: ${report.transport}"
            findViewById<TextView>(R.id.tvExtras).text = report.extras
            findViewById<TextView>(R.id.tvTotal).text = report.total
        }
    }
}
