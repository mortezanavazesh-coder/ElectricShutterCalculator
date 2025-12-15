package com.morteza.shuttercalculator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.morteza.shuttercalculator.utils.FormatUtils

class ReportDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        val report = intent.getSerializableExtra("report") as? ReportModel
            ?: return

        // اطلاعات مشتری
        val tvCustomerName = findViewById<TextView>(R.id.tvCustomerName)
        val tvCustomerPhone = findViewById<TextView>(R.id.tvCustomerPhone)
        val tvDate = findViewById<TextView>(R.id.tvDate)

        tvCustomerName.text = "نام مشتری: ${report.customerName}"
        tvDate.text = "تاریخ: ${report.date}"

        if (!report.customerPhone.isNullOrEmpty()) {
            tvCustomerPhone.text = "شماره: ${report.customerPhone}"
            tvCustomerPhone.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${report.customerPhone}"))
                startActivity(intent)
            }
        } else {
            tvCustomerPhone.text = "شماره: ثبت نشده"
        }

        // مشخصات کرکره
        findViewById<TextView>(R.id.tvBlade).text =
            "تیغه: ${report.bladeName} (قیمت پایه: ${FormatUtils.formatToman(report.bladeBasePrice)}) → جمع: ${FormatUtils.formatToman(report.bladeTotal)}"
        findViewById<TextView>(R.id.tvMotor).text =
            "موتور: ${report.motorName} (قیمت پایه: ${FormatUtils.formatToman(report.motorBasePrice)}) → جمع: ${FormatUtils.formatToman(report.motorTotal)}"
        findViewById<TextView>(R.id.tvShaft).text =
            "شفت: ${report.shaftName} (قیمت پایه: ${FormatUtils.formatToman(report.shaftBasePrice)}) → جمع: ${FormatUtils.formatToman(report.shaftTotal)}"
        findViewById<TextView>(R.id.tvBox).text =
            "قوطی: ${report.boxName} (قیمت پایه: ${FormatUtils.formatToman(report.boxBasePrice)}) → جمع: ${FormatUtils.formatToman(report.boxTotal)}"

        // هزینه‌های پایه
        findViewById<TextView>(R.id.tvInstall).text =
            "نصب: قیمت پایه ${FormatUtils.formatToman(report.installBasePrice)} → جمع: ${FormatUtils.formatToman(report.installTotal)}"
        findViewById<TextView>(R.id.tvWelding).text =
            "جوشکاری: قیمت پایه ${FormatUtils.formatToman(report.weldingBasePrice)} → جمع: ${FormatUtils.formatToman(report.weldingTotal)}"
        findViewById<TextView>(R.id.tvTransport).text =
            "کرایه حمل: قیمت پایه ${FormatUtils.formatToman(report.transportBasePrice)} → جمع: ${FormatUtils.formatToman(report.transportTotal)}"

        // گزینه‌های اضافی
        val tvExtras = findViewById<TextView>(R.id.tvExtras)
        if (report.extrasSelected.isNotEmpty()) {
            val extrasText = report.extrasSelected.joinToString("\n") {
                "${it.name} (قیمت پایه: ${FormatUtils.formatToman(it.basePrice)})"
            }
            tvExtras.text = "گزینه‌های اضافی:\n$extrasText\nجمع: ${FormatUtils.formatToman(report.extrasTotal)}"
        } else {
            tvExtras.text = "گزینه‌های اضافی: انتخاب نشده"
        }

        // جمع کل
        findViewById<TextView>(R.id.tvTotal).text =
            "قیمت نهایی: ${FormatUtils.formatToman(report.total)}"
    }
}
