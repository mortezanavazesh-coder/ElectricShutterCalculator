package com.morteza.shutter

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class BasePriceActivity : AppCompatActivity() {

    lateinit var bladeName: EditText
    lateinit var bladeThickness: EditText
    lateinit var bladeHeight: EditText
    lateinit var bladePrice: EditText
    lateinit var addBlade: Button

    lateinit var motorName: EditText
    lateinit var motorPrice: EditText
    lateinit var addMotor: Button

    lateinit var shaftName: EditText
    lateinit var shaftDiameter: EditText
    lateinit var shaftPrice: EditText
    lateinit var addShaft: Button

    lateinit var boxName: EditText
    lateinit var boxPrice: EditText
    lateinit var addBox: Button

    lateinit var installBasePrice: EditText
    lateinit var weldBasePrice: EditText
    lateinit var transportBasePrice: EditText

    lateinit var optionName: EditText
    lateinit var optionPrice: EditText
    lateinit var addOption: Button

    lateinit var saveBasePrices: Button
    lateinit var backToMain: Button

    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_price)

        prefs = getSharedPreferences("shutterPrefs", Context.MODE_PRIVATE)

        bladeName = findViewById(R.id.bladeName)
        bladeThickness = findViewById(R.id.bladeThickness)
        bladeHeight = findViewById(R.id.bladeHeight)
        bladePrice = findViewById(R.id.bladePrice)
        addBlade = findViewById(R.id.addBlade)

        motorName = findViewById(R.id.motorName)
        motorPrice = findViewById(R.id.motorPrice)
        addMotor = findViewById(R.id.addMotor)

        shaftName = findViewById(R.id.shaftName)
        shaftDiameter = findViewById(R.id.shaftDiameter)
        shaftPrice = findViewById(R.id.shaftPrice)
        addShaft = findViewById(R.id.addShaft)

        boxName = findViewById(R.id.boxName)
        boxPrice = findViewById(R.id.boxPrice)
        addBox = findViewById(R.id.addBox)

        installBasePrice = findViewById(R.id.installBasePrice)
        weldBasePrice = findViewById(R.id.weldBasePrice)
        transportBasePrice = findViewById(R.id.transportBasePrice)

        optionName = findViewById(R.id.optionName)
        optionPrice = findViewById(R.id.optionPrice)
        addOption = findViewById(R.id.addOption)

        saveBasePrices = findViewById(R.id.saveBasePrices)
        backToMain = findViewById(R.id.backToMain)

        addBlade.setOnClickListener {
            val name = bladeName.text.toString()
            val thickness = bladeThickness.text.toString()
            val height = bladeHeight.text.toString()
            val price = bladePrice.text.toString()
            prefs.edit().putString("blade_$name", "$thickness|$height|$price").apply()
        }

        addMotor.setOnClickListener {
            val name = motorName.text.toString()
            val price = motorPrice.text.toString()
            prefs.edit().putString("motor_$name", price).apply()
        }

        addShaft.setOnClickListener {
            val name = shaftName.text.toString()
            val diameter = shaftDiameter.text.toString()
            val price = shaftPrice.text.toString()
            prefs.edit().putString("shaft_$name", "$diameter|$price").apply()
        }

        addBox.setOnClickListener {
            val name = boxName.text.toString()
            val price = boxPrice.text.toString()
            prefs.edit().putString("box_$name", price).apply()
        }

        addOption.setOnClickListener {
            val name = optionName.text.toString()
            val price = optionPrice.text.toString()
            prefs.edit().putString("option_$name", price).apply()
        }

        saveBasePrices.setOnClickListener {
            prefs.edit()
                .putString("install_base", installBasePrice.text.toString())
                .putString("weld_base", weldBasePrice.text.toString())
                .putString("transport_base", transportBasePrice.text.toString())
                .apply()
            Toast.makeText(this, "ذخیره شد", Toast.LENGTH_SHORT).show()
        }

        backToMain.setOnClickListener {
            finish()
        }
    }
}
