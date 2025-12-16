package com.morteza.shuttercalculator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.morteza.shuttercalculator.utils.FormatUtils

class ReportDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        val report = intent.getSerializableExtra("report") as? ReportModel ?: run {
            finish()
            return
        }

        // اطلاعات مشتری
        findViewById<TextView>(R.id.tvCustomerName).text = "نام مشتری: ${safeText(report.customerName)}"
        findViewById<TextView>(R.id.tvDate).text = "تاریخ: ${safeText(report.date)}"

        val tvCustomerPhone = findViewById<TextView>(R.id.tvCustomerPhone)
        val phone = report.customerPhone?.trim().orEmpty()
        if (phone.isNotEmpty()) {
            tvCustomerPhone.text = "شماره: $phone"
            tvCustomerPhone.setOnClickListener {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
            }
        } else {
            tvCustomerPhone.text = "شماره: ثبت نشده"
            tvCustomerPhone.setOnClickListener(null)
        }

        // ابعاد کرکره
        findViewById<TextView>(R.id.tvHeight).text = "ارتفاع کرکره: ${report.height} سانتی‌متر"
        findViewById<TextView>(R.id.tvWidth).text = "عرض کرکره: ${report.width} سانتی‌متر"
        findViewById<TextView>(R.id.tvArea).text = "مساحت: ${String.format("%.3f", report.area)} متر مربع"

        // قطعات: قیمت پایه روبرو، جمع زیرش
        findViewById<TextView>(R.id.tvBlade).text =
            "تیغه — قیمت پایه: ${FormatUtils.formatToman(report.bladeBasePrice)}\nجمع: ${FormatUtils.formatToman(report.bladeTotal)}"

        findViewById<TextView>(R.id.tvMotor).text =
            "موتور — قیمت پایه: ${FormatUtils.formatToman(report.motorBasePrice)}\nجمع: ${FormatUtils.formatToman(report.motorTotal)}"

        findViewById<TextView>(R.id.tvShaft).text =
            "شفت — قیمت پایه: ${FormatUtils.formatToman(report.shaftBasePrice)}\nجمع: ${FormatUtils.formatToman(report.shaftTotal)}"

        findViewById<TextView>(R.id.tvBox).text =
            "قوطی — قیمت پایه: ${FormatUtils.formatToman(report.boxBasePrice)}\nجمع: ${FormatUtils.formatToman(report.boxTotal)}"

        // هزینه‌های پایه: قیمت پایه روبرو، جمع زیرش
        findViewById<TextView>(R.id.tvInstall).text =
            "هزینه نصب — قیمت پایه: ${FormatUtils.formatToman(report.installBasePrice)}\nجمع: ${FormatUtils.formatToman(report.installTotal)}"

        findViewById<TextView>(R.id.tvWelding).text =
            "جوشکاری — قیمت پایه: ${FormatUtils.formatToman(report.weldingBasePrice)}\nجمع: ${FormatUtils.formatToman(report.weldingTotal)}"

        findViewById<TextView>(R.id.tvTransport).text =
            "کرایه حمل — قیمت پایه: ${FormatUtils.formatToman(report.transportBasePrice)}\nجمع: ${FormatUtils.formatToman(report.transportTotal)}"

        // گزینه‌های اضافی: نمایش لیست با قیمت و جمع کل
        val tvExtras = findViewById<TextView>(R.id.tvExtras)
        if (!report.extrasSelected.isNullOrEmpty()) {
            val extrasLines = report.extrasSelected.joinToString("\n") { extra ->
                "${extra.name} — ${FormatUtils.formatToman(extra.basePrice)}"
            }
            tvExtras.text = "گزینه‌های اضافی:\n$extrasLines\nجمع: ${FormatUtils.formatToman(report.extrasTotal)}"
        } else {
            tvExtras.text = "گزینه‌های اضافی: انتخاب نشده"
        }

        // ریز محاسبات
        findViewById<TextView>(R.id.tvBreakBlade).text =
            "جمع تیغه: ${FormatUtils.formatToman(report.bladeTotal)}"
        findViewById<TextView>(R.id.tvBreakMotor).text =
            "موتور: ${FormatUtils.formatToman(report.motorTotal)}"
        findViewById<TextView>(R.id.tvBreakShaft).text =
            "جمع شفت: ${FormatUtils.formatToman(report.shaftTotal)}"
        findViewById<TextView>(R.id.tvBreakBox).text =
            "جمع قوطی: ${FormatUtils.formatToman(report.boxTotal)}"
        findViewById<TextView>(R.id.tvBreakInstall).text =
            "هزینه نصب: ${FormatUtils.formatToman(report.installTotal)}"
        findViewById<TextView>(R.id.tvBreakWelding).text =
            "جوشکاری: ${FormatUtils.formatToman(report.weldingTotal)}"
        findViewById<TextView>(R.id.tvBreakTransport).text =
            "کرایه حمل: ${FormatUtils.formatToman(report.transportTotal)}"
        findViewById<TextView>(R.id.tvBreakExtras).text =
            "گزینه‌های اضافی: ${FormatUtils.formatToman(report.extrasTotal)}"

        // جمع کل
        findViewById<TextView>(R.id.tvTotal).text = "قیمت نهایی: ${FormatUtils.formatToman(report.total)}"

        // دکمه بازگشت
        findViewById<Button>(R.id.buttonBackToReports).setOnClickListener { finish() }
    }

    private fun safeText(value: String?): String = value?.trim().takeUnless { it.isNullOrEmpty() } ?: "—"
}
