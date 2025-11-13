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

class MainActivity : AppCompatActivity() {

    private lateinit var vm: MainViewModel

    // ورودی‌ها
    private lateinit var inputHeightCm: EditText
    private lateinit var inputWidthCm: EditText
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

    // هزینه‌های پایه
    private lateinit var inputInstallPriceBase: EditText
    private lateinit var inputWeldingPrice: EditText
    private lateinit var inputTransportPrice: EditText
    private lateinit var textInstallComputed: TextView

    // اضافات
    private lateinit var extrasContainer: LinearLayout

    // جزئیات هزینه
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

    private lateinit var buttonBasePrice: Button
    private lateinit var buttonRollDiameter: Button
    private lateinit var buttonReports: Button
    private lateinit var buttonSaveReport: Button

    private var previousInstallBase: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vm = ViewModelProvider(this).get(MainViewModel::class.java)

        bindViews()
        protectInitialFocus()
        setupTextWatchers()
        setupSpinners()
        setupButtons()

        vm.basePrices.observe(this) { bp ->
            spinnerBlade.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.blades)
            spinnerMotor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.motors)
            spinnerShaft.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.shafts)
            spinnerBox.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bp.boxes)

            inputInstallPriceBase.setText(FormatUtils.formatTomanPlain(bp.installBase))
            inputWeldingPrice.setText(FormatUtils.formatTomanPlain(bp.weldingBase))
            inputTransportPrice.setText(FormatUtils.formatTomanPlain(bp.transportBase))

            previousInstallBase = bp.installBase
            buildExtrasCheckboxes(bp.extras)
            contentViewPost { recalcAllAndDisplay() }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.reloadFromPrefs(this)
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

        inputInstallPriceBase.addTextChangedListener(ThousandSeparatorTextWatcher(inputInstallPriceBase))
        inputWeldingPrice.addTextChangedListener(ThousandSeparatorTextWatcher(inputWeldingPrice))
        inputTransportPrice.addTextChangedListener(ThousandSeparatorTextWatcher(inputTransportPrice))
    }

    private fun protectInitialFocus() {
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
    }

    private fun setupSpinners() {
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                contentViewPost { recalcAllAndDisplay() }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerBlade.onItemSelectedListener = listener
        spinnerMotor.onItemSelectedListener = listener
        spinnerShaft.onItemSelectedListener = listener
        spinnerBox.onItemSelectedListener = listener
        checkboxBoxEnabled.setOnCheckedChangeListener { _, _ -> contentViewPost { recalcAllAndDisplay() } }
    }

    private fun setupButtons() {
        buttonBasePrice.setOnClickListener { startActivity(Intent(this, BasePriceActivity::class.java)) }
        buttonRollDiameter.setOnClickListener { startActivity(Intent(this, RollCalculatorActivity::class.java)) }
        buttonReports.setOnClickListener { startActivity(Intent(this, ReportActivity::class.java)) }
        buttonSaveReport.setOnClickListener { showSaveReportDialog() }
    }

    private fun buildExtrasCheckboxes(extras: Map<String, Float>) {
        extrasContainer.removeAllViews()
        val sorted = extras.keys.sortedWith(String.CASE_INSENSITIVE_ORDER)
        for (name in sorted) {
            val cb = CheckBox(this)
            cb.text = "$name (${FormatUtils.formatToman(extras[name] ?: 0f)})"
            cb.isChecked = PrefsHelper.getBool(this, "extra_enabled_$name")
            cb.setOnCheckedChangeListener { _, isChecked ->
                PrefsHelper.saveBool(this, "extra_enabled_$name", isChecked)
                contentViewPost { recalcAllAndDisplay() }
            }
            extrasContainer.addView(cb)
        }
    }

    private fun showSaveReportDialog() {
        val input = EditText(this)
        input.hint = "عنوان گزارش"
        AlertDialog.Builder(this)
            .setTitle("ذخیره گزارش")
            .setView(input)
            .setPositiveButton("ذخیره") { _, _ ->
                val title = input.text.toString().trim()
                if (title.isNotEmpty())
