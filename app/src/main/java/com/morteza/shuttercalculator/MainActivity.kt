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
                    val installBase = FormatUtils.parseTomanInput(inputInstallPrice.text.toString()).toLong()
                    val weldingBase = FormatUtils.parseTomanInput(inputWeldingPrice.text.toString()).toLong()
                    val transportBase = FormatUtils.parseTomanInput(inputTransportPrice.text.toString()).toLong()

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
                    val extras = PrefsHelper.getAllExtraOptions(this)
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
