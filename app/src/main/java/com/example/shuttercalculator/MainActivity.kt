package com.example.shuttercalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var widthInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var areaText: TextView
    private lateinit var bladeSpinner: Spinner
    private lateinit var motorSpinner: Spinner
    private lateinit var calculateButton: Button
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        widthInput = findViewById(R.id.widthInput)
        heightInput = findViewById(R.id.heightInput)
        areaText = findViewById(R.id.areaText)
        bladeSpinner = findViewById(R.id.bladeSpinner)
        motorSpinner = findViewById(R.id.motorSpinner)
        calculateButton = findViewById(R.id.calculateButton)
        resultText = findViewById(R.id.resultText)

        calculateButton.setOnClickListener {
            val width = parseInput(widthInput.text.toString())
            val height = parseInput(heightInput.text.toString())
            val area = width * height
            areaText.text = "مساحت: $area متر مربع"

            val bladePrice = 500.0 // فرضی
            val motorPrice = 800.0 // فرضی
            val shutterPrice = area * bladePrice
            val totalPrice = shutterPrice + motorPrice

            resultText.text = "قیمت نهایی: $totalPrice تومان"
        }
		val basePricesButton = findViewById<Button>(R.id.basePricesButton)
        val rollDiameterButton = findViewById<Button>(R.id.rollDiameterButton)

        basePricesButton.setOnClickListener {
         startActivity(Intent(this, BasePricesActivity::class.java))
         }

         rollDiameterButton.setOnClickListener {
         startActivity(Intent(this, RollDiameterActivity::class.java))
         }

    }

    private fun parseInput(input: String): Double {
        return if (input.contains("cm")) {
            input.replace("cm", "").trim().toDouble() / 100
        } else {
            input.replace("m", "").trim().toDouble()
        }
    }
}
