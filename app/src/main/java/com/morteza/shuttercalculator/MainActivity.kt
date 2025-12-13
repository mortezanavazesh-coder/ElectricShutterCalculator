package com.morteza.shuttercalculator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.morteza.shuttercalculator.utils.FormatUtils
import com.morteza.shuttercalculator.utils.PrefsHelper
import com.morteza.shuttercalculator.utils.ReportStorage
import com.morteza.shuttercalculator.utils.ThousandSeparatorTextWatcher
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    private lateinit var vm: MainViewModel

    // ورودی‌ها
    private lateinit var inputHeightCm: EditText
    private lateinit var inputWidthCm: EditText

    // نمایش‌ها
    private lateinit var textAreaM2: TextView

    // تیغه
    private lateinit var spinnerBlade: Spinner
    private lateinit var textBladeLine: TextView

    // موتور
    private lateinit var spinnerMotor: Spinner
    private lateinit var textMotorLine: TextView

    // شفت
    private lateinit var spinnerShaft: Spinner
    private lateinit var textShaftLine: TextView

    // قوطی
    private lateinit var checkboxBoxEnabled: CheckBox
    private lateinit var spinnerBox: Spinner
    private lateinit var textBoxLine: TextView

    // هزینه‌ها
    private lateinit var inputInstallPrice: EditText
    private lateinit var inputWeldingPrice: EditText
    private lateinit var inputTransportPrice: EditText
    private lateinit var textInstallComputed: TextView

    // گزینه‌های اضافی
    private lateinit var extrasContainer: LinearLayout

    // ریز محاسبات
    private lateinit var textBreakBlade: TextView
    private lateinit var textBreakMotor: TextView
    private lateinit var textBreakShaft: TextView
    private lateinit var textBreakBox: TextView
    private lateinit var textBreakInstall: TextView
    private lateinit var textBreakWelding: TextView
    private lateinit var textBreakTransport: TextView
    private lateinit var textBreakExtras: TextView

    // جمع کل
    private lateinit var textTotal: TextView

    // دکمه‌ها
    private lateinit var buttonSaveReport: Button
    private lateinit var buttonBasePrice: Button
    private lateinit var buttonRollDiameter: Button
    private lateinit var buttonReports: Button

    // نگهداری آخرین نرخ نصب معتبر
    private var previousInstallBase: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vm = ViewModelProvider(this).get(MainViewModel::class.java)

        bindViews()
        setupTextWatchers()
        setupSpinners()
        setupButtons()

        vm.basePrices.observe(this) { bp ->
            spinnerBlade.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.blades)
                .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            spinnerMotor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.motors)
                .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            spinnerShaft.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.shafts)
                .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            spinnerBox.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.boxes)
                .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            inputInstallPrice.setText(FormatUtils.formatTomanPlain(bp.installBase))
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

        inputInstallPrice = findViewById(R.id.inputInstallPrice)
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

        buttonSaveReport = findViewById(R.id.buttonSaveReport)
        buttonBasePrice = findViewById(R.id.buttonBasePrice)
        buttonRollDiameter = findViewById(R.id.buttonRollDiameter)
        buttonReports = findViewById(R.id.buttonReports)

        inputInstallPrice.addTextChangedListener(ThousandSeparatorTextWatcher(inputInstallPrice))
        inputWeldingPrice.addTextChangedListener(ThousandSeparatorTextWatcher(inputWeldingPrice))
        inputTransportPrice.addTextChangedListener(ThousandSeparatorTextWatcher(inputTransportPrice))
    }

    private fun setupButtons() {
        buttonSaveReport.setOnClickListener {
    val view = LayoutInflater.from(this).inflate(R.layout.dialog_save_report, null)
    val etName = view.findViewById<EditText>(R.id.etCustomerName)
    val etPhone = view.findViewById<EditText>(R.id.etCustomerPhone)

    AlertDialog.Builder(this)
        .setTitle("ذخیره گزارش")
        .setView(view)
        .setPositiveButton("ذخیره") { dialog, _ ->
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "نام مشتری الزامی است", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val today = sdf.format(Date())

            val report = ReportModel(
                id = ReportStorage.generateId().toString(), // ← تبدیل Long به String
                customerName = name,
                customerPhone = phone, // شماره موبایل اختیاری
                date = today,
                height = inputHeightCm.text.toString().toFloatOrNull() ?: 0f,
                width = inputWidthCm.text.toString().toFloatOrNull() ?: 0f,
                area = extractAreaFloat(textAreaM2.text.toString()),
                blade = spinnerBlade.selectedItem?.toString() ?: "-",
                motor = spinnerMotor.selectedItem?.toString() ?: "-",
                shaft = spinnerShaft.selectedItem?.toString() ?: "-",
                box = if (checkboxBoxEnabled.isChecked) spinnerBox.selectedItem?.toString() ?: "-" else "محاسبه نشده",
                install = FormatUtils.parseTomanInput(textInstallComputed.text.toString()),
                welding = FormatUtils.parseTomanInput(inputWeldingPrice.text.toString()),
                transport = FormatUtils.parseTomanInput(inputTransportPrice.text.toString()),
                extras = extractExtrasFloat(textBreakExtras.text.toString()),
                total = FormatUtils.parseTomanInput(textTotal.text.toString())
            )
            ReportStorage.saveReport(this, report)
            Toast.makeText(this, "گزارش ذخیره شد ✅", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        .setNegativeButton("لغو", null)
        .show()
}

        buttonBasePrice.setOnClickListener {
            startActivity(Intent(this, BasePriceActivity::class.java))
        }
        buttonRollDiameter.setOnClickListener {
            startActivity(Intent(this, RollCalculatorActivity::class.java))
        }
        buttonReports.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }
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

        // اعتبارسنجی نرخ نصب و ذخیره
        inputInstallPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = FormatUtils.parseTomanInput(s?.toString())
                if (v <= 0f) {
                    Toast.makeText(this@MainActivity, "نرخ نصب باید بزرگتر از صفر باشد", Toast.LENGTH_SHORT).show()
                    val desired = FormatUtils.formatTomanPlain(previousInstallBase)
                    if (inputInstallPrice.text.toString() != desired) {
                        inputInstallPrice.post {
                            inputInstallPrice.setText(desired)
                            inputInstallPrice.setSelection(desired.length)
                        }
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
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
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
        // ابعاد و مساحت
        val heightCm = max(0.0, parseDoubleSafe(inputHeightCm.text?.toString()))
        val widthCm = max(0.0, parseDoubleSafe(inputWidthCm.text?.toString()))
        val areaM2 = (widthCm * heightCm) / 10000.0
        textAreaM2.text = String.format("مساحت: %.3f متر مربع", areaM2)

        // تیغه
        var bladeComputed = 0.0
        var bladeBase = 0f
        if (spinnerBlade.adapter != null && spinnerBlade.adapter.count > 0) {
            val bladeName = spinnerBlade.selectedItem as? String
            bladeBase = if (bladeName != null) PrefsHelper.getFloat(this, "تیغه_price_$bladeName", 0f) else 0f
            bladeComputed = areaM2 * bladeBase
            textBladeLine.text = "تیغه — قیمت پایه: ${FormatUtils.formatToman(bladeBase)}  |  قیمت کل: ${FormatUtils.formatToman(bladeComputed.toFloat())}"
        } else {
            textBladeLine.text = "تیغه — داده‌ای موجود نیست"
        }

        // موتور
        var motorBase = 0f
        if (spinnerMotor.adapter != null && spinnerMotor.adapter.count > 0) {
            val motorName = spinnerMotor.selectedItem as? String
            motorBase = if (motorName != null) PrefsHelper.getFloat(this, "موتور_price_$motorName", 0f) else 0f
            textMotorLine.text = "موتور — قیمت: ${FormatUtils.formatToman(motorBase)}"
        } else {
            textMotorLine.text = "موتور — داده‌ای موجود نیست"
        }

        // شفت
        var shaftComputed = 0.0
        var shaftBase = 0f
        if (spinnerShaft.adapter != null && spinnerShaft.adapter.count > 0) {
            val shaftName = spinnerShaft.selectedItem as? String
            shaftBase = if (shaftName != null) PrefsHelper.getFloat(this, "شفت_price_$shaftName", 0f) else 0f
            val widthM = widthCm / 100.0
            shaftComputed = shaftBase * widthM
            textShaftLine.text = "شفت — قیمت پایه: ${FormatUtils.formatToman(shaftBase)}  |  قیمت کل: ${FormatUtils.formatToman(shaftComputed.toFloat())}"
        } else {
            textShaftLine.text = "شفت — داده‌ای موجود نیست"
        }

        // قوطی
        var boxComputedValue = 0.0
        if (checkboxBoxEnabled.isChecked && spinnerBox.adapter != null && spinnerBox.adapter.count > 0) {
            val boxName = spinnerBox.selectedItem as? String
            val boxBase = if (boxName != null) PrefsHelper.getFloat(this, "قوطی_price_$boxName", 0f) else 0f
            val effectiveHeight = max(0.0, heightCm - 30.0) // کم کردن فضای آزاد
            val units = (effectiveHeight * 2.0) / 100.0      // دو خط عمودی به متر
            boxComputedValue = units * boxBase
            textBoxLine.text = "قوطی — قیمت پایه: ${FormatUtils.formatToman(boxBase)}  |  قیمت کل: ${FormatUtils.formatToman(boxComputedValue.toFloat())}"
        } else {
            textBoxLine.text = "قوطی — محاسبه نشده"
        }

        // نصب / جوشکاری / حمل
        val installRate = FormatUtils.parseTomanInput(inputInstallPrice.text?.toString()).toDouble()
        val installComputed = when {
            areaM2 == 0.0 -> installRate                      // اگر مساحت نامعتبر است، همان نرخ پایه
            areaM2 in 2.0..10.0 -> installRate * 10.0         // برای بازه 2 تا 10
            areaM2 > 10.0 -> installRate * areaM2             // تناسبی با مساحت
            else -> installRate * 10.0                        // برای 0 < area < 2
        }
        textInstallComputed.text = FormatUtils.formatToman(installComputed.toFloat())

        val weldingComputed = FormatUtils.parseTomanInput(inputWeldingPrice.text?.toString()).toDouble()
        val transportComputed = FormatUtils.parseTomanInput(inputTransportPrice.text?.toString()).toDouble()

        // مجموع گزینه‌های اضافی
        val extras = PrefsHelper.getAllExtraOptions(this)
        var extrasTotal = 0.0
        for ((name, priceF) in extras) {
            val enabled = PrefsHelper.getBool(this, "extra_enabled_$name")
            if (enabled) extrasTotal += priceF.toDouble()
        }

        // نمایش ریزمحاسبات
        textBreakBlade.text = "جمع تیغه: ${FormatUtils.formatToman(bladeComputed.toFloat())}"
        textBreakMotor.text = "موتور: ${FormatUtils.formatToman(motorBase)}"
        textBreakShaft.text = "جمع شفت: ${FormatUtils.formatToman(shaftComputed.toFloat())}"
        textBreakBox.text = "جمع قوطی: ${FormatUtils.formatToman(boxComputedValue.toFloat())}"
        textBreakInstall.text = "نصب: ${FormatUtils.formatToman(installComputed.toFloat())}"
        textBreakWelding.text = "جوشکاری: ${FormatUtils.formatToman(weldingComputed.toFloat())}"
        textBreakTransport.text = "کرایه حمل: ${FormatUtils.formatToman(transportComputed.toFloat())}"
        textBreakExtras.text = "گزینه‌های اضافی: ${FormatUtils.formatToman(extrasTotal.toFloat())}"

        // جمع کل
        val total = bladeComputed + motorBase + shaftComputed + boxComputedValue + installComputed + weldingComputed + transportComputed + extrasTotal
        textTotal.text = "قیمت نهایی: ${FormatUtils.formatToman(total.toFloat())}"
    }

    private fun extractAreaFloat(areaText: String): Float {
        // ورودی مثل: "مساحت: 2.345 متر مربع"
        return areaText
            .replace("مساحت:", "")
            .replace("متر مربع", "")
            .trim()
            .toFloatOrNull() ?: 0f
    }

    private fun extractExtrasFloat(extrasText: String): Float {
        // ورودی مثل: "گزینه‌های اضافی: 120,000 تومان"
        return FormatUtils.parseTomanInput(extrasText)
    }
}


