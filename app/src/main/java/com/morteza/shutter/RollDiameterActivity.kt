package com.morteza.shutter

import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt

class RollDiameterActivity : AppCompatActivity() {

    lateinit var inputHeight: EditText
    lateinit var bladeSpinner: Spinner
    lateinit var bladeInfo: TextView
    lateinit var bladeCount: TextView
    lateinit var rollResult: TextView
    lateinit var calculateRoll: Button
    lateinit var backToMain: Button

    lateinit var prefs: android.content.SharedPreferences
    val bladeMap = mutableMapOf<String, Triple<Double, Double, Double>>() // name → thickness, height, price

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roll_diameter)

        inputHeight = findViewById(R.id.inputHeight)
        bladeSpinner = findViewById(R.id.bladeSpinner)
        bladeInfo = findViewById(R.id.bladeInfo)
        bladeCount = findViewById(R.id.bladeCount)
        rollResult = findViewById(R.id.rollResult)
        calculateRoll = findViewById(R.id.calculateRoll)
        backToMain = findViewById(R.id.backToMain)

        prefs = getSharedPreferences("shutterPrefs", Context.MODE_PRIVATE)

        // Load blades
        val blades = prefs.all.filterKeys { it.startsWith("blade_") }
        val bladeNames = blades.keys.map { it.removePrefix("blade_") }
        bladeNames.forEach { name ->
            val parts = prefs.getString("blade_$name", "")!!.split("|")
            val thickness = parts[0].toDouble()
            val height = parts[1].toDouble()
            val price = parts[2].toDouble()
            bladeMap[name] = Triple(thickness, height, price)
        }

        bladeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bladeNames)

        calculateRoll.setOnClickListener {
            val h = inputHeight.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            val bladeName = bladeSpinner.selectedItem.toString()
            val (t, bladeHeight, _) = bladeMap[bladeName] ?: return@setOnClickListener

            val n = h / bladeHeight
            val diameter = sqrt((h + 40) * (h + 40) + (n * t) * (n * t))

            bladeInfo.text = "ضخامت: $t میلی‌متر | ارتفاع: $bladeHeight سانتی‌متر"
            bladeCount.text = "تعداد تیغه‌ها: ${"%.1f".format(n)}"
            rollResult.text = "قطر رول: ${"%.2f".format(diameter / 10)} سانتی‌متر"
        }

        backToMain.setOnClickListener {
            finish()
        }
    }
}
