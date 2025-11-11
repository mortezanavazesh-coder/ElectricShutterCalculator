package com.morteza.shuttercalculator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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

    // keep previous valid install base to restore if user inputs invalid
    private var previousInstallBase: Float = 0f

    // DB
    private val db by lazy { AppDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vm = ViewModelProvider(this).get(MainViewModel::class.java)

        bindViews()
        setupTextWatchers()
        setupSpinners()
        setupButtons()

        vm.basePrices.observe(this) { bp ->
            spinnerBlade.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.blades).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            spinnerMotor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.motors).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            spinnerShaft.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.shafts).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            spinnerBox.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.boxes).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

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
        recalcAllAndDisplay()
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

        // validate install base: keep previous valid value if parse fails
        inputInstallPriceBase.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val vStr = s?.toString()?.replace(",", "")?.trim().orEmpty()
                val f = vStr.toFloatOrNull()
                if (f != null) {
                    previousInstallBase = f
                } else {
                    inputInstallPriceBase.post {
                        inputInstallPriceBase.setText(FormatUtils.formatTomanPlain(previousInstallBase))
                        inputInstallPriceBase.setSelection(inputInstallPriceBase.text.length)
                    }
                }
                recalcAllAndDisplay()
            }
        })

        // save editable bases to prefs and recalc
        inputInstallPriceBase.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = FormatUtils.parseTomanInput(s?.toString()).coerceAtLeast(0f)
                if (v <= 0f) {
                    // keep previous but avoid multiple toasts by only showing when user cleared the field entirely
                    if (s?.toString()?.isNotBlank() == true) {
                        Toast.makeText(this@MainActivity, "نرخ نصب باید بزرگتر از صفر باشد", Toast.LENGTH_SHORT).show()
                        inputInstallPriceBase.post {
                            inputInstallPriceBase.setText(FormatUtils.formatTomanPlain(previousInstallBase))
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
                val v = FormatUtils.parseTomanInput(s?.toString()).coerceAtLeast(0f)
                PrefsHelper.saveFloat(this@MainActivity, "welding_base", v)
                recalcAllAndDisplay()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputTransportPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = FormatUtils.parseTomanInput(s?.toString()).coerceAtLeast(0f)
                PrefsHelper.saveFloat(this@MainActivity, "transport_base", v)
                recalcAllAndDisplay()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupSpinners() {
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { recalcAllAndDisplay() }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerBlade.onItemSelectedListener = listener
        spinnerMotor.onItemSelectedListener = listener
        spinnerShaft.onItemSelectedListener = listener
        spinnerBox.onItemSelectedListener = listener
        checkboxBoxEnabled.setOnCheckedChangeListener { _, _ -> recalcAllAndDisplay() }
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

        buttonReports.setOnClickListener {
            startActivity(Intent(this, ReportListActivity::class.java))
        }

        buttonSaveReport.setOnClickListener { showSaveReportDialog() }
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
            withContext(Dispatchers.IO) {
                db.reportDao().insert(entity)
            }
            Toast.makeText(this@MainActivity, "گزارش ذخیره شد", Toast.LENGTH_SHORT).show()
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
        val nf = NumberFormat.getInstance(Locale("fa"))
        return "${nf.format(v)} تومان"
    }
}
