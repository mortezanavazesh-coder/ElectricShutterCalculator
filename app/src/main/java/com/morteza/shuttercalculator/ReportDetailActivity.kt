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

        val report = intent.getSerializableExtra("report") as? ReportModel ?: return

        // اطلاعات مشتری
        findViewById<TextView>(R.id.tvCustomerName).text = "نام مشتری: ${report.customerName}"
        findViewById<TextView>(R.id.tvDate).text = "تاریخ: ${report.date}"

        val tvCustomerPhone = findViewById<TextView>(R.id.tvCustomerPhone)
        if (!report.customerPhone.isNullOrEmpty()) {
            tvCustomerPhone.text = "شماره: ${report.customerPhone}"
            tvCustomerPhone.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${report.customerPhone}"))
                startActivity(intent)
            }
        } else {
            tvCustomerPhone.text = "شماره: ثبت نشده"
        }

        // ابعاد کرکره
        findViewById<TextView>(R.id.tvHeight).text = "ارتفاع کرکره: ${report.height} سانتی‌متر"
        findViewById<TextView>(R.id.tvWidth).text = "عرض کرکره: ${report.width} سانتی‌متر"
        findViewById<TextView>(R.id.tvArea).text = "مساحت: ${String.format("%.3f", report.area)} متر مربع"

        // مشخصات کرکره
        findViewById<TextView>(R.id.tvBlade).text =
            "تیغه: ${report.bladeName}\nقیمت پایه: ${FormatUtils.formatToman(report.bladeBasePrice)}\nجمع: ${FormatUtils.formatToman(report.bladeTotal)}"

        findViewById<TextView>(R.id.tvMotor).text =
            "موتور: ${report.motorName}\nقیمت پایه: ${FormatUtils.formatToman(report.motorBasePrice)}\nجمع: ${FormatUtils.formatToman(report.motorTotal)}"

        findViewById<TextView>(R.id.tvShaft).text =
            "شفت: ${report.shaftName}\nقیمت پایه: ${FormatUtils.formatToman(report.shaftBasePrice)}\nجمع: ${FormatUtils.formatToman(report.shaftTotal)}"

        findViewById<TextView>(R.id.tvBox).text =
            "قوطی: ${report.boxName}\nقیمت پایه: ${FormatUtils.formatToman(report.boxBasePrice)}\nجمع: ${FormatUtils.formatToman(report.boxTotal)}"

        // هزینه‌های پایه
        findViewById<TextView>(R.id.tvInstall).text =
            "نصب\nقیمت پایه: ${FormatUtils.formatToman(report.installBasePrice)}\nجمع: ${FormatUtils.formatToman(report.installTotal)}"

        findViewById<TextView>(R.id.tvWelding).text =
            "جوشکاری\nقیمت پایه: ${FormatUtils.formatToman(report.weldingBasePrice)}\nجمع: ${FormatUtils.formatToman(report.weldingTotal)}"

        findViewById<TextView>(R.id.tvTransport).text =
            "کرایه حمل\nقیمت پایه: ${FormatUtils.formatToman(report.transportBasePrice)}\nجمع: ${FormatUtils.formatToman(report.transportTotal)}"

        // گزینه‌های اضافی
        val tvExtras = findViewById<TextView>(R.id.tvExtras)
        if (report.extrasSelected.isNotEmpty()) {
            val extrasText = report.extrasSelected.joinToString("\n") {
                "${it.name} — ${FormatUtils.formatToman(it.basePrice)}"
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
