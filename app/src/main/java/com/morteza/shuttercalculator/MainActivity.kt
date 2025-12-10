package com.morteza.shuttercalculator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.morteza.shuttercalculator.utils.FormatUtils
import com.morteza.shuttercalculator.utils.PrefsHelper
import com.morteza.shuttercalculator.utils.ThousandSeparatorTextWatcher
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    // ViewModel
    private lateinit var vm: MainViewModel

    // inputs (dimensions)
    private lateinit var inputHeightCm: EditText
    private lateinit var inputWidthCm: EditText

    // derived displays
    private lateinit var textAreaM2: TextView

    // blade
    private lateinit var spinnerBlade: Spinner
    private lateinit var textBladeLine: TextView

    // motor
    private lateinit var spinnerMotor: Spinner
    private lateinit var textMotorLine: TextView

    // shaft
    private lateinit var spinnerShaft: Spinner
    private lateinit var textShaftLine: TextView

    // box (قوطی)
    private lateinit var checkboxBoxEnabled: CheckBox
    private lateinit var spinnerBox: Spinner
    private lateinit var textBoxLine: TextView

    // editable base cost fields
    private lateinit var inputInstallPriceBase: EditText
    private lateinit var inputWeldingPrice: EditText
    private lateinit var inputTransportPrice: EditText

    // computed install (non-editable)
    private lateinit var textInstallComputed: TextView

    // extras (checkboxes container)
    private lateinit var extrasContainer: LinearLayout

    // breakdown views
    private lateinit var textBreakBlade: TextView
    private lateinit var textBreakMotor: TextView
    private lateinit var textBreakShaft: TextView
    private lateinit var textBreakBox: TextView
    private lateinit var textBreakInstall: TextView
    private lateinit var textBreakWelding: TextView
    private lateinit var textBreakTransport: TextView
    private lateinit var textBreakExtras: TextView

    // final total
    private lateinit var textTotal: TextView

    // buttons
    private lateinit var buttonBasePrice: Button
    private lateinit var buttonRollDiameter: Button
    private lateinit var buttonReports: Button

    // keep previous valid install base
    private var previousInstallBase: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vm = ViewModelProvider(this).get(MainViewModel::class.java)

        bindViews()
        setupTextWatchers()
        setupSpinners()
        setupButtons()

        // observe base prices and populate UI
        vm.basePrices.observe(this) { bp ->
            spinnerBlade.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.blades)
                .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            spinnerMotor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.motors)
                .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            spinnerShaft.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.shafts)
                .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            spinnerBox.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.boxes)
                .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            inputInstallPriceBase.setText(FormatUtils.formatTomanPlain(bp.installBase))
            inputWeldingPrice.setText(FormatUtils.formatTomanPlain(bp.weldingBase))
            inputTransportPrice.setText(FormatUtils.formatTomanPlain(bp.transportBase))

            previousInstallBase = if (bp.installBase > 0f) bp.installBase else 0f

            buildExtrasCheckboxes(bp.extras)
            recalcAllAndDisplay()
        }
    }

    override fun onResume() {
        super.onResume()
        vm.reloadFromPrefs(this)
    }

    private fun bindViews() {
        inputHeightCm = findViewById(R.id.inputHeightCm)
        inputWidthCm = findViewById(R.id.inputWidthCm)
        textAreaM2 = findViewById(R.id.textAreaM2)

        spinnerBlade = findViewById(R.id.spinnerBlade)
        textBladeLine = findViewById(R.id.textBladeLine)

        spinnerMotor = findViewById(R.id.spinnerMotor)
        textMotorLine = findViewById(R.id.textMotorLine)

        spinnerShaft = findViewById(R.id.spinnerShaft)
        textShaftLine = findViewById(R.id.textShaftLine)

        checkboxBoxEnabled = findViewById(R.id.checkboxBoxEnabled)
        spinnerBox = findViewById(R.id.spinnerBox)
        textBoxLine = findViewById(R.id.textBoxLine)

        inputInstallPriceBase = findViewById(R.id.inputInstallPrice)
        inputWeldingPrice = findViewById(R.id.inputWeldingPrice)
        inputTransportPrice = findViewById(R.id.inputTransportPrice)
        textInstallComputed = findViewById(R.id.textInstallComputed)

        extrasContainer = findViewById(R.id.extrasContainer)

        textBreakBlade = findViewById(R.id.textBreakBlade)
        textBreakMotor = findViewById(R.id.textBreakMotor)
        textBreakShaft = findViewById(R.id.textBreakShaft)
        textBreakBox = findViewById(R.id.textBreakBox)
        textBreakInstall = findViewById(R.id.textBreakInstall)
        textBreakWelding = findViewById(R.id.textBreakWelding)
        textBreakTransport = findViewById(R.id.textBreakTransport)
        textBreakExtras = findViewById(R.id.textBreakExtras)

        textTotal = findViewById(R.id.textTotal)

        buttonBasePrice = findViewById(R.id.buttonBasePrice)
        buttonRollDiameter = findViewById(R.id.buttonRollDiameter)
        buttonReports = findViewById(R.id.buttonReports)

        // format helpers for editable numeric fields
        inputInstallPriceBase.addTextChangedListener(ThousandSeparatorTextWatcher(inputInstallPriceBase))
        inputWeldingPrice.addTextChangedListener(ThousandSeparatorTextWatcher(inputWeldingPrice))
        inputTransportPrice.addTextChangedListener(ThousandSeparatorTextWatcher(inputTransportPrice))
    }

    private fun setupTextWatchers() {
        val recomputeTrigger = { recalcAllAndDisplay() }
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { recomputeTrigger.invoke() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        inputHeightCm.addTextChangedListener(watcher)
        inputWidthCm.addTextChangedListener(watcher)

        // install base editing with validation and persistence
        inputInstallPriceBase.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = FormatUtils.parseTomanInput(s?.toString())
                if (v <= 0f) {
                    Toast.makeText(this@MainActivity, "نرخ نصب باید بزرگتر از صفر باشد", Toast.LENGTH_SHORT).show()
                    inputInstallPriceBase.post {
                        inputInstallPriceBase.setText(FormatUtils.formatTomanPlain(previousInstallBase))
                    }
                    return
                }
                previousInstallBase = v
                PrefsHelper.saveFloat(this@MainActivity, "install_base", v)
                recalcAllAndDisplay()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputWeldingPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = FormatUtils.parseTomanInput(s?.toString())
                PrefsHelper.saveFloat(this@MainActivity, "welding_base", v)
                recalcAllAndDisplay()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputTransportPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = FormatUtils.parseTomanInput(s?.toString())
                PrefsHelper.saveFloat(this@MainActivity, "transport_base", v)
                recalcAllAndDisplay()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupSpinners() {
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                recalcAllAndDisplay()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerBlade.onItemSelectedListener = listener
        spinnerMotor.onItemSelectedListener = listener
        spinnerShaft.onItemSelectedListener = listener
        spinnerBox.onItemSelectedListener = listener

        checkboxBoxEnabled.setOnCheckedChangeListener { _, _ -> recalcAllAndDisplay() }
    }

    private fun setupButtons() {
	// ذخیره گزارش
         findViewById<Button>(R.id.buttonSaveReport).setOnClickListener {
             val report = ReportModel(
                 id = ReportStorage.generateId(),
                 customerName = "مشتری ناشناس", // می‌تونی بعداً ورودی نام مشتری بذاری
                 date = FormatUtils.getTodayDate(),
                 height = FormatUtils.parseTomanInput(inputHeightCm.text.toString()),
                 width = FormatUtils.parseTomanInput(inputWidthCm.text.toString()),
                 area = textAreaM2.text.toString(),
                 blade = spinnerBlade.selectedItem?.toString() ?: "-",
                 motor = spinnerMotor.selectedItem?.toString() ?: "-",
                 shaft = spinnerShaft.selectedItem?.toString() ?: "-",
                 box = if (checkboxBoxEnabled.isChecked) spinnerBox.selectedItem?.toString() ?: "-" else "محاسبه نشده",
                 install = textInstallComputed.text.toString(),
                 welding = inputWeldingPrice.text.toString(),
                 transport = inputTransportPrice.text.toString(),
                 extras = textBreakExtras.text.toString(),
                 total = textTotal.text.toString()
              )

             ReportStorage.saveReport(this, report)
             Toast.makeText(this, "گزارش ذخیره شد ✅", Toast.LENGTH_SHORT).show()
          }

        // قیمت پایه
        buttonBasePrice.setOnClickListener {
            startActivity(Intent(this, BasePriceActivity::class.java))
        }
        // قطر رول
        buttonRollDiameter.setOnClickListener {
            startActivity(Intent(this, RollCalculatorActivity::class.java))
        }
        // گزارش‌ها
        buttonReports.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }
    }

    private fun buildExtrasCheckboxes(extras: Map<String, Float>) {
        extrasContainer.removeAllViews()
        if (extras.isEmpty()) return
        val sorted = extras.keys.sortedWith(String.CASE_INSENSITIVE_ORDER)
        for (name in sorted) {
            val cb = CheckBox(this)
            cb.text = "$name  (${FormatUtils.formatToman(extras[name] ?: 0f)})"
            cb.isChecked = PrefsHelper.getBool(this, "extra_enabled_$name")
            cb.setOnCheckedChangeListener { _, isChecked ->
                PrefsHelper.saveBool(this, "extra_enabled_$name", isChecked)
                recalcAllAndDisplay()
            }
            extrasContainer.addView(cb)
        }
    }

    private fun parseDoubleSafe(s: String?): Double {
        return try { s?.toDouble() ?: 0.0 } catch (e: Exception) { 0.0 }
    }

    private fun recalcAllAndDisplay() {
        // dimensions
        val heightCm = max(0.0, parseDoubleSafe(inputHeightCm.text.toString()))
        val widthCm = max(0.0, parseDoubleSafe(inputWidthCm.text.toString()))
        val areaM2 = (widthCm * heightCm) / 10000.0
        textAreaM2.text = String.format("مساحت: %.3f متر مربع", areaM2)

        // blade
        val bladeName = spinnerBlade.selectedItem as? String
        val bladeBase = if (bladeName != null) PrefsHelper.getFloat(this, "تیغه_price_$bladeName") else 0f
        val bladeComputed = areaM2 * bladeBase
        textBladeLine.text = "تیغه — قیمت پایه: ${FormatUtils.formatToman(bladeBase)}  |  قیمت کل: ${FormatUtils.formatToman(bladeComputed.toFloat())}"

        // motor
        val motorName = spinnerMotor.selectedItem as? String
        val motorBase = if (motorName != null) PrefsHelper.getFloat(this, "موتور_price_$motorName") else 0f
        textMotorLine.text = "موتور — قیمت: ${FormatUtils.formatToman(motorBase)}"

        // shaft
        val shaftName = spinnerShaft.selectedItem as? String
        val shaftBase = if (shaftName != null) PrefsHelper.getFloat(this, "شفت_price_$shaftName") else 0f
        val widthM = widthCm / 100.0
        val shaftComputed = shaftBase * widthM
        textShaftLine.text = "شفت — قیمت پایه: ${FormatUtils.formatToman(shaftBase)}  |  قیمت کل: ${FormatUtils.formatToman(shaftComputed.toFloat())}"

        // box
        val boxComputedValue = if (checkboxBoxEnabled.isChecked) {
            val boxName = spinnerBox.selectedItem as? String
            val boxBase = if (boxName != null) PrefsHelper.getFloat(this, "قوطی_price_$boxName") else 0f
            val effectiveHeight = max(0.0, heightCm - 30.0)
            val units = (effectiveHeight * 2.0) / 100.0
            val computed = units * boxBase
            textBoxLine.text = "قوطی — قیمت پایه: ${FormatUtils.formatToman(boxBase)}  |  قیمت کل: ${FormatUtils.formatToman(computed.toFloat())}"
            computed
        } else {
            textBoxLine.text = "قوطی — محاسبه نشده"
            0.0
        }

        // install / welding / transport
        val installRate = FormatUtils.parseTomanInput(inputInstallPriceBase.text.toString()).toDouble()
        val installComputed = when {
            areaM2 == 0.0 -> installRate
            areaM2 in 2.0..10.0 -> installRate * 10.0
            areaM2 > 10.0 -> installRate * areaM2
            else -> installRate * 10.0 // برای 0 < area < 2
        }
        textInstallComputed.text = FormatUtils.formatToman(installComputed.toFloat())

        val weldingComputed = FormatUtils.parseTomanInput(inputWeldingPrice.text.toString()).toDouble()
        val transportComputed = FormatUtils.parseTomanInput(inputTransportPrice.text.toString()).toDouble()

        // extras total
        val extras = PrefsHelper.getAllExtraOptions(this)
        var extrasTotal = 0.0
        for ((name, priceF) in extras) {
            val enabled = PrefsHelper.getBool(this, "extra_enabled_$name")
            if (enabled) extrasTotal += priceF.toDouble()
        }

        // breakdown display
        textBreakBlade.text = "جمع تیغه: ${FormatUtils.formatToman(bladeComputed.toFloat())}"
        textBreakMotor.text = "موتور: ${FormatUtils.formatToman(motorBase)}"
        textBreakShaft.text = "جمع شفت: ${FormatUtils.formatToman(shaftComputed.toFloat())}"
        textBreakBox.text = "جمع قوطی: ${FormatUtils.formatToman(boxComputedValue.toFloat())}"
        textBreakInstall.text = "نصب: ${FormatUtils.formatToman(installComputed.toFloat())}"
        textBreakWelding.text = "جوشکاری: ${FormatUtils.formatToman(weldingComputed.toFloat())}"
        textBreakTransport.text = "کرایه حمل: ${FormatUtils.formatToman(transportComputed.toFloat())}"
        textBreakExtras.text = "گزینه‌های اضافی: ${FormatUtils.formatToman(extrasTotal.toFloat())}"

        val total = bladeComputed + motorBase + shaftComputed + boxComputedValue + installComputed + weldingComputed + transportComputed + extrasTotal
        textTotal.text = "قیمت نهایی: ${FormatUtils.formatToman(total.toFloat())}"
    }
}
