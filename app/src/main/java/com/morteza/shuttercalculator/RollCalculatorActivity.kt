package com.morteza.shuttercalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.morteza.shuttercalculator.utils.FormatUtils
import com.morteza.shuttercalculator.utils.PrefsHelper
import com.morteza.shuttercalculator.utils.ThousandSeparatorTextWatcher
import kotlin.math.PI
import kotlin.math.sqrt

class RollCalculatorActivity : AppCompatActivity() {

    private lateinit var spinnerBlade: Spinner
    private lateinit var inputWidthCm: EditText
    private lateinit var inputHeightCm: EditText
    private lateinit var inputThicknessMm: EditText
    private lateinit var inputCoreDiameterMm: EditText
    private lateinit var inputPackingFactor: EditText
    private lateinit var buttonCalc: Button
    private lateinit var textResultDiameter: TextView
    private lateinit var buttonSaveResult: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roll_calculator)

        spinnerBlade = findViewById(R.id.spinnerBlade)
        inputWidthCm = findViewById(R.id.inputWidthCm)
        inputHeightCm = findViewById(R.id.inputHeightCm)
        inputThicknessMm = findViewById(R.id.inputThicknessMm)
        inputCoreDiameterMm = findViewById(R.id.inputCoreDiameterMm)
        inputPackingFactor = findViewById(R.id.inputPackingFactor)
        buttonCalc = findViewById(R.id.buttonCalc)
        textResultDiameter = findViewById(R.id.textResultDiameter)
        buttonSaveResult = findViewById(R.id.buttonSaveResult)

        // format helpers for numeric fields (thousand separator for mm values not necessary but okay)
        inputThicknessMm.addTextChangedListener(ThousandSeparatorTextWatcher(inputThicknessMm))
        inputCoreDiameterMm.addTextChangedListener(ThousandSeparatorTextWatcher(inputCoreDiameterMm))

        // load blade list from PrefsHelper via MainViewModel data shape or directly
        // Here we try to read option list for category "تیغه"
        val blades = PrefsHelper.getSortedOptionList(this, "تیغه")
        if (blades.isEmpty()) {
            spinnerBlade.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("بدون تیغه")).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        } else {
            spinnerBlade.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, blades).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }

        // set defaults from PrefsHelper if available
        val defaultCore = PrefsHelper.getFloat(this, "roll_core_diameter")
        if (defaultCore > 0f) inputCoreDiameterMm.setText(FormatUtils.formatTomanPlain(defaultCore))

        val defaultThickness = PrefsHelper.getFloat(this, "default_blade_thickness_mm")
        if (defaultThickness > 0f) inputThicknessMm.setText(FormatUtils.formatTomanPlain(defaultThickness)) 
        else inputThicknessMm.setText("0.9")

        inputPackingFactor.setText("1.0") // default packing factor

        // when user picks a blade, try to load its thickness if stored under blade_thickness_<name> (mm)
        spinnerBlade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val name = spinnerBlade.selectedItem as? String ?: return
                val key = "blade_thickness_$name"
                val t = PrefsHelper.getFloat(this@RollCalculatorActivity, key)
                if (t > 0f) {
                    inputThicknessMm.setText(FormatUtils.formatTomanPlain(t))
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        buttonCalc.setOnClickListener { calculateAndShow() }

        // save result (optional) to prefs
        buttonSaveResult.setOnClickListener {
            val last = textResultDiameter.text.toString()
            if (last.contains("-") || last.isEmpty()) {
                Toast.makeText(this, "نتیجه‌ای برای ذخیره وجود ندارد", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // store last diameter in mm as float under key
            val mm = last.filter { it.isDigit() || it == '.' }.toFloatOrNull()
            if (mm != null) {
                PrefsHelper.saveFloat(this, "last_roll_diameter_mm", mm)
                Toast.makeText(this, "قطر رول ذخیره شد", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "خطا در ذخیره", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateAndShow() {
        try {
            val widthCm = inputWidthCm.text.toString().replace("[,\\s]".toRegex(), "").toDoubleOrNull() ?: 0.0
            val heightCm = inputHeightCm.text.toString().replace("[,\\s]".toRegex(), "").toDoubleOrNull() ?: 0.0
            val thicknessMm = FormatUtils.parseTomanInput(inputThicknessMm.text.toString()).toDouble()
            val coreMm = FormatUtils.parseTomanInput(inputCoreDiameterMm.text.toString()).toDouble()
            val packing = inputPackingFactor.text.toString().toDoubleOrNull() ?: 1.0

            if (widthCm <= 0.0 || heightCm <= 0.0) {
                Toast.makeText(this, "عرض و ارتفاع معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                return
            }
            if (thicknessMm <= 0.0) {
                Toast.makeText(this, "ضخامت تیغه معتبر نیست", Toast.LENGTH_SHORT).show()
                return
            }
            if (coreMm <= 0.0) {
                Toast.makeText(this, "قطر هسته معتبر نیست", Toast.LENGTH_SHORT).show()
                return
            }
            // convert units
            val widthM = widthCm / 100.0
            val heightM = heightCm / 100.0
            val thicknessM = (thicknessMm / 1000.0) * packing
            val r0_m = (coreMm / 1000.0) / 2.0

            // V = width * height * thickness (m^3)
            val V = widthM * heightM * thicknessM

            // R^2 = r0^2 + V / (π * width)
            val R2 = r0_m * r0_m + (V / (PI * widthM))

            val Rout_m = sqrt(R2)
            val Dout_mm = Rout_m * 2.0 * 1000.0

            // show results with one decimal
            textResultDiameter.text = String.format("قطر رول تقریبی: %.1f mm", Dout_mm)

            // show minor info: inner radius and outer radius in mm
            val inner = r0_m * 1000.0
            val outer = Dout_mm / 2.0
            textResultDiameter.append("\nشعاع داخلی: %.1f mm  |  شعاع بیرونی: %.1f mm".format(inner, outer))

            // warn if too large
            val warnLimit = PrefsHelper.getFloat(this, "max_allowed_roll_diameter_mm")
            if (warnLimit > 0f && Dout_mm > warnLimit) {
                Toast.makeText(this, "هشدار: قطر بیشتر از حد مجاز است (${warnLimit.toInt()} mm)", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "خطا در محاسبه", Toast.LENGTH_SHORT).show()
        }
    }
}
