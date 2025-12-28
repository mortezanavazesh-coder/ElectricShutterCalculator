package com.morteza.shuttercalculator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.morteza.shuttercalculator.utils.FormatUtils

class ReportDetailActivity : AppCompatActivity() {

    private lateinit var tvCustomerName: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvCustomerPhone: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvWidth: TextView
    private lateinit var tvArea: TextView

    private lateinit var tvBlade: TextView
    private lateinit var tvMotor: TextView
    private lateinit var tvShaft: TextView
    private lateinit var tvBox: TextView

    private lateinit var tvInstall: TextView
    private lateinit var tvWelding: TextView
    private lateinit var tvTransport: TextView

    private lateinit var tvExtras: TextView

    private lateinit var tvBreakBlade: TextView
    private lateinit var tvBreakMotor: TextView
    private lateinit var tvBreakShaft: TextView
    private lateinit var tvBreakBox: TextView
    private lateinit var tvBreakInstall: TextView
    private lateinit var tvBreakWelding: TextView
    private lateinit var tvBreakTransport: TextView
    private lateinit var tvBreakExtras: TextView

    private lateinit var tvTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        val report = intent.getSerializableExtra("report") as? ReportModel ?: run {
            finish()
            return
        }

        bindViews()
        bindCustomerInfo(report)
        bindDimensions(report)
        bindBasePrices(report)
        bindExtras(report)
        bindBreakdown(report)
        bindTotal(report)

        findViewById<Button>(R.id.buttonBackToReports).setOnClickListener { finish() }
    }

    private fun bindViews() {
        tvCustomerName = findViewById(R.id.tvCustomerName)
        tvDate = findViewById(R.id.tvDate)
        tvCustomerPhone = findViewById(R.id.tvCustomerPhone)
        tvHeight = findViewById(R.id.tvHeight)
        tvWidth = findViewById(R.id.tvWidth)
        tvArea = findViewById(R.id.tvArea)

        tvBlade = findViewById(R.id.tvBlade)
        tvMotor = findViewById(R.id.tvMotor)
        tvShaft = findViewById(R.id.tvShaft)
        tvBox = findViewById(R.id.tvBox)

        tvInstall = findViewById(R.id.tvInstall)
        tvWelding = findViewById(R.id.tvWelding)
        tvTransport = findViewById(R.id.tvTransport)

        tvExtras = findViewById(R.id.tvExtras)

        tvBreakBlade = findViewById(R.id.tvBreakBlade)
        tvBreakMotor = findViewById(R.id.tvBreakMotor)
        tvBreakShaft = findViewById(R.id.tvBreakShaft)
        tvBreakBox = findViewById(R.id.tvBreakBox)
        tvBreakInstall = findViewById(R.id.tvBreakInstall)
        tvBreakWelding = findViewById(R.id.tvBreakWelding)
        tvBreakTransport = findViewById(R.id.tvBreakTransport)
        tvBreakExtras = findViewById(R.id.tvBreakExtras)

        tvTotal = findViewById(R.id.tvTotal)
    }

    private fun bindCustomerInfo(report: ReportModel) {
        tvCustomerName.text = "نام مشتری: ${safeText(report.customerName)}"
        tvDate.text = "تاریخ: ${safeText(report.date)}"

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
    }

    private fun bindDimensions(report: ReportModel) {
        tvHeight.text = "ارتفاع کرکره: ${report.height.toInt()} سانتی‌متر"
        tvWidth.text = "عرض کرکره: ${report.width.toInt()} سانتی‌متر"
        tvArea.text = "مساحت: ${String.format("%.3f", report.area)} متر مربع"
    }

    private fun bindBasePrices(report: ReportModel) {
        tvBlade.text = "تیغه — قیمت پایه: ${FormatUtils.formatToman(report.bladeBasePrice)}"
        tvMotor.text = "موتور — قیمت پایه: ${FormatUtils.formatToman(report.motorBasePrice)}"
        tvShaft.text = "شفت — قیمت پایه: ${FormatUtils.formatToman(report.shaftBasePrice)}"
        tvBox.text = "قوطی — قیمت پایه: ${FormatUtils.formatToman(report.boxBasePrice)}"

        tvInstall.text = "هزینه نصب — قیمت پایه: ${FormatUtils.formatToman(report.installBasePrice)}"
        tvWelding.text = "جوشکاری — قیمت پایه: ${FormatUtils.formatToman(report.weldingBasePrice)}"
        tvTransport.text = "کرایه حمل — قیمت پایه: ${FormatUtils.formatToman(report.transportBasePrice)}"
    }

    private fun bindExtras(report: ReportModel) {
        if (!report.extrasSelected.isNullOrEmpty()) {
            val extrasLines = report.extrasSelected.joinToString("\n") { extra ->
                "${extra.name} — قیمت پایه: ${FormatUtils.formatToman(extra.basePrice)}"
            }
            tvExtras.text = "گزینه‌های اضافی:\n$extrasLines"
        } else {
            tvExtras.text = "گزینه‌های اضافی: انتخاب نشده"
        }
    }

    private fun bindBreakdown(report: ReportModel) {
        tvBreakBlade.text = "جمع تیغه: ${FormatUtils.formatToman(report.bladeTotal)}"
        tvBreakMotor.text = "جمع موتور: ${FormatUtils.formatToman(report.motorTotal)}"
        tvBreakShaft.text = "جمع شفت: ${FormatUtils.formatToman(report.shaftTotal)}"
        tvBreakBox.text = "جمع قوطی: ${FormatUtils.formatToman(report.boxTotal)}"
        tvBreakInstall.text = "هزینه نصب: ${FormatUtils.formatToman(report.installTotal)}"
        tvBreakWelding.text = "جوشکاری: ${FormatUtils.formatToman(report.weldingTotal)}"
        tvBreakTransport.text = "کرایه حمل: ${FormatUtils.formatToman(report.transportTotal)}"
        tvBreakExtras.text = "گزینه‌های اضافی: ${FormatUtils.formatToman(report.extrasTotal)}"
    }

    private fun bindTotal(report: ReportModel) {
        tvTotal.text = "قیمت نهایی: ${FormatUtils.formatToman(report.total)}"
    }

    private fun safeText(value: String?): String =
        value?.trim().takeUnless { it.isNullOrEmpty() } ?: "—"
}
