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

        // keep previousInstallBase safe while user types
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

        // when user finishes editing the base install price, validate and persist
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
                // persist
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        PrefsHelper.saveFloat(this@MainActivity, "install_base", v)
                    } catch (e: Exception) {
                        // swallow to avoid crash; show optional feedback on main thread
                        withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "خطا در ذخیره", Toast.LENGTH_SHORT).show() }
                    }
                }
                contentViewPost { recalcAllAndDisplay() }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputWeldingPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = FormatUtils.parseTomanInput(s?.toString()).coerceAtLeast(0f)
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        PrefsHelper.saveFloat(this@MainActivity, "welding_base", v)
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "خطا در ذخیره", Toast.LENGTH_SHORT).show() }
                    }
                }
                contentViewPost { recalcAllAndDisplay() }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputTransportPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val v = FormatUtils.parseTomanInput(s?.toString()).coerceAtLeast(0f)
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        PrefsHelper.saveFloat(this@MainActivity, "transport_base", v)
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "خطا در ذخیره", Toast.LENGTH_SHORT).show() }
                    }
                }
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

        // changed to ReportsActivity which uses PrefsHelper-based implementation
        buttonReports.setOnClickListener {
            try {
                startActivity(Intent(this, ReportsActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "صفحه گزارشات موجود نیست", Toast.LENGTH_SHORT).show()
            }
        }

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
                // use safe encoded keys via PrefsHelper; PrefsHelper handles legacy keys too
                val enabled = try { PrefsHelper.getBool(this, "extra_enabled_${name}") } catch (e: Exception) { false }
                cb.isChecked = enabled
                cb.setOnCheckedChangeListener { _, isChecked ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        try { PrefsHelper.saveBool(this@MainActivity, "extra_enabled_${name}", isChecked) }
                        catch (_: Exception) { /* ignore */ }
                    }
                    contentViewPost { recalcAllAndDisplay() }
                }
                extrasContainer.addView(cb)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- New: Save report dialog that uses PrefsHelper instead of DB ---
    private fun showSaveReportDialog() {
        try {
            val input = EditText(this)
            input.hint = "عنوان گزارش (مثال: گزارش 1404/01/01)"
            AlertDialog.Builder(this)
                .setTitle("ذخیره گزارش")
                .setView(input)
                .setPositiveButton("ذخیره") { dialog, _ ->
                    val title = input.text.toString().trim()
                    if (title.isEmpty()) {
                        Toast.makeText(this, "عنوان را وارد کنید", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    // save off main thread
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            if (PrefsHelper.optionExists(this@MainActivity, "گزارش", title)) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, "چنین گزارشی قبلاً وجود دارد", Toast.LENGTH_SHORT).show()
                                }
                                return@launch
                            }
                            PrefsHelper.addOption(this@MainActivity, "گزارش", title, 0f)
                            PrefsHelper.saveMetaTimestamp(this@MainActivity, "گزارش", title, System.currentTimeMillis())
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "گزارش ذخیره شد", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "خطا در ذخیره گزارش", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("انصراف", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "خطا در نمایش دیالوگ", Toast.LENGTH_SHORT).show()
        }
    }

    // NOTE: Methods referenced like contentViewPost(...) and recalcAllAndDisplay() are preserved unchanged.
    // They must remain in this class or be available via superclass/extension as before.
}
