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
import kotlin.math.max
import saman.zamani.persiandate.PersianDate

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
    private var previousInstallBase: Long = 0L

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

            previousInstallBase = if (bp.installBase > 0L) bp.installBase else 0L

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

                    // تاریخ شمسی
                    val persianDate = PersianDate()
                    val today = persianDate.toString()

                    // ابعاد
                    val height = inputHeightCm.text.toString().toFloatOrNull() ?: 0f
                    val width = inputWidthCm.text.toString().toFloatOrNull() ?: 0f
                    val area = (height * width) / 10000f
                    textAreaM2.text = String.format("مساحت: %.3f متر مربع", area)

                    // تیغه
                    val bladeName = spinnerBlade.selectedItem?.toString() ?: "-"
                    val bladeBase = PrefsHelper.getLong(this, "تیغه_price_$bladeName", 0L)
                    val bladeTotal = (area * bladeBase).toLong()
                    textBladeLine.text = "تیغه — قیمت پایه: ${FormatUtils.formatToman(bladeBase)}  |  قیمت کل: ${FormatUtils.formatToman(bladeTotal)}"

                    // موتور
                    val motorName = spinnerMotor.selectedItem?.toString() ?: "-"
                    val motorBase = PrefsHelper.getLong(this, "موتور_price_$motorName", 0L)
                    val motorTotal = motorBase
                    textMotorLine.text = "موتور — قیمت: ${FormatUtils.formatToman(motorBase)}"

                    // شفت
                    val shaftName = spinnerShaft.selectedItem?.toString() ?: "-"
                    val shaftBase = PrefsHelper.getLong(this, "شفت_price_$shaftName", 0L)
                    val shaftTotal = (shaftBase * (width / 100f)).toLong()
                    textShaftLine.text = "شفت — قیمت پایه: ${FormatUtils.formatToman(shaftBase)}  |  قیمت کل: ${FormatUtils.formatToman(shaftTotal)}"

                    // قوطی
                    val boxName = if (checkboxBoxEnabled.isChecked) spinnerBox.selectedItem?.toString() ?: "-" else "محاسبه نشده"
                    val boxBase = if (checkboxBoxEnabled.isChecked) PrefsHelper.getLong(this, "قوطی_price_$boxName", 0L) else 0L
                    val effectiveHeight = if (height > 30f) height - 30f else 0f
                    val boxTotal = if (checkboxBoxEnabled.isChecked) (((effectiveHeight * 2f) / 100f) * boxBase).toLong() else 0L
                    textBoxLine.text = if (checkboxBoxEnabled.isChecked) {
                        "قوطی — قیمت پایه: ${FormatUtils.formatToman(boxBase)}  |  قیمت کل: ${FormatUtils.formatToman(boxTotal)}"
                    } else {
                        "قوطی — محاسبه نشده"
                    }

                    // هزینه‌های پایه
                    val installBase = FormatUtils.parseTomanInput(inputInstallPrice.text.toString())
                    val weldingBase = FormatUtils.parseTomanInput(inputWeldingPrice.text.toString())
                    val transportBase = FormatUtils.parseTomanInput(inputTransportPrice.text.toString())

                    // نصب محاسبه‌شده
                    val installTotal = when {
                        area == 0f -> installBase
                        area in 2f..10f -> installBase * 10L
                        area > 10f -> (installBase * area).toLong()
                        else -> installBase * 10L
                    }
                    textInstallComputed.text = FormatUtils.formatToman(installTotal)

                    // جوشکاری و حمل
                    val weldingTotal = weldingBase
                    val transportTotal = transportBase

                    // گزینه‌های اضافی
                    val extrasSelected = mutableListOf<ExtraOption>()
                    val extras = PrefsHelper.getAllExtraOptions(this) // Map<String, Float>
                    var extrasTotal = 0L
                    for ((exName, priceF) in extras) {
                        val enabled = PrefsHelper.getBool(this, "extra_enabled_$exName")
                        if (enabled) {
                            extrasSelected.add(ExtraOption(exName, priceF.toLong()))
                            extrasTotal += priceF.toLong()
                        }
                    }
                    textBreakExtras.text = "گزینه‌های اضافی: ${FormatUtils.formatToman(extrasTotal)}"

                    // ریز محاسبات
                    textBreakBlade.text = "جمع تیغه: ${FormatUtils.formatToman(bladeTotal)}"
                    textBreakMotor.text = "موتور: ${FormatUtils.formatToman(motorTotal)}"
                    textBreakShaft.text = "جمع شفت: ${FormatUtils.formatToman(shaftTotal)}"
                    textBreakBox.text = "جمع قوطی: ${FormatUtils.formatToman(boxTotal)}"
                    textBreakInstall.text = "نصب: ${FormatUtils.formatToman(installTotal)}"
                    textBreakWelding.text = "جوشکاری: ${FormatUtils.formatToman(weldingTotal)}"
                    textBreakTransport.text = "کرایه حمل: ${FormatUtils.formatToman(transportTotal)}"

                    // جمع کل
                    val total = bladeTotal + motorTotal + shaftTotal + boxTotal +
                            installTotal + weldingTotal + transportTotal + extrasTotal
                    if (total <= 0L) {
                        Toast.makeText(this, "جمع کل نامعتبر است، لطفاً ابتدا محاسبه را انجام دهید", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    textTotal.text = "قیمت نهایی: ${FormatUtils.formatToman(total)}"

                    // ساخت گزارش کامل
                    val report = ReportModel(
                        id = ReportStorage.generateId().toString(),
                        customerName = name,
                        customerPhone = phone,
                        date = today,
                        height = height,
                        width = width,
                        area = area,
                        bladeName = bladeName,
                        bladeBasePrice = bladeBase,
                        motorName = motorName,
                        motorBasePrice = motorBase,
                        shaftName = shaftName,
                        shaftBasePrice = shaftBase,
                        boxName = boxName,
                        boxBasePrice = boxBase,
                        installBasePrice = installBase,
                        weldingBasePrice = weldingBase,
                        transportBasePrice = transportBase,
                        extrasSelected = extrasSelected,
                        bladeTotal = bladeTotal,
                        motorTotal = motorTotal,
                        shaftTotal = shaftTotal,
                        boxTotal = boxTotal,
                        installTotal = installTotal,
                        weldingTotal = weldingTotal,
                        transportTotal = transportTotal,
                        extrasTotal = extrasTotal,
                        total = total
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

        // اعتبارسنجی نرخ نصب و ذخیره (Long)
        inputInstallPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = FormatUtils.parseTomanInput(s?.toString())
                if (v <= 0L) {
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
                PrefsHelper.saveLong(this@MainActivity, "install_base", v)
                recalcAllAndDisplay()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputWeldingPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = FormatUtils.parseTomanInput(s?.toString())
                PrefsHelper.saveLong(this@MainActivity, "welding_base", v)
                recalcAllAndDisplay()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputTransportPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = FormatUtils.parseTomanInput(s?.toString())
                PrefsHelper.saveLong(this@MainActivity, "transport_base", v)
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
            cb.text = "$name  (${FormatUtils.formatToman(extras[name]?.toLong() ?: 0L)})"
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
        var bladeComputed = 0L
        var bladeBase = 0L
        if (spinnerBlade.adapter != null && spinnerBlade.adapter.count > 0) {
            val bladeName = spinnerBlade.selectedItem as? String
            bladeBase = if (bladeName != null) PrefsHelper.getLong(this, "تیغه_price_$bladeName", 0L) else 0L
            bladeComputed = (areaM2 * bladeBase).toLong()
            textBladeLine.text = "تیغه — قیمت پایه: ${FormatUtils.formatToman(bladeBase)}  |  قیمت کل: ${FormatUtils.formatToman(bladeComputed)}"
        } else {
            textBladeLine.text = "تیغه — داده‌ای موجود نیست"
        }

        // موتور
        var motorBase = 0L
        if (spinnerMotor.adapter != null && spinnerMotor.adapter.count > 0) {
            val motorName = spinnerMotor.selectedItem as? String
            motorBase = if (motorName != null) PrefsHelper.getLong(this, "موتور_price_$motorName", 0L) else 0L
            textMotorLine.text = "موتور — قیمت: ${FormatUtils.formatToman(motorBase)}"
        } else {
            textMotorLine.text = "موتور — داده‌ای موجود نیست"
        }

        // شفت
        var shaftComputed = 0L
        var shaftBase = 0L
        if (spinnerShaft.adapter != null && spinnerShaft.adapter.count > 0) {
            val shaftName = spinnerShaft.selectedItem as? String
            shaftBase = if (shaftName != null) PrefsHelper.getLong(this, "شفت_price_$shaftName", 0L) else 0L
            val widthM = widthCm / 100.0
            shaftComputed = (shaftBase * widthM).toLong()
            textShaftLine.text = "شفت — قیمت پایه: ${FormatUtils.formatToman(shaftBase)}  |  قیمت کل: ${FormatUtils.formatToman(shaftComputed)}"
        } else {
            textShaftLine.text = "شفت — داده‌ای موجود نیست"
        }

        // قوطی
        var boxComputedValue = 0L
        if (checkboxBoxEnabled.isChecked && spinnerBox.adapter != null && spinnerBox.adapter.count > 0) {
            val boxName = spinnerBox.selectedItem as? String
            val boxBase = if (boxName != null) PrefsHelper.getLong(this, "قوطی_price_$boxName", 0L) else 0L
            val effectiveHeight = max(0.0, heightCm - 30.0) // کم کردن فضای آزاد
            val units = (effectiveHeight * 2.0) / 100.0      // دو خط عمودی به متر
            boxComputedValue = (units * boxBase).toLong()
            textBoxLine.text = "قوطی — قیمت پایه: ${FormatUtils.formatToman(boxBase)}  |  قیمت کل: ${FormatUtils.formatToman(boxComputedValue)}"
        } else {
            textBoxLine.text = "قوطی — محاسبه نشده"
        }

        // نصب / جوشکاری / حمل
        val installRate = FormatUtils.parseTomanInput(inputInstallPrice.text?.toString())
        val installComputed = when {
            areaM2 == 0.0 -> installRate
            areaM2 in 2.0..10.0 -> installRate * 10L
            areaM2 > 10.0 -> (installRate * areaM2).toLong()
            else -> installRate * 10L
        }
        textInstallComputed.text = FormatUtils.formatToman(installComputed)

        val weldingComputed = FormatUtils.parseTomanInput(inputWeldingPrice.text?.toString())
        val transportComputed = FormatUtils.parseTomanInput(inputTransportPrice.text?.toString())

        // مجموع گزینه‌های اضافی
        val extras = PrefsHelper.getAllExtraOptions(this) // Map<String, Float>
        var extrasTotal = 0L
        for ((name, priceF) in extras) {
            val enabled = PrefsHelper.getBool(this, "extra_enabled_$name")
            if (enabled) extrasTotal += priceF.toLong()
        }

        // نمایش ریزمحاسبات
        textBreakBlade.text = "جمع تیغه: ${FormatUtils.formatToman(bladeComputed)}"
        textBreakMotor.text = "موتور: ${FormatUtils.formatToman(motorBase)}"
        textBreakShaft.text = "جمع شفت: ${FormatUtils.formatToman(shaftComputed)}"
        textBreakBox.text = "جمع قوطی: ${FormatUtils.formatToman(boxComputedValue)}"
        textBreakInstall.text = "نصب: ${FormatUtils.formatToman(installComputed)}"
        textBreakWelding.text = "جوشکاری: ${FormatUtils.formatToman(weldingComputed)}"
        textBreakTransport.text = "کرایه حمل: ${FormatUtils.formatToman(transportComputed)}"
        textBreakExtras.text = "گزینه‌های اضافی: ${FormatUtils.formatToman(extrasTotal)}"

        // جمع کل
        val total = bladeComputed + motorBase + shaftComputed + boxComputedValue +
                installComputed + weldingComputed + transportComputed + extrasTotal
        textTotal.text = "قیمت نهایی: ${FormatUtils.formatToman(total)}"
    }
}
