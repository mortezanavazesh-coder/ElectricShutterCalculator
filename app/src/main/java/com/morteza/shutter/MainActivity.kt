package com.morteza.shutter

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    lateinit var inputWidth: EditText
    lateinit var inputHeight: EditText
    lateinit var areaResult: TextView
    lateinit var bladeSpinner: Spinner
    lateinit var bladePrice: TextView
    lateinit var motorSpinner: Spinner
    lateinit var motorPrice: TextView
    lateinit var shaftSpinner: Spinner
    lateinit var shaftLength: EditText
    lateinit var shaftPrice: TextView
    lateinit var boxEnabled: CheckBox
    lateinit var boxSpinner: Spinner
    lateinit var boxLength: EditText
    lateinit var boxPrice: TextView
    lateinit var installPrice: EditText
    lateinit var weldPrice: EditText
    lateinit var transportPrice: EditText
    lateinit var lockElectric: CheckBox
    lateinit var lockManual: CheckBox
    lateinit var motorCover: CheckBox
    lateinit var rollDiameter: TextView
    lateinit var finalPrice: TextView
    lateinit var basePriceButton: Button
    lateinit var rollPageButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputWidth = findViewById(R.id.inputWidth)
        inputHeight = findViewById(R.id.inputHeight)
        areaResult = findViewById(R.id.areaResult)
        bladeSpinner = findViewById(R.id.bladeSpinner)
        bladePrice = findViewById(R.id.bladePrice)
        motorSpinner = findViewById(R.id.motorSpinner)
        motorPrice = findViewById(R.id.motorPrice)
