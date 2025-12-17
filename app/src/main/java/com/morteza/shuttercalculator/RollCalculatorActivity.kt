package com.morteza.shuttercalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.morteza.shuttercalculator.utils.PrefsHelper
import kotlin.math.PI
import kotlin.math.max

class RollCalculatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roll_calculator)

        // ویوها
        val inputHeight = findViewById<EditText>(R.id.inputHeight)
        val spinnerBlade = findViewById<Spinner>(R.id.spinnerBlade)
        val spinnerShaft = findViewById<Spinner>(R.id.spinnerShaft)
        val buttonBack = findViewById<Button>(R.id.buttonBackToMain)
        val buttonCalculate = findViewById<Button>(R.id.buttonCalculateRoll)

        val textTotalBlades = findViewById<TextView>(R.id.textTotalBlades)
        val textFullRounds = findViewById<TextView>(R.id.textFullRounds)
        val textPartialRound = findViewById<TextView>(R.id.textPartialRound)
        val textRemainingBlades = findViewById<TextView>(R.id.textRemainingBlades)
        val textRollDiameter = findViewById<TextView>(R.id.textRollDiameter)

        // گرفتن لیست تیغه‌ها و شفت‌ها از PrefsHelper
        val blades = PrefsHelper.getSortedOptionList(this, "تیغه") ?: emptyList()
        val shafts = PrefsHelper.getSortedOptionList(this, "شفت") ?: emptyList()

        spinnerBlade.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, blades)
        spinnerShaft.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, shafts)

        // دکمه بازگشت
        buttonBack.setOnClickListener { finish() }

        // دکمه محاسبه
        buttonCalculate.setOnClickListener {
            val heightCm = inputHeight.text.toString().toDoubleOrNull() ?: 0.0

            if (spinnerBlade.adapter == null || spinnerBlade.adapter.count == 0 ||
                spinnerShaft.adapter == null || spinnerShaft.adapter.count == 0) {
                Toast.makeText(this, "ابتدا تیغه و شفت را تعریف کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // استخراج داده تیغه و شفت (همگی بر حسب سانتی‌متر)
            val bladeSelected = spinnerBlade.selectedItem?.toString() ?: ""
            val shaftSelected = spinnerShaft.selectedItem?.toString() ?: ""

            val bladeWidthCm = PrefsHelper.getSlatWidth(this, bladeSelected).toDouble()
            val bladeThicknessCm = PrefsHelper.getSlatThickness(this, bladeSelected).toDouble()
            val shaftDiameterCm = PrefsHelper.getShaftDiameter(this, shaftSelected).toDouble()

            // اعتبارسنجی ورودی‌ها
            if (bladeWidthCm <= 0 || bladeThicknessCm <= 0 || shaftDiameterCm <= 0 || heightCm <= 0) {
                Toast.makeText(this, "اطلاعات معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // محاسبات
            // تعداد تیغه‌ها (بدون اعشار در نمایش)
            val totalBlades = heightCm / bladeWidthCm
            var remainingBlades = totalBlades

            // شروع قطر رول از قطر شفت
            var rollDiameterCm = shaftDiameterCm

            var fullRounds = 0
            var partialRoundRatio = 0.0
            var lastRemainingBlades = 0.0

            // حلقه دورها: در هر دور کامل، قطر رول به اندازه 2 * ضخامت تیغه افزایش می‌یابد
            while (remainingBlades > 0) {
                val currentCircumference = rollDiameterCm * PI
                val bladesPerRound = currentCircumference / bladeWidthCm

                if (remainingBlades >= bladesPerRound) {
                    // دور کامل
                    remainingBlades -= bladesPerRound
                    rollDiameterCm += (2.0 * bladeThicknessCm) // افزایش قطر: ۲ × ضخامت
                    fullRounds++
                } else {
                    // دور ناقص
                    partialRoundRatio = max(0.0, remainingBlades / bladesPerRound)
                    // افزایش قطر متناسب با نسبت دور ناقص
                    rollDiameterCm += (2.0 * bladeThicknessCm * partialRoundRatio)
                    lastRemainingBlades = remainingBlades
                    remainingBlades = 0.0
                }
            }

            // نمایش خروجی‌ها
            textTotalBlades.text = "تعداد تیغه: %.0f".format(totalBlades)
            textFullRounds.text = "تعداد دور کامل: $fullRounds"
            textPartialRound.text = "دور ناقص: %.2f".format(partialRoundRatio)
            textRemainingBlades.text = "تیغه‌های ناقص دور آخر: %.2f".format(lastRemainingBlades)
            textRollDiameter.text = "قطر رول نهایی: %.2f cm".format(rollDiameterCm)
        }
    }
}
