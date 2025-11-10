package com.morteza.shuttercalculator

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.morteza.shuttercalculator.utils.CalculationUtils
import com.morteza.shuttercalculator.utils.PrefsHelper

class ReportActivity : AppCompatActivity() {

    private lateinit var textSlatInfo: TextView
    private lateinit var textMotorInfo: TextView
    private lateinit var textShaftInfo: TextView
    private lateinit var textBoxInfo: TextView
    private lateinit var textInstallInfo: TextView
    private lateinit var textWeldingInfo: TextView
    private lateinit var textTransportInfo: TextView
    private lateinit var textTotalInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // اتصال ویوها
        textSlatInfo = findViewById(R.id.textSlatInfo)
        textMotorInfo = findViewById(R.id.textMotorInfo)
        textShaftInfo = findViewById(R.id.textShaftInfo)
        textBoxInfo = findViewById(R.id.textBoxInfo)
        textInstallInfo = findViewById(R.id.textInstallInfo)
        textWeldingInfo = findViewById(R.id.textWeldingInfo)
        textTransportInfo = findViewById(R.id.textTransportInfo)
        textTotalInfo = findViewById(R.id.textTotalInfo)

        // دریافت داده‌ها از SharedPreferences
        val slatName = intent.getStringExtra("slat") ?: ""
        val motorName = intent.getStringExtra("motor") ?: ""
        val shaftName = intent.getStringExtra("shaft") ?: ""
        val boxName = intent.getStringExtra("box") ?: ""
        val shaftLength = intent.getFloatExtra("shaftLength", 0f)
        val boxLength = intent.getFloatExtra("boxLength", 0f)
        val includeBox = intent.getBooleanExtra("includeBox", false)
        val install = intent.getFloatExtra("install", 0f)
        val welding = intent.getFloatExtra("welding", 0f)
        val transport = intent.getFloatExtra("transport", 0f)

        val slatPrice = CalculationUtils.getPrice(this, "تیغه", slatName)
        val motorPrice = CalculationUtils.getPrice(this, "موتور", motorName)
        val shaftPrice = CalculationUtils.getPrice(this, "شفت", shaftName)
        val boxPrice = CalculationUtils.getPrice(this, "قوطی", boxName)

        val total = CalculationUtils.calculateTotalPrice(
            slatPrice,
            motorPrice,
            shaftPrice,
            shaftLength,
            boxPrice,
            boxLength,
            includeBox,
            install,
            welding,
            transport
        )

        // نمایش اطلاعات
        textSlatInfo.text = "تیغه: $slatName - ${slatPrice.toInt()} تومان"
        textMotorInfo.text = "موتور: $motorName - ${motorPrice.toInt()} تومان"
        textShaftInfo.text = "شفت: $shaftName - ${shaftLength} متر × ${shaftPrice.toInt()} تومان"
        textBoxInfo.text = if (includeBox) {
            "قوطی: $boxName - ${boxLength} متر × ${boxPrice.toInt()} تومان"
        } else {
            "قوطی: لحاظ نشده"
        }
        textInstallInfo.text = "نصب: ${install.toInt()} تومان"
        textWeldingInfo.text = "جوشکاری: ${welding.toInt()} تومان"
        textTransportInfo.text = "حمل: ${transport.toInt()} تومان"
        textTotalInfo.text = "قیمت نهایی: ${total.toInt()} تومان"
    }
}
