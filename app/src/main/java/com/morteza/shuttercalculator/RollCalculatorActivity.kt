package com.morteza.shuttercalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.morteza.shuttercalculator.utils.PrefsHelper

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
            val height = inputHeight.text.toString().toDoubleOrNull() ?: 0.0

            // بررسی خالی بودن لیست‌ها
            if (spinnerBlade.adapter == null || spinnerBlade.adapter.count == 0 ||
                spinnerShaft.adapter == null || spinnerShaft.adapter.count == 0) {
                Toast.makeText(this, "ابتدا تیغه و شفت را تعریف کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // استخراج داده تیغه
            val bladeSelected = spinnerBlade.selectedItem?.toString() ?: ""
            val bladeWidth = PrefsHelper.getSlatWidth(this, bladeSelected).toDouble()
            val bladeThickness = PrefsHelper.getSlatThickness(this, bladeSelected).toDouble()

            // استخراج داده شفت
            val shaftSelected = spinnerShaft.selectedItem?.toString() ?: ""
            val shaftDiameter = PrefsHelper.getShaftDiameter(this, shaftSelected).toDouble()

            // اعتبارسنجی ورودی‌ها
            if (bladeWidth <= 0 || bladeThickness <= 0 || shaftDiameter <= 0 || height <= 0) {
                Toast.makeText(this, "اطلاعات معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // محاسبات
            val totalBlades = height / bladeWidth
            var remainingBlades = totalBlades
            var rollDiameter = shaftDiameter

            val circumference = shaftDiameter * Math.PI
            val firstRoundBlades = circumference / bladeWidth
            remainingBlades -= firstRoundBlades
            rollDiameter += (2 * bladeThickness)

            var fullRounds = 0
            var partialRound = 0.0
            var lastRemainingBlades = 0.0   // تیغه‌های ناقص دور آخر

            while (remainingBlades > 0) {
                val currentCircumference = rollDiameter * Math.PI
                val bladesPerRound = currentCircumference / bladeWidth

                if (remainingBlades >= bladesPerRound) {
                    remainingBlades -= bladesPerRound
                    rollDiameter += (2 * bladeThickness)
                    fullRounds++
                } else {
                    partialRound = remainingBlades / bladesPerRound
                    rollDiameter += (2 * bladeThickness * partialRound)
                    lastRemainingBlades = remainingBlades   // ذخیره تیغه‌های ناقص دور آخر
                    remainingBlades = 0.0
                }
            }

            // نمایش خروجی‌ها
            textTotalBlades.text = "تعداد تیغه: %.2f".format(totalBlades)
            textFullRounds.text = "تعداد دور کامل: $fullRounds"
            textPartialRound.text = "دور ناقص: %.2f".format(partialRound)
            textRemainingBlades.text = "تیغه‌های ناقص دور آخر: %.2f".format(lastRemainingBlades)
            textRollDiameter.text = "قطر رول نهایی: %.2f cm".format(rollDiameter)
        }
    }
}
