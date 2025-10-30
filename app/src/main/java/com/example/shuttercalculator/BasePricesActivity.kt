package com.example.shuttercalculator

import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class BasePricesActivity : AppCompatActivity() {

    private lateinit var bladePriceInput: EditText
    private lateinit var motorPriceInput: EditText
    private lateinit var shaftPriceInput: EditText
    private lateinit var tubePriceInput: EditText
    private lateinit var installPriceInput: EditText
    private lateinit var weldingPriceInput: EditText
    private lateinit var transportPriceInput: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_prices)

        bladePriceInput = findViewById(R.id.bladePriceInput)
        motorPriceInput = findViewById(R.id.motorPriceInput)
        shaftPriceInput = findViewById(R.id.shaftPriceInput)
        tubePriceInput = findViewById(R.id.tubePriceInput)
        installPriceInput = findViewById(R.id.installPriceInput)
        weldingPriceInput = findViewById(R.id.weldingPriceInput)
        transportPriceInput = findViewById(R.id.transportPriceInput)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)

        val prefs = getSharedPreferences("base_prices", Context.MODE_PRIVATE)

        saveButton.setOnClickListener {
            prefs.edit().apply {
                putFloat("blade", bladePriceInput.text.toString().toFloatOrNull() ?: 0f)
                putFloat("motor", motorPriceInput.text.toString().toFloatOrNull() ?: 0f)
                putFloat("shaft", shaftPriceInput.text.toString().toFloatOrNull() ?: 0f)
                putFloat("tube", tubePriceInput.text.toString().toFloatOrNull() ?: 0f)
                putFloat("install", installPriceInput.text.toString().toFloatOrNull() ?: 0f)
                putFloat("welding", weldingPriceInput.text.toString().toFloatOrNull() ?: 0f)
                putFloat("transport", transportPriceInput.text.toString().toFloatOrNull() ?: 0f)
                apply()
            }
            Toast.makeText(this, "قیمت‌ها ذخیره شدند", Toast.LENGTH_SHORT).show()
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}
