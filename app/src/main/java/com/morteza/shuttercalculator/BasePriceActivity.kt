package com.morteza.shuttercalculator

import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ContextThemeWrapper
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.morteza.shuttercalculator.utils.FormatUtils
import com.morteza.shuttercalculator.utils.PrefsHelper
import com.morteza.shuttercalculator.utils.ThousandSeparatorTextWatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BasePriceActivity : AppCompatActivity() {

    private lateinit var rvSlats: RecyclerView
    private lateinit var rvMotors: RecyclerView
    private lateinit var rvShafts: RecyclerView
    private lateinit var rvBoxes: RecyclerView
    private lateinit var rvExtras: RecyclerView

    private lateinit var emptySlats: TextView
    private lateinit var emptyMotors: TextView
    private lateinit var emptyShafts: TextView
    private lateinit var emptyBoxes: TextView
    private lateinit var emptyExtras: TextView

    private lateinit var buttonAddSlat: ImageView
    private lateinit var buttonAddMotor: ImageView
    private lateinit var buttonAddShaft: ImageView
    private lateinit var buttonAddBox: ImageView
    private lateinit var buttonAddExtra: ImageView

    private lateinit var inputInstallBase: EditText
    private lateinit var inputWeldingBase: EditText
    private lateinit var inputTransportBase: EditText
    private lateinit var buttonSaveAll: View
    private lateinit var buttonBack: View

    private lateinit var adapterSlats: BasePriceAdapter
    private lateinit var adapterMotors: BasePriceAdapter
    private lateinit var adapterShafts: BasePriceAdapter
    private lateinit var adapterBoxes: BasePriceAdapter
    private lateinit var adapterExtras: BasePriceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_price)

        bindViews()
        setupListsAndAdapters()
        setupActions()
        preloadBaseCosts()
        refreshAll()
    }

    private fun bindViews() {
        rvSlats = findViewById(R.id.rvSlats)
        rvMotors = findViewById(R.id.rvMotors)
        rvShafts = findViewById(R.id.rvShafts)
        rvBoxes = findViewById(R.id.rvBoxes)
        rvExtras = findViewById(R.id.rvExtras)

        emptySlats = findViewById(R.id.emptySlats)
        emptyMotors = findViewById(R.id.emptyMotors)
        emptyShafts = findViewById(R.id.emptyShafts)
        emptyBoxes = findViewById(R.id.emptyBoxes)
        emptyExtras = findViewById(R.id.emptyExtras)

        buttonAddSlat = findViewById(R.id.buttonAddSlat)
        buttonAddMotor = findViewById(R.id.buttonAddMotor)
        buttonAddShaft = findViewById(R.id.buttonAddShaft)
        buttonAddBox = findViewById(R.id.buttonAddBox)
        buttonAddExtra = findViewById(R.id.buttonAddExtra)

        inputInstallBase = findViewById(R.id.inputInstallBase)
        inputWeldingBase = findViewById(R.id.inputWeldingBase)
        inputTransportBase = findViewById(R.id.inputTransportBase)
        buttonSaveAll = findViewById(R.id.buttonSaveAll)
        buttonBack = findViewById(R.id.buttonBack)

        inputInstallBase.addTextChangedListener(ThousandSeparatorTextWatcher(inputInstallBase))
        inputWeldingBase.addTextChangedListener(ThousandSeparatorTextWatcher(inputWeldingBase))
        inputTransportBase.addTextChangedListener(ThousandSeparatorTextWatcher(inputTransportBase))

        inputInstallBase.inputType = InputType.TYPE_CLASS_NUMBER
        inputWeldingBase.inputType = InputType.TYPE_CLASS_NUMBER
        inputTransportBase.inputType = InputType.TYPE_CLASS_NUMBER
    }

    private fun setupListsAndAdapters() {
        adapterSlats = createAdapter("تیغه")
        adapterMotors = createAdapter("موتور")
        adapterShafts = createAdapter("شفت")
        adapterBoxes = createAdapter("قوطی")
        adapterExtras = createAdapter("اضافات")

        rvSlats.layoutManager = LinearLayoutManager(this)
        rvMotors.layoutManager = LinearLayoutManager(this)
        rvShafts.layoutManager = LinearLayoutManager(this)
        rvBoxes.layoutManager = LinearLayoutManager(this)
        rvExtras.layoutManager = LinearLayoutManager(this)

        rvSlats.adapter = adapterSlats
        rvMotors.adapter = adapterMotors
        rvShafts.adapter = adapterShafts
        rvBoxes.adapter = adapterBoxes
        rvExtras.adapter = adapterExtras
    }

    private fun setupActions() {
        buttonAddSlat.setOnClickListener { showAddSlatDialog() }
        buttonAddMotor.setOnClickListener { showAddItemDialog("موتور") }
        buttonAddShaft.setOnClickListener { showAddShaftDialog() }
        buttonAddBox.setOnClickListener { showAddItemDialog("قوطی") }
        buttonAddExtra.setOnClickListener { showAddItemDialog("اضافات") }

        buttonSaveAll.setOnClickListener { saveCosts() }
        buttonBack.setOnClickListener { finish() }
    }

    private fun preloadBaseCosts() {
        inputInstallBase.setText(
            PrefsHelper.getLong(this, "install_base", 0L).let { if (it == 0L) "" else FormatUtils.formatTomanPlain(it) }
        )
        inputWeldingBase.setText(
            PrefsHelper.getLong(this, "welding_base", 0L).let { if (it == 0L) "" else FormatUtils.formatTomanPlain(it) }
        )
        inputTransportBase.setText(
            PrefsHelper.getLong(this, "transport_base", 0L).let { if (it == 0L) "" else FormatUtils.formatTomanPlain(it) }
        )
    }

    private fun createAdapter(category: String): BasePriceAdapter {
        return BasePriceAdapter(
            items = emptyList(),
            onDelete = { title ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        PrefsHelper.removeOption(this@BasePriceActivity, category, title)
                        PrefsHelper.removeKey(this@BasePriceActivity, "${category}_price_$title")
                        if (category == "اضافات") {
                            PrefsHelper.removeKey(this@BasePriceActivity, "extra_enabled_$title")
                        }
                    }
                    refreshCategory(category)
                    toast("$category حذف شد ✅")
                }
            },
            onEdit = { title ->
                showEditItemDialog(category, title)
            }
        )
    }

    private fun refreshAll() {
        refreshCategory("تیغه")
        refreshCategory("موتور")
        refreshCategory("شفت")
        refreshCategory("قوطی")
        refreshCategory("اضافات")
    }

    private fun refreshCategory(category: String) {
        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                val list = PrefsHelper.getSortedOptionList(this@BasePriceActivity, category) ?: emptyList()
                list.map { title ->
                    val key = "${category}_price_$title"
                    val price = PrefsHelper.getLong(this@BasePriceActivity, key, 0L)
                    title to price
                }
            }
            when (category) {
                "تیغه" -> {
                    adapterSlats.update(items)
                    emptySlats.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                }
                "موتور" -> {
                    adapterMotors.update(items)
                    emptyMotors.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                }
                "شفت" -> {
                    adapterShafts.update(items)
                    emptyShafts.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                }
                "قوطی" -> {
                    adapterBoxes.update(items)
                    emptyBoxes.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                }
                "اضافات" -> {
                    adapterExtras.update(items)
                    emptyExtras.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    // ------------------ افزودن تیغه ------------------
    private fun showAddSlatDialog() {
        val themed = themedContext()
        val view = LayoutInflater.from(themed).inflate(R.layout.dialog_add_slat, null)

        val etTitle = view.findViewById<TextInputEditText>(R.id.etSlatTitle)
        val etPrice = view.findViewById<TextInputEditText>(R.id.etSlatPrice)
        val etWidth = view.findViewById<TextInputEditText>(R.id.etSlatWidth)
        val etThickness = view.findViewById<TextInputEditText>(R.id.etSlatThickness)

        applyPriceWatcher(etPrice)
        applyNumericInput(etWidth, etThickness)

        val dialog = buildThemedDialog(themed, view)

        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveSlat)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelSlat)
        styleDialogButtons(btnSave, btnCancel)

        btnSave.setOnClickListener {
            val title = etTitle.text?.toString()?.trim().orEmpty()
            val priceLong = FormatUtils.parseTomanInput(etPrice.text?.toString().orEmpty())
            val widthCm = etWidth.text?.toString()?.toFloatOrNull() ?: 0f
            val thicknessCm = etThickness.text?.toString()?.toFloatOrNull() ?: 0f

            if (title.isEmpty() || priceLong < 0L || widthCm <= 0f || thicknessCm <= 0f) {
                toast("اطلاعات معتبر وارد کنید")
                return@setOnClickListener
            }

            PrefsHelper.putLong(this, "تیغه_price_$title", priceLong)
            PrefsHelper.saveSlatSpecs(this, title, widthCm, thicknessCm)

            refreshCategory("تیغه")
            toast("تیغه اضافه شد ✅")
            dialog.dismiss()
        }
        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    // ------------------ افزودن شفت ------------------
    private fun showAddShaftDialog() {
        val themed = themedContext()
        val view = LayoutInflater.from(themed).inflate(R.layout.dialog_add_shaft, null)

        val etTitle = view.findViewById<TextInputEditText>(R.id.etShaftTitle)
        val etPrice = view.findViewById<TextInputEditText>(R.id.etShaftPrice)
        val etDiameter = view.findViewById<TextInputEditText>(R.id.etShaftDiameter)

        applyPriceWatcher(etPrice)
        applyNumericInput(etDiameter)

        val dialog = buildThemedDialog(themed, view)

        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveShaft)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelShaft)
        styleDialogButtons(btnSave, btnCancel)

        btnSave.setOnClickListener {
            val title = etTitle.text?.toString()?.trim().orEmpty()
            val priceLong = FormatUtils.parseTomanInput(etPrice.text?.toString().orEmpty())
            val diameterCm = etDiameter.text?.toString()?.toFloatOrNull() ?: 0f

            if (title.isEmpty() || priceLong < 0L || diameterCm <= 0f) {
                toast("اطلاعات معتبر وارد کنید")
                return@setOnClickListener
            }

            PrefsHelper.putLong(this, "شفت_price_$title", priceLong)
            PrefsHelper.saveShaftSpecs(this, title, diameterCm)

            refreshCategory("شفت")
            toast("شفت اضافه شد ✅")
            dialog.dismiss()
        }
        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    // ------------------ افزودن آیتم‌های عمومی (موتور، قوطی، اضافات) ------------------
    private fun showAddItemDialog(category: String) {
        val themed = themedContext()
        val view = LayoutInflater.from(themed).inflate(R.layout.dialog_add_item, null)

        val etTitle = view.findViewById<TextInputEditText>(R.id.etItemTitle)
        val etPrice = view.findViewById<TextInputEditText>(R.id.etItemPrice)

        applyPriceWatcher(etPrice)

        val dialog = buildThemedDialog(themed, view)

        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveItem)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelItem)
        styleDialogButtons(btnSave, btnCancel)

        btnSave.setOnClickListener {
            val title = etTitle.text?.toString()?.trim().orEmpty()
            val priceLong = FormatUtils.parseTomanInput(etPrice.text?.toString().orEmpty())

            if (title.isEmpty() || priceLong < 0L) {
                toast("عنوان و قیمت معتبر وارد کنید")
                return@setOnClickListener
            }

            PrefsHelper.putLong(this, "${category}_price_$title", priceLong)

            if (category == "اضافات") {
                PrefsHelper.saveBool(this, "extra_enabled_$title", true)
            }

            refreshCategory(category)
            toast("$category اضافه شد ✅")
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    // ------------------ ذخیره هزینه‌های پایه ------------------
    private fun saveCosts() {
        val install = FormatUtils.parseTomanInput(inputInstallBase.text.toString())
        val welding = FormatUtils.parseTomanInput(inputWeldingBase.text.toString())
        val transport = FormatUtils.parseTomanInput(inputTransportBase.text.toString())

        PrefsHelper.putLong(this, "install_base", install)
        PrefsHelper.putLong(this, "welding_base", welding)
        PrefsHelper.putLong(this, "transport_base", transport)

        toast("هزینه‌های پایه ذخیره شد ✅")
    }

    // ------------------ تغییر نام آیتم (Theme-driven) ------------------
    private fun showRenameDialog(category: String, oldTitle: String) {
        val themed = themedContext()

        val container = LinearLayout(themed).apply {
            orientation = LinearLayout.VERTICAL
            val padding = getDimenPx(R.dimen.space_md)
            setPadding(padding, padding, padding, padding)
        }

        val inputLayout = TextInputLayout(themed).apply {
            hint = "نام جدید"
        }
        val input = TextInputEditText(themed).apply {
            setText(oldTitle)
        }
        inputLayout.addView(input)

        val buttons = buildEndAlignedButtons(themed)

        container.addView(inputLayout)
        container.addView(buttons.container)

        val dialog = buildThemedDialog(themed, container)

        buttons.btnSave.setOnClickListener {
            val newTitle = input.text?.toString()?.trim().orEmpty()
            if (newTitle.isEmpty()) {
                toast("نام معتبر وارد کنید")
                return@setOnClickListener
            }
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    PrefsHelper.renameOption(this@BasePriceActivity, category, oldTitle, newTitle)

                    val oldKey = "${category}_price_$oldTitle"
                    val newKey = "${category}_price_$newTitle"
                    val price = PrefsHelper.getLong(this@BasePriceActivity, oldKey, -1L)
                    if (price != -1L) {
                        PrefsHelper.putLong(this@BasePriceActivity, newKey, price)
                        PrefsHelper.removeKey(this@BasePriceActivity, oldKey)
                    }

                    if (category == "اضافات") {
                        val oldEnabledKey = "extra_enabled_$oldTitle"
                        val newEnabledKey = "extra_enabled_$newTitle"
                        val enabled = PrefsHelper.getBool(this@BasePriceActivity, oldEnabledKey)
                        PrefsHelper.saveBool(this@BasePriceActivity, newEnabledKey, enabled)
                        PrefsHelper.removeKey(this@BasePriceActivity, oldEnabledKey)
                    }
                }
                refreshCategory(category)
                toast("نام $category تغییر کرد ✅")
                dialog.dismiss()
            }
        }
        buttons.btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    // ------------------ ویرایش قیمت آیتم (Theme-driven) ------------------
    private fun showEditPriceDialog(category: String, title: String) {
        val themed = themedContext()

        val container = LinearLayout(themed).apply {
            orientation = LinearLayout.VERTICAL
            val padding = getDimenPx(R.dimen.space_md)
            setPadding(padding, padding, padding, padding)
        }

        val inputLayout = TextInputLayout(themed).apply { hint = "قیمت" }
        val input = TextInputEditText(themed).apply {
            val key = "${category}_price_$title"
            val current = PrefsHelper.getLong(this@BasePriceActivity, key, 0L)
            if (current > 0L) setText(FormatUtils.formatTomanPlain(current))
            addTextChangedListener(ThousandSeparatorTextWatcher(this))
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        inputLayout.addView(input)

        val buttons = buildEndAlignedButtons(themed)

        container.addView(inputLayout)
        container.addView(buttons.container)

        val dialog = buildThemedDialog(themed, container)

        buttons.btnSave.setOnClickListener {
            val value = FormatUtils.parseTomanInput(input.text?.toString().orEmpty())
            if (value < 0L) {
                toast("قیمت معتبر وارد کنید")
                return@setOnClickListener
            }
            val key = "${category}_price_$title"
            PrefsHelper.putLong(this, key, value)
            refreshCategory(category)
            toast("قیمت $category بروزرسانی شد ✅")
            dialog.dismiss()
        }
        buttons.btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    // ------------------ ویرایش کلی آیتم ------------------
    private fun showEditItemDialog(category: String, title: String) {
        val themed = themedContext()
        val view = LayoutInflater.from(themed).inflate(R.layout.dialog_edit_item, null)

        val etTitle = view.findViewById<TextInputEditText>(R.id.etTitle)
        val etPrice = view.findViewById<TextInputEditText>(R.id.etPrice)
        val layoutWidth = view.findViewById<View>(R.id.layoutWidth)
        val etWidth = view.findViewById<TextInputEditText>(R.id.etWidth)
        val layoutThickness = view.findViewById<View>(R.id.layoutThickness)
        val etThickness = view.findViewById<TextInputEditText>(R.id.etThickness)
        val layoutDiameter = view.findViewById<View>(R.id.layoutDiameter)
        val etDiameter = view.findViewById<TextInputEditText>(R.id.etDiameter)

        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveEdit)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelEdit)
        styleDialogButtons(btnSave, btnCancel)

        layoutWidth.visibility = if (category == "تیغه") View.VISIBLE else View.GONE
        layoutThickness.visibility = if (category == "تیغه") View.VISIBLE else View.GONE
        layoutDiameter.visibility = if (category == "شفت") View.VISIBLE else View.GONE

        etTitle.setText(title)
        val price = PrefsHelper.getLong(this, "${category}_price_$title", 0L)
        etPrice.setText(FormatUtils.formatTomanPlain(price))
        applyPriceWatcher(etPrice)
        applyNumericInput(etWidth, etThickness, etDiameter)

        if (category == "تیغه") {
            PrefsHelper.getSlatSpecs(this, title)?.let {
                etWidth.setText(it.width.toString())
                etThickness.setText(it.thickness.toString())
            }
        } else if (category == "شفت") {
            PrefsHelper.getShaftSpecs(this, title)?.let {
                etDiameter.setText(it.diameter.toString())
            }
        }

        val dialog = buildThemedDialog(themed, view)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val newTitle = etTitle.text?.toString()?.trim().orEmpty()
            val newPrice = FormatUtils.parseTomanInput(etPrice.text?.toString().orEmpty())
            val newWidth = etWidth.text?.toString()?.toFloatOrNull() ?: 0f
            val newThickness = etThickness.text?.toString()?.toFloatOrNull() ?: 0f
            val newDiameter = etDiameter.text?.toString()?.toFloatOrNull() ?: 0f

            if (newTitle.isEmpty() || newPrice < 0L) {
                toast("نام و قیمت معتبر وارد کنید")
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                if (newTitle != title) {
                    PrefsHelper.removeOption(this@BasePriceActivity, category, title)
                    PrefsHelper.removeKey(this@BasePriceActivity, "${category}_price_$title")
                    if (category == "تیغه") PrefsHelper.removeSlatSpecs(this@BasePriceActivity, title)
                    if (category == "شفت") PrefsHelper.removeShaftSpecs(this@BasePriceActivity, title)
                }
                PrefsHelper.putLong(this@BasePriceActivity, "${category}_price_$newTitle", newPrice)
                if (category == "تیغه") PrefsHelper.saveSlatSpecs(this@BasePriceActivity, newTitle, newWidth, newThickness)
                if (category == "شفت") PrefsHelper.saveShaftSpecs(this@BasePriceActivity, newTitle, newDiameter)

                withContext(Dispatchers.Main) {
                    refreshCategory(category)
                    toast("تغییرات ذخیره شد ✅")
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    // ------------------ Helpers ------------------

    private fun themedContext(): ContextThemeWrapper =
        ContextThemeWrapper(this, R.style.AppAlertDialogTheme)

    private fun buildThemedDialog(themed: ContextThemeWrapper, contentView: View) =
        MaterialAlertDialogBuilder(themed, R.style.AppAlertDialogTheme)
            .setView(contentView)
            .setCancelable(true)
            .create().apply {
                // اطمینان از عرض مناسب و پس‌زمینه‌ی تم
                setOnShowListener {
                    window?.setLayout(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
            }

    private data class ButtonRow(
        val container: LinearLayout,
        val btnCancel: MaterialButton,
        val btnSave: MaterialButton
    )

    private fun buildEndAlignedButtons(themed: ContextThemeWrapper): ButtonRow {
        val gap = getDimenPx(R.dimen.space_lg)
        val btnGap = getDimenPx(R.dimen.space_sm)

        val container = LinearLayout(themed).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, gap, 0, 0)
        }
        val btnCancel = MaterialButton(
            themed, null,
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply { text = "لغو" }

        val btnSave = MaterialButton(themed).apply {
            text = "ذخیره"
            setPadding(btnGap, 0, 0, 0)
        }

        container.addView(btnCancel)
        container.addView(btnSave)
        return ButtonRow(container, btnCancel, btnSave)
    }

    private fun styleDialogButtons(btnSave: MaterialButton, btnCancel: MaterialButton) {
        // هماهنگ با تم: یکی outlined و دیگری filled
        btnCancel.setTextColor(getColorCompat(R.color.md_theme_primary))
        btnSave.setTextColor(getColorCompat(R.color.md_theme_onPrimary))
        btnSave.setBackgroundColor(getColorCompat(R.color.md_theme_primary))
    }

    private fun applyPriceWatcher(editText: TextInputEditText?) {
        editText ?: return
        editText.addTextChangedListener(ThousandSeparatorTextWatcher(editText))
        editText.inputType = InputType.TYPE_CLASS_NUMBER
    }

    private fun applyNumericInput(vararg editTexts: TextInputEditText?) {
        editTexts.forEach { et ->
            et ?: return@forEach
            et.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
    }

    private fun getDimenPx(resId: Int): Int = resources.getDimensionPixelSize(resId)

    private fun getColorCompat(resId: Int): Int = resources.getColor(resId, theme)

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
