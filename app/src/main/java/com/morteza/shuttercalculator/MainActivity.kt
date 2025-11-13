package com.morteza.shuttercalculator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.morteza.shuttercalculator.utils.FormatUtils
import com.morteza.shuttercalculator.utils.PrefsHelper
import com.morteza.shuttercalculator.utils.ThousandSeparatorTextWatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

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

    // editable base cost fields (on main screen)
    private lateinit var inputInstallPriceBase: EditText
    private lateinit var inputWeldingPrice: EditText
    private lateinit var inputTransportPrice: EditText

    // computed install shown in a non-editable TextView
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

    private lateinit var buttonBasePrice: Button
    private lateinit var buttonRollDiameter: Button
    private lateinit var buttonReports: Button
    private lateinit var buttonSaveReport: Button

    private var previousInstallBase: Float = 0f
    private val db by lazy { AppDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ensure ContentView set before any findViewById usage
        setContentView(R.layout.activity_main)

        vm = ViewModelProvider(this).get(MainViewModel::class.java)

        bindViews()
        protectInitialFocus()
        setupTextWatchers()
        setupSpinners()
        setupButtons()

        // observe base prices (populate spinners and initial editable fields)
        vm.basePrices.observe(this) { bp ->
            runOnUiThread {
                try {
                    spinnerBlade.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.blades).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                    spinnerMotor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.motors).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                    spinnerShaft.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.shafts).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                    spinnerBox.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.boxes).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                    inputInstallPriceBase.setText(FormatUtils.formatTomanPlain(bp.installBase))
                    inputWeldingPrice.setText(FormatUtils.formatTomanPlain(bp.weldingBase))
                    inputTransportPrice.setText(FormatUtils.formatTomanPlain(bp.transportBase))

                    previousInstallBase = if (bp.installBase > 0f) bp.installBase else 0f

                    buildExtrasCheckboxes(bp.extras)
                    // post to ensure layout pass completed before heavy UI update
                    contentViewPost { recalcAllAndDisplay() }
                } catch (e: Exception) {
                    // fail-safe
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.reloadFromPrefs(this)
        // small delay to allow view system to settle
        contentViewPost { recalcAllAndDisplay() }
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
        buttonSaveReport = findViewById(R.id.buttonSaveReport)

        // thousand separators for editable numeric fields
        inputInstallPriceBase.addTextChangedListener(ThousandSeparatorTextWatcher(inputInstallPriceBase))
        inputWeldingPrice.addTextChangedListener(ThousandSeparatorTextWatcher(inputWeldingPrice))
        inputTransportPrice.addTextChangedListener(ThousandSeparatorTextWatcher(inputTransportPrice))
    }

    private fun protectInitialFocus() {
        // avoid auto focus opening keyboard; clear focus and set next focusable
        inputHeightCm.isFocusableInTouchMode = true
        inputWidthCm.isFocusableInTouchMode = true
        inputHeightCm.clearFocus()
        inputWidthCm.clearFocus()
        window.decorView.post { window.decorView.clearFocus() }
    }

    private fun setupTextWatchers() {
        val recomputeTrigger = { contentViewPost { recalcAllAndDisplay() } }
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { recomputeTrigger.invoke() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        inputHeightCm.addTextChangedListener(watcher)
        inputWidthCm.addTextChangedListener(watcher)

        inputInstallPriceBase.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val vStr = s?.toString()?.replace(",", "")?.trim().orEmpty()
                val f = vStr.toFloatOrNull()
                if (f != null) previousInstallBase = f
                else {
                    inputInstallPriceBase.post {
                        inputInstallPriceBase.setText(FormatUtils.formatTomanPlain(previousInstallBase))
                        inputInstallPriceBase.setSelection(inputInstallPriceBase.text.length)
                    }
                }
                contentViewPost { recalcAllAndDisplay() }
            }
        })

        inputInstallPriceBase.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = FormatUtils.parseTomanInput(s?.toString()).coerceAtLeast(0f)
                if (v <= 0f) {
                    if (s?.toString()?.isNotBlank() == true) {
                        Toast.makeText(this@MainActivity, "نرخ نصب باید بزرگتر از صفر باشد", Toast.LENGTH_SHORT).show()
                        inputInstallPriceBase.post { inputInstallPriceBase.setText(FormatUtils.formatTomanPlain(previousInstallBase)) }
                    }
                    return
                }
                previousInstallBase = v
                PrefsHelper.saveFloat(this@MainActivity, "install_base", v)
                contentViewPost { recalcAllAndDisplay() }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputWeldingPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = FormatUtils.parseTomanInput(s?.toString()).coerceAtLeast(0f)
                PrefsHelper.saveFloat(this@MainActivity, "welding_base", v)
                contentViewPost { recalcAllAndDisplay() }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputTransportPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = FormatUtils.parseTomanInput(s?.toString()).coerceAtLeast(0f)
                PrefsHelper.saveFloat(this@MainActivity, "transport_base", v)
                contentViewPost { recalcAllAndDisplay() }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ensure IME "Done" hides keyboard and does not change layout unexpectedly
        inputTransportPrice.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
                true
            } else false
        }
    }

    private fun setupSpinners() {
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { contentViewPost { recalcAllAndDisplay() } }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerBlade.onItemSelectedListener = listener
        spinnerMotor.onItemSelectedListener = listener
        spinnerShaft.onItemSelectedListener = listener
        spinnerBox.onItemSelectedListener = listener
        checkboxBoxEnabled.setOnCheckedChangeListener { _, _ -> contentViewPost { recalcAllAndDisplay() } }
    }

    private fun setupButtons() {
        buttonBasePrice.setOnClickListener {
            try { startActivity(Intent(this, BasePriceActivity::class.java)) }
            catch (e: Exception) { Toast.makeText(this, "صفحه قیمت‌های پایه یافت نشد", Toast.LENGTH_SHORT).show() }
        }
        buttonRollDiameter.setOnClickListener {
            try {
                val clazz = Class.forName("com.morteza.shuttercalculator.RollDiameterActivity")
                startActivity(Intent(this, clazz))
            } catch (e: ClassNotFoundException) {
                Toast.makeText(this, "صفحه قطر رول موجود نیست", Toast.LENGTH_SHORT).show()
            }
        }
        buttonReports.setOnClickListener { startActivity(Intent(this, ReportListActivity::class.java)) }
        buttonSaveReport.setOnClickListener { showSaveReportDialog() }
    }

    private fun buildExtrasCheckboxes(extras: Map<String, Float>) {
        try {
            // clear existing children safely; then add new ones
            extrasContainer.removeAllViews()
            if (extras.isEmpty()) return
            val sorted = extras.keys.sortedWith(String.CASE_INSENSITIVE_ORDER)
            for (name in sorted) {
                val cb = CheckBox(this)
                cb.text = "$name  (${FormatUtils.formatToman(extras[name] ?: 0f)})"
                cb.isChecked = PrefsHelper.getBool(this, "extra_enabled_$name")
                cb.setOnCheckedChangeListener { _, isChecked ->
                    PrefsHelper.saveBool(this, "extra_enabled_$name", isChecked)
                    contentViewPost { recalcAllAndDisplay() }
                }
                extrasContainer.addView(cb)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // dialog for saving report
    private fun showSaveReportDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("ذخیره گزارش")
        val input = EditText(this)
        input.hint = "نام مشتری را وارد کنید"
        builder.setView(input)
        builder.setPositiveButton("ذخیره") { _, _ ->
            val name = input.text.toString().ifBlank { "مشتری بدون نام" }
            saveCurrentReportForCustomer(name)
        }
        builder.setNegativeButton("انصراف", null)
        builder.show()
    }

    private fun saveCurrentReportForCustomer(name: String) {
        val height = parseDoubleSafe(inputHeightCm.text.toString())
        val width = parseDoubleSafe(inputWidthCm.text.toString())
        val breakdown = buildBreakdownText(height, width)
        val total = calculateTotalPrice(height, width)

        val entity = ReportEntity(
            customerName = name,
            heightCm = height,
            widthCm = width,
            breakdown = breakdown,
            totalPriceToman = total.toLong()
        )

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { db.reportDao().insert(entity) }
                runOnUiThread { Toast.makeText(this@MainActivity, "گزارش ذخیره شد", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this@MainActivity, "خطا هنگام ذخیره گزارش", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun buildBreakdownText(heightCm: Double, widthCm: Double): String {
        val areaM2 = (heightCm * widthCm) / 10000.0
        val bladeName = spinnerBlade.selectedItem?.toString() ?: "-"
        val bladeBase = PrefsHelper.getFloat(this, "تیغه_price_$bladeName")
        val bladeComputed = areaM2 * bladeBase

        val motorName = spinnerMotor.selectedItem?.toString() ?: "-"
        val motorBase = PrefsHelper.getFloat(this, "موتور_price_$motorName")

        val shaftName = spinnerShaft.selectedItem?.toString() ?: "-"
        val shaftBase = PrefsHelper.getFloat(this, "شفت_price_$shaftName")
        val shaftComputed = shaftBase * (widthCm / 100.0)

        val boxComputedValue = if (checkboxBoxEnabled.isChecked) {
            val boxName = spinnerBox.selectedItem?.toString() ?: "-"
            val boxBase = PrefsHelper.getFloat(this, "قوطی_price_$boxName")
            val effectiveHeight = max(0.0, heightCm - 30.0)
            val units = (effectiveHeight * 2.0) / 100.0
            units * boxBase
        } else 0.0

        val installComputed = FormatUtils.parseTomanInput(inputInstallPriceBase.text.toString()).toDouble()
        val weldingComputed = FormatUtils.parseTomanInput(inputWeldingPrice.text.toString()).toDouble()
        val transportComputed = FormatUtils.parseTomanInput(inputTransportPrice.text.toString()).toDouble()

        val extras = PrefsHelper.getAllExtraOptions(this)
        var extrasTotal = 0.0
        for ((key, priceF) in extras) {
            if (PrefsHelper.getBool(this, "extra_enabled_$key")) extrasTotal += priceF.toDouble()
        }

        val sb = StringBuilder()
        sb.append("تیغه($bladeName): ${formatTomanNullable(bladeComputed)}; ")
        sb.append("موتور($motorName): ${formatTomanNullable(motorBase.toDouble())}; ")
        sb.append("شفت($shaftName): ${formatTomanNullable(shaftComputed)}; ")
        if (checkboxBoxEnabled.isChecked) sb.append("قوطی: ${formatTomanNullable(boxComputedValue)}; ")
        sb.append("نصب: ${formatTomanNullable(installComputed)}; ")
        sb.append("جوشکاری: ${formatTomanNullable(weldingComputed)}; ")
        sb.append("حمل: ${formatTomanNullable(transportComputed)}; ")
        sb.append("اضافات: ${formatTomanNullable(extrasTotal)}")
        return sb.toString()
    }

    private fun calculateTotalPrice(heightCm: Double, widthCm: Double): Double {
        val areaM2 = (heightCm * widthCm) / 10000.0

        val bladeName = spinnerBlade.selectedItem?.toString() ?: "-"
        val bladeBase = PrefsHelper.getFloat(this, "تیغه_price_$bladeName")
        val bladeComputed = areaM2 * bladeBase

        val motorName = spinnerMotor.selectedItem?.toString() ?: "-"
        val motorBase = PrefsHelper.getFloat(this, "موتور_price_$motorName")

        val shaftName = spinnerShaft.selectedItem?.toString() ?: "-"
        val shaftBase = PrefsHelper.getFloat(this, "شفت_price_$shaftName")
        val shaftComputed = shaftBase * (widthCm / 100.0)

        val boxComputedValue = if (checkboxBoxEnabled.isChecked) {
            val boxName = spinnerBox.selectedItem?.toString() ?: "-"
            val boxBase = PrefsHelper.getFloat(this, "قوطی_price_$boxName")
            val effectiveHeight = max(0.0, heightCm - 30.0)
            val units = (effectiveHeight * 2.0) / 100.0
            units * boxBase
        } else 0.0

        val installComputed = when {
            areaM2 == 0.0 -> FormatUtils.parseTomanInput(inputInstallPriceBase.text.toString()).toDouble()
            areaM2 in 2.0..10.0 -> FormatUtils.parseTomanInput(inputInstallPriceBase.text.toString()).toDouble() * 10.0
            areaM2 > 10.0 -> FormatUtils.parseTomanInput(inputInstallPriceBase.text.toString()).toDouble() * areaM2
            else -> FormatUtils.parseTomanInput(inputInstallPriceBase.text.toString()).toDouble() * 10.0
        }

        val weldingComputed = FormatUtils.parseTomanInput(inputWeldingPrice.text.toString()).toDouble()
        val transportComputed = FormatUtils.parseTomanInput(inputTransportPrice.text.toString()).toDouble()

        val extras = PrefsHelper.getAllExtraOptions(this)
        var extrasTotal = 0.0
        for ((key, priceF) in extras) {
            if (PrefsHelper.getBool(this, "extra_enabled_$key")) extrasTotal += priceF.toDouble()
        }

        return bladeComputed + motorBase + shaftComputed + boxComputedValue + installComputed + weldingComputed + transportComputed + extrasTotal
    }

    // helpers
    private fun parseDoubleSafe(s: String?): Double {
        return try { s?.replace(",", "")?.trim()?.toDouble() ?: 0.0 } catch (e: Exception) { 0.0 }
    }

    private fun formatTomanNullable(v: Double): String {
        return formatToman(v.toLong())
    }

    private fun formatToman(v: Long): String {
        return try {
            val nf = NumberFormat.getInstance(Locale("fa"))
            "${nf.format(v)} تومان"
        } catch (e: Exception) {
            "$v تومان"
        }
    }

    private fun contentViewPost(action: () -> Unit) {
        try {
            val root = findViewById<View>(android.R.id.content)
            root?.post { action() }
        } catch (e: Exception) {
            try { action() } catch (_: Exception) {}
        }
    }

    // central recalc/display function
    private fun recalcAllAndDisplay() {
        try {
            val heightCm = max(0.0, parseDoubleSafe(inputHeightCm.text.toString()))
            val widthCm = max(0.0, parseDoubleSafe(inputWidthCm.text.toString()))
            val areaM2 = (widthCm * heightCm) / 10000.0
            textAreaM2.text = String.format(Locale.getDefault(), "مساحت: %.3f متر مربع", areaM2)

            val bladeName = spinnerBlade.selectedItem as? String
            val bladeBase = if (bladeName != null) PrefsHelper.getFloat(this, "تیغه_price_$bladeName") else 0f
            val bladeComputed = areaM2 * bladeBase
            textBladeLine.text = "تیغه — پایه: ${FormatUtils.formatToman(bladeBase)}  |  جمع: ${FormatUtils.formatToman(bladeComputed.toFloat())}"

            val motorName = spinnerMotor.selectedItem as? String
            val motorBase = if (motorName != null) PrefsHelper.getFloat(this, "موتور_price_$motorName") else 0f
            textMotorLine.text = "موتور — قیمت: ${FormatUtils.formatToman(motorBase)}"

            val shaftName = spinnerShaft.selectedItem as? String
            val shaftBase = if (shaftName != null) PrefsHelper.getFloat(this, "شفت_price_$shaftName") else 0f
            val shaftComputed = shaftBase * (widthCm / 100.0)
            textShaftLine.text = "شفت — پایه: ${FormatUtils.formatToman(shaftBase)}  |  جمع: ${FormatUtils.formatToman(shaftComputed.toFloat())}"

            val boxComputedValue = if (checkboxBoxEnabled.isChecked) {
                val boxName = spinnerBox.selectedItem as? String
                val boxBase = if (boxName != null) PrefsHelper.getFloat(this, "قوطی_price_$boxName") else 0f
                val effectiveHeight = max(0.0, heightCm - 30.0)
                val units = (effectiveHeight * 2.0) / 100.0
                val computed = units * boxBase
                textBoxLine.text = "قوطی — پایه: ${FormatUtils.formatToman(boxBase)}  |  جمع: ${FormatUtils.formatToman(computed.toFloat())}"
                computed
            } else {
                textBoxLine.text = "قوطی — محاسبه نشده"
                0.0
            }

            val installRate = FormatUtils.parseTomanInput(inputInstallPriceBase.text.toString()).toDouble()
            val installComputed = when {
                areaM2 == 0.0 -> installRate
                areaM2 in 2.0..10.0 -> installRate * 10.0
                areaM2 > 10.0 -> installRate * areaM2
                else -> installRate * 10.0
            }
            textInstallComputed.text = FormatUtils.formatToman(installComputed.toFloat())

            val weldingComputed = FormatUtils.parseTomanInput(inputWeldingPrice.text.toString()).toDouble()
            val transportComputed = FormatUtils.parseTomanInput(inputTransportPrice.text.toString()).toDouble()

            val extras = PrefsHelper.getAllExtraOptions(this)
            var extrasTotal = 0.0
            for ((name, priceF) in extras) {
                if (PrefsHelper.getBool(this, "extra_enabled_$name")) extrasTotal += priceF.toDouble()
            }

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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
