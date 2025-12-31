package com.morteza.shuttercalculator

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.morteza.shuttercalculator.utils.FormatUtils
import com.morteza.shuttercalculator.utils.PrefsHelper
import com.morteza.shuttercalculator.utils.ThousandSeparatorTextWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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

    private val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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

    /**
     * ایجاد Adapter هماهنگ با امضای جدید:
     * BasePriceAdapter(items, onDelete, onEdit)
     */
    private fun createAdapter(category: String): BasePriceAdapter {
        return BasePriceAdapter(
            items = emptyList(),
            onDelete = { title ->
                uiScope.launch {
                    withContext(Dispatchers.IO) {
                        PrefsHelper.removeOption(this@BasePriceActivity, category, title)
                        PrefsHelper.removeKey(this@BasePriceActivity, "${category}_price_$title")
                        if (category == "اضافات") {
                            PrefsHelper.removeKey(this@BasePriceActivity, "extra_enabled_$title")
                        }
                    }
                    refreshCategory(category)
                    Toast.makeText(this@BasePriceActivity, "$category حذف شد ✅", Toast.LENGTH_SHORT).show()
                }
            },
            onEdit = { title ->
                // ویرایش کلی آیتم: باز کردن دیالوگ ویرایش تمام فیلدهای مرتبط
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
        uiScope.launch {
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
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_slat, null)
        val etTitle = view.findViewById<EditText>(R.id.etSlatTitle)
        val etPrice = view.findViewById<EditText>(R.id.etSlatPrice)
        val etWidth = view.findViewById<EditText>(R.id.etSlatWidth)
        val etThickness = view.findViewById<EditText>(R.id.etSlatThickness)

        etPrice.addTextChangedListener(ThousandSeparatorTextWatcher(etPrice))

        val dialog = AlertDialog.Builder(this, R.style.AppAlertDialogTheme)
            .setView(view)
            .create()

        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveSlat)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelSlat)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val priceLong = FormatUtils.parseTomanInput(etPrice.text.toString())
            val widthCm = etWidth.text.toString().toFloatOrNull() ?: 0f
            val thicknessCm = etThickness.text.toString().toFloatOrNull() ?: 0f

            if (title.isEmpty() || priceLong < 0L || widthCm <= 0f || thicknessCm <= 0f) {
                Toast.makeText(this, "اطلاعات معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            PrefsHelper.addOption(this, "تیغه", title, priceLong)
            PrefsHelper.putLong(this, "تیغه_price_$title", priceLong)
            PrefsHelper.saveSlatSpecs(this, title, widthCm, thicknessCm)

            refreshCategory("تیغه")
            Toast.makeText(this, "تیغه اضافه شد ✅", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    // ------------------ افزودن شفت ------------------
    private fun showAddShaftDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_shaft, null)
        val etTitle = view.findViewById<EditText>(R.id.etShaftTitle)
        val etPrice = view.findViewById<EditText>(R.id.etShaftPrice)
        val etDiameter = view.findViewById<EditText>(R.id.etShaftDiameter)

        etPrice.addTextChangedListener(ThousandSeparatorTextWatcher(etPrice))

        val dialog = AlertDialog.Builder(this, R.style.AppAlertDialogTheme)
            .setView(view)
            .create()

        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveShaft)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelShaft)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val priceLong = FormatUtils.parseTomanInput(etPrice.text.toString())
            val diameterCm = etDiameter.text.toString().toFloatOrNull() ?: 0f

            if (title.isEmpty() || priceLong < 0L || diameterCm <= 0f) {
                Toast.makeText(this, "اطلاعات معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            PrefsHelper.addOption(this, "شفت", title, priceLong)
            PrefsHelper.putLong(this, "شفت_price_$title", priceLong)
            PrefsHelper.saveShaftSpecs(this, title, diameterCm)

            refreshCategory("شفت")
            Toast.makeText(this, "شفت اضافه شد ✅", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    // ------------------ افزودن آیتم‌های عمومی (موتور، قوطی، اضافات) ------------------
    private fun showAddItemDialog(category: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
        val etTitle = view.findViewById<EditText>(R.id.etItemTitle)
        val etPrice = view.findViewById<EditText>(R.id.etItemPrice)

        etPrice.addTextChangedListener(ThousandSeparatorTextWatcher(etPrice))

        val dialog = AlertDialog.Builder(this, R.style.AppAlertDialogTheme)
            .setView(view)
            .create()

        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveItem)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelItem)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val priceLong = FormatUtils.parseTomanInput(etPrice.text.toString())

            if (title.isEmpty() || priceLong < 0L) {
                Toast.makeText(this, "عنوان و قیمت معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            PrefsHelper.addOption(this, category, title, priceLong)
            PrefsHelper.putLong(this, "${category}_price_$title", priceLong)

            if (category == "اضافات") {
                PrefsHelper.saveBool(this, "extra_enabled_$title", true)
            }

            refreshCategory(category)
            Toast.makeText(this, "$category اضافه شد ✅", Toast.LENGTH_SHORT).show()
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

        Toast.makeText(this, "هزینه‌های پایه ذخیره شد ✅", Toast.LENGTH_SHORT).show()
    }

    // ------------------ تغییر نام آیتم (Theme-driven) ------------------
    private fun showRenameDialog(category: String, oldTitle: String) {
        val padding = getDimenPx(R.dimen.space_md)
        val gap = getDimenPx(R.dimen.space_lg)
        val btnGap = getDimenPx(R.dimen.space_sm)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
            // پس‌زمینه را به تم بسپار؛ شفاف برای نمایش کارت دیالوگ
            setBackgroundResource(android.R.color.transparent)
        }
        val input = EditText(this).apply {
            setText(oldTitle)
            hint = "نام جدید"
        }
        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, gap, 0, 0)
        }
        val btnCancel = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = "لغو"
        }
        val btnSave = MaterialButton(this).apply {
            text = "ذخیره"
            setPadding(btnGap, 0, 0, 0)
        }
        buttons.addView(btnCancel)
        buttons.addView(btnSave)
        container.addView(input)
        container.addView(buttons)

        val dialog = AlertDialog.Builder(this, R.style.AppAlertDialogTheme)
            .setView(container)
            .create()

        btnSave.setOnClickListener {
            val newTitle = input.text.toString().trim()
            if (newTitle.isEmpty()) {
                Toast.makeText(this, "نام معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uiScope.launch {
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
                Toast.makeText(this@BasePriceActivity, "نام $category تغییر کرد ✅", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    // ------------------ ویرایش قیمت آیتم (Theme-driven) ------------------
    private fun showEditPriceDialog(category: String, title: String) {
        val padding = getDimenPx(R.dimen.space_md)
        val gap = getDimenPx(R.dimen.space_lg)
        val btnGap = getDimenPx(R.dimen.space_sm)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
            setBackgroundResource(android.R.color.transparent)
        }
        val input = EditText(this).apply {
            val key = "${category}_price_$title"
            val current = PrefsHelper.getLong(this@BasePriceActivity, key, 0L)
            if (current > 0L) setText(FormatUtils.formatTomanPlain(current))
            addTextChangedListener(ThousandSeparatorTextWatcher(this))
            hint = "قیمت"
        }
        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, gap, 0, 0)
        }
        val btnCancel = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = "لغو"
        }
        val btnSave = MaterialButton(this).apply {
            text = "ذخیره"
            setPadding(btnGap, 0, 0, 0)
        }
        buttons.addView(btnCancel)
        buttons.addView(btnSave)
        container.addView(input)
        container.addView(buttons)

        val dialog = AlertDialog.Builder(this, R.style.AppAlertDialogTheme)
            .setView(container)
            .create()

        btnSave.setOnClickListener {
            val value = FormatUtils.parseTomanInput(input.text.toString())
            if (value < 0L) {
                Toast.makeText(this, "قیمت معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val key = "${category}_price_$title"
            PrefsHelper.putLong(this, key, value)
            refreshCategory(category)
            Toast.makeText(this, "قیمت $category بروزرسانی شد ✅", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    /**
     * دیالوگ ویرایش کلی آیتم: نام، قیمت و فیلدهای اختصاصی (پهنا/ضخامت/قطر)
     * این متد را adapter هنگام کلیک روی آیکن ویرایش فراخوانی می‌کند.
     */
    private fun showEditItemDialog(category: String, title: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_item, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etPrice = dialogView.findViewById<EditText>(R.id.etPrice)
        val layoutWidth = dialogView.findViewById<View>(R.id.layoutWidth)
        val etWidth = dialogView.findViewById<EditText>(R.id.etWidth)
        val layoutThickness = dialogView.findViewById<View>(R.id.layoutThickness)
        val etThickness = dialogView.findViewById<EditText>(R.id.etThickness)
        val layoutDiameter = dialogView.findViewById<View>(R.id.layoutDiameter)
        val etDiameter = dialogView.findViewById<EditText>(R.id.etDiameter)

        // نمایش/مخفی کردن فیلدها بر اساس دسته
        layoutWidth.visibility = if (category == "تیغه") View.VISIBLE else View.GONE
        layoutThickness.visibility = if (category == "تیغه") View.VISIBLE else View.GONE
        layoutDiameter.visibility = if (category == "شفت") View.VISIBLE else View.GONE

        // بارگذاری مقادیر فعلی
        etTitle.setText(title)
        val price = PrefsHelper.getLong(this, "${category}_price_$title", 0L)
        etPrice.setText(FormatUtils.formatTomanPlain(price))

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

        val dialog = AlertDialog.Builder(this, R.style.AppAlertDialogTheme)
            .setView(dialogView)
            .setPositiveButton("ذخیره", null)
            .setNegativeButton("لغو", null)
            .create()

        dialog.setOnShowListener {
            val btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSave.setOnClickListener {
                val newTitle = etTitle.text.toString().trim()
                val newPrice = FormatUtils.parseTomanInput(etPrice.text.toString())
                val newWidth = etWidth.text.toString().toFloatOrNull() ?: 0f
                val newThickness = etThickness.text.toString().toFloatOrNull() ?: 0f
                val newDiameter = etDiameter.text.toString().toFloatOrNull() ?: 0f

                if (newTitle.isEmpty() || newPrice < 0L) {
                    Toast.makeText(this, "نام و قیمت معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                uiScope.launch(Dispatchers.IO) {
                    // اگر نام تغییر کرد، کلیدهای قدیمی را پاک کن
                    if (newTitle != title) {
                        PrefsHelper.removeOption(this@BasePriceActivity, category, title)
                        PrefsHelper.removeKey(this@BasePriceActivity, "${category}_price_$title")
                        if (category == "تیغه") PrefsHelper.removeSlatSpecs(this@BasePriceActivity, title)
                        if (category == "شفت") PrefsHelper.removeShaftSpecs(this@BasePriceActivity, title)
                    }
                    // ذخیرهٔ جدید
                    PrefsHelper.addOption(this@BasePriceActivity, category, newTitle)
                    PrefsHelper.putLong(this@BasePriceActivity, "${category}_price_$newTitle", newPrice)
                    if (category == "تیغه") PrefsHelper.saveSlatSpecs(this@BasePriceActivity, newTitle, newWidth, newThickness)
                    if (category == "شفت") PrefsHelper.saveShaftSpecs(this@BasePriceActivity, newTitle, newDiameter)
                }

                refreshCategory(category)
                Toast.makeText(this, "تغییرات ذخیره شد", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun getDimenPx(resId: Int): Int = resources.getDimensionPixelSize(resId)

    override fun onDestroy() {
        super.onDestroy()
        uiScope.cancel()
    }
}
