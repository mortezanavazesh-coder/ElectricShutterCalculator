package com.morteza.shuttercalculator

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.morteza.shuttercalculator.utils.PrefsHelper
import kotlin.math.PI
import kotlin.math.max
import android.view.ViewGroup


class RollCalculatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roll_calculator)

        // ویوها
        val inputHeight = findViewById<EditText>(R.id.inputHeight)
        val spinnerBlade = findViewById<Spinner>(R.id.spinnerBlade)
        val spinnerShaft = findViewById<Spinner>(R.id.spinnerShaft)
        val buttonBack = findViewById<MaterialButton>(R.id.buttonBackToMain)
        val buttonCalculate = findViewById<MaterialButton>(R.id.buttonCalculateRoll)

        val textTotalBlades = findViewById<TextView>(R.id.textTotalBlades)
        val textFullRounds = findViewById<TextView>(R.id.textFullRounds)
        val textPartialRound = findViewById<TextView>(R.id.textPartialRound)
        val textRemainingBlades = findViewById<TextView>(R.id.textRemainingBlades)
        val textRollDiameter = findViewById<TextView>(R.id.textRollDiameter)

        // گرفتن لیست تیغه‌ها و شفت‌ها از PrefsHelper
        val blades = PrefsHelper.getSortedOptionList(this, "تیغه") ?: emptyList()
        val shafts = PrefsHelper.getSortedOptionList(this, "شفت") ?: emptyList()

        spinnerBlade.adapter = makeThemedAdapter(blades)
        spinnerShaft.adapter = makeThemedAdapter(shafts)

        // دکمه بازگشت
        buttonBack.setOnClickListener { finish() }

        // دکمه محاسبه
        buttonCalculate.setOnClickListener {
            val heightCm = inputHeight.text.toString().toDoubleOrNull() ?: 0.0

            if (spinnerBlade.adapter == null || spinnerBlade.adapter.count == 0 ||
                spinnerShaft.adapter == null || spinnerShaft.adapter.count == 0) {
                Toast.makeText(this, getString(R.string.error_define_blade_shaft), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, getString(R.string.error_invalid_input), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // محاسبات
            val totalBlades = heightCm / bladeWidthCm
            var remainingBlades = totalBlades
            var rollDiameterCm = shaftDiameterCm
            var fullRounds = 0
            var partialRoundRatio = 0.0
            var lastRemainingBlades = 0.0

            while (remainingBlades > 0) {
                val currentCircumference = rollDiameterCm * PI
                val bladesPerRound = currentCircumference / bladeWidthCm

                if (remainingBlades >= bladesPerRound) {
                    remainingBlades -= bladesPerRound
                    rollDiameterCm += (2.0 * bladeThicknessCm)
                    fullRounds++
                } else {
                    partialRoundRatio = max(0.0, remainingBlades / bladesPerRound)
                    rollDiameterCm += (2.0 * bladeThicknessCm * partialRoundRatio)
                    lastRemainingBlades = remainingBlades
                    remainingBlades = 0.0
                }
            }

            // نمایش خروجی‌ها
            textTotalBlades.text = getString(R.string.result_total_blades, totalBlades)
            textFullRounds.text = getString(R.string.result_full_rounds, fullRounds)
            textPartialRound.text = getString(R.string.result_partial_round, partialRoundRatio)
            textRemainingBlades.text = getString(R.string.result_remaining_blades, lastRemainingBlades)
            textRollDiameter.text = getString(R.string.result_roll_diameter, rollDiameterCm)
        }
    }

    // آداپتر اسپینر Theme-driven
    private fun makeThemedAdapter(items: List<String>): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            items
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tv = super.getView(position, convertView, parent) as TextView
                tv.setTextColor(MaterialColors.getColor(tv, com.google.android.material.R.attr.colorOnPrimary))
                tv.ellipsize = TextUtils.TruncateAt.END
                tv.maxLines = 1
                return tv
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tv = super.getDropDownView(position, convertView, parent) as TextView
                tv.setTextColor(MaterialColors.getColor(tv, com.google.android.material.R.attr.colorOnSurface))
                tv.ellipsize = TextUtils.TruncateAt.END
                tv.maxLines = 1
                return tv
            }
        }.apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }
}

