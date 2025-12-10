package com.morteza.shuttercalculator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import java.io.Serializable
import com.morteza.shuttercalculator.ReportModel


class ReportDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_REPORT = "extra_report"

        fun newIntent(context: Context, report: ReportModel): Intent {
            return Intent(context, ReportDetailActivity::class.java).apply {
                putExtra(EXTRA_REPORT, report as Serializable) // مشخص کردن Serializable
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        val report = intent.getSerializableExtra(EXTRA_REPORT) as? ReportModel
        report?.let {
            findViewById<TextView>(R.id.tvCustomerName).text = "نام مشتری: ${it.customerName}"
            findViewById<TextView>(R.id.tvReportDate).text = "تاریخ: ${it.date}"
            findViewById<TextView>(R.id.tvHeight).text = "ارتفاع: ${it.height} cm"
            findViewById<TextView>(R.id.tvWidth).text = "عرض: ${it.width} cm"
            findViewById<TextView>(R.id.tvArea).text = "مساحت: ${it.area}"
            findViewById<TextView>(R.id.tvBlade).text = "تیغه: ${it.blade}"
            findViewById<TextView>(R.id.tvMotor).text = "موتور: ${it.motor}"
            findViewById<TextView>(R.id.tvShaft).text = "شفت: ${it.shaft}"
            findViewById<TextView>(R.id.tvBox).text = "قوطی: ${it.box}"
            findViewById<TextView>(R.id.tvInstall).text = "نصب: ${it.install}"
            findViewById<TextView>(R.id.tvWelding).text = "جوشکاری: ${it.welding}"
            findViewById<TextView>(R.id.tvTransport).text = "کرایه حمل: ${it.transport}"
            findViewById<TextView>(R.id.tvExtras).text = "اضافات: ${it.extras}"
            findViewById<TextView>(R.id.tvTotal).text = "جمع کل: ${it.total}"
        }
    }
}

