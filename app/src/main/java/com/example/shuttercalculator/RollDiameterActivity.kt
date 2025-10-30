package com.example.shuttercalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RollDiameterActivity : AppCompatActivity() {

    private lateinit var shaftDiameterInput: EditText
    private lateinit var shutterHeightInput: EditText
    private lateinit var calculateButton: Button
    private lateinit var resultText: TextView
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roll_diameter)

        shaftDiameterInput = findViewById(R.id.shaftDiameterInput)
        shutterHeightInput = findViewById(R.id.shutterHeightInput)
        calculateButton = findViewById(R.id.calculateRollButton)
        resultText = findViewById(R.id.rollResultText)
        backButton = findViewById(R.id.backToMainButton)

        calculateButton.setOnClickListener {
            val shaft = shaftDiameterInput.text.toString().toDoubleOrNull() ?: 0.0
            val height = shutterHeightInput.text.toString().toDoubleOrNull() ?: 0.0

            // فرمول تقریبی قطر رول (قابل تغییر)
            val rollDiameter = Math.sqrt((height * 10000) / Math.PI + Math.pow(shaft / 2, 2.0)) * 2

            resultText.text = "قطر رول تقریبی: ${"%.1f".format(rollDiameter)} سانتی‌متر"
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}
