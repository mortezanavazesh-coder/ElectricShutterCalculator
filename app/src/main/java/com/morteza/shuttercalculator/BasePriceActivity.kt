package com.morteza.shuttercalculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
            onEditFull = { title, price ->
                when (category) {
                    "تیغه" -> showEditSlatDialog(title)
                    "موتور" -> showEditMotorDialog(title, price)
                    "شفت" -> showEditShaftDialog(title)
                    "قوطی" -> showEditBoxDialog(title, price)
                    "اضافات" -> showEditExtraDialog(title, price)
                }
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

        val dialog = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
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

        val dialog = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
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

    // ------------------ افزودن آیتم‌های عمومی ------------------
    private fun showAddItemDialog(category: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
        val etTitle = view.findViewById<EditText>(R.id.etItemTitle)
        val etPrice = view.findViewById<EditText>(R.id.etItemPrice)

        etPrice.addTextChangedListener(ThousandSeparatorTextWatcher(etPrice))

        val dialog = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
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

    // ------------------ ویرایش تیغه (نام، قیمت، عرض، ضخامت) ------------------
    private fun showEditSlatDialog(title: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_slat, null)
        val etTitle = view.findViewById<EditText>(R.id.etSlatTitle)
        val etPrice = view.findViewById<EditText>(R.id.etSlatPrice)
        val etWidth = view.findViewById<EditText>(R.id.etSlatWidth)
        val etThickness = view.findViewById<EditText>(R.id.etSlatThickness)

        etTitle.setText(title)
        val oldPrice = PrefsHelper.getLong(this, "تیغه_price_$title", 0L)
        if (oldPrice > 0L) etPrice.setText(FormatUtils.formatTomanPlain(oldPrice))
        etPrice.addTextChangedListener(ThousandSeparatorTextWatcher(etPrice))

        val specs = getSlatSpecs(title)
        etWidth.setText(specs.width.toString())
        etThickness.setText(specs.thickness.toString())

        val dialog = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(view)
            .create()

        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveSlat)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelSlat)

        btnSave.setOnClickListener {
            val newTitle = etTitle.text.toString().trim()
            val newPrice = FormatUtils.parseTomanInput(etPrice.text.toString())
            val newWidth = etWidth.text.toString().toFloatOrNull() ?: 0f
            val newThickness = etThickness.text.toString().toFloatOrNull() ?: 0f

            if (newTitle.isEmpty() || newPrice < 0L || newWidth <= 0f || newThickness <= 0f) {
                Toast.makeText(this, "اطلاعات معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            PrefsHelper.renameOption(this, "تیغه", title, newTitle)
            PrefsHelper.putLong(this, "تیغه_price_$newTitle", newPrice)
            PrefsHelper.removeKey(this, "تیغه_price_$title")
            PrefsHelper.saveSlatSpecs(this, newTitle, newWidth, newThickness)

            refreshCategory("تیغه")
            Toast.makeText(this, "تیغه ویرایش شد ✅", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ------------------ ویرایش موتور (نام، قیمت) ------------------
    private fun showEditMotorDialog(title: String, oldPrice: Long) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
        val etTitle = view.findViewById<EditText>(R.id.etItemTitle)
        val etPrice = view.findViewById<EditText>(R.id.etItemPrice)

        etTitle.setText(title)
        val price = if (oldPrice > 0L) oldPrice else PrefsHelper.getLong(this, "موتور_price_$title", 0L)
        if (price > 0L) etPrice.setText(FormatUtils.formatTomanPlain(price))
        etPrice.addTextChangedListener(ThousandSeparatorTextWatcher(etPrice))

        val dialog = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(view)
            .create()

        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveItem)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelItem)

        btnSave.setOnClickListener {
            val newTitle = etTitle.text.toString().trim()
            val newPrice = FormatUtils.parseTomanInput(etPrice.text.toString())

            if (newTitle.isEmpty() || newPrice < 0L) {
                Toast.makeText(this, "عنوان و قیمت معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            PrefsHelper.renameOption(this, "موتور", title, newTitle)
            PrefsHelper.putLong(this, "موتور_price_$newTitle", newPrice)
            PrefsHelper.removeKey(this, "موتور_price_$title")

            refreshCategory("موتور")
            Toast.makeText(this, "موتور ویرایش شد ✅", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ------------------ ویرایش شفت (نام، قیمت، قطر) ------------------
    private fun showEditShaftDialog(title: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_shaft, null)
        val etTitle = view.findViewById<EditText>(R.id.etShaftTitle)
        val etPrice = view.findViewById<EditText>(R.id.etShaftPrice)
        val etDiameter = view.findViewById<EditText>(R.id.etShaftDiameter)

        etTitle.setText(title)
        val oldPrice = PrefsHelper.getLong(this, "شفت_price_$title", 0L)
        if (oldPrice > 0L) etPrice.setText(FormatUtils.formatTomanPlain(oldPrice))
        etPrice.addTextChangedListener(ThousandSeparatorTextWatcher(etPrice))

        val specs = getShaftSpecs(title)
        etDiameter.setText(specs.diameter.toString())

        val dialog = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
        .setView(view)
        .create()

        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveShaft)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelShaft)

        btnSave.setOnClickListener {
            val newTitle = etTitle.text.toString().trim()
            val newPrice = FormatUtils.parseTomanInput(etPrice.text.toString())
            val newDiameter = etDiameter.text.toString().toFloatOrNull() ?: 0f

            if (newTitle.isEmpty() || newPrice < 0L || newDiameter <= 0f) {
                Toast.makeText(this, "اطلاعات معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            PrefsHelper.renameOption(this, "شفت", title, newTitle)
            PrefsHelper.putLong(this, "شفت_price_$newTitle", newPrice)
            PrefsHelper.removeKey(this, "شفت_price_$title")
            PrefsHelper.saveShaftSpecs(this, newTitle, newDiameter)

            refreshCategory("شفت")
            Toast.makeText(this, "شفت ویرایش شد ✅", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ------------------ ویرایش قوطی (نام، قیمت) ------------------
    private fun showEditBoxDialog(title: String, oldPrice: Long) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
        val etTitle = view.findViewById<EditText>(R.id.etItemTitle)
        val etPrice = view.findViewById<EditText>(R.id.etItemPrice)

        etTitle.setText(title)
        val price = if (oldPrice > 0L) oldPrice else PrefsHelper.getLong(this, "قوطی_price_$title", 0L)
        if (price > 0L) etPrice.setText(FormatUtils.formatTomanPlain(price))
        etPrice.addTextChangedListener(ThousandSeparatorTextWatcher(etPrice))

        val dialog = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(view)
            .create()

        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveItem)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelItem)

        btnSave.setOnClickListener {
            val newTitle = etTitle.text.toString().trim()
            val newPrice = FormatUtils.parseTomanInput(etPrice.text.toString())

            if (newTitle.isEmpty() || newPrice < 0L) {
                Toast.makeText(this, "عنوان و قیمت معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            PrefsHelper.renameOption(this, "قوطی", title, newTitle)
            PrefsHelper.putLong(this, "قوطی_price_$newTitle", newPrice)
            PrefsHelper.removeKey(this, "قوطی_price_$title")

            refreshCategory("قوطی")
            Toast.makeText(this, "قوطی ویرایش شد ✅", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ------------------ ویرایش اضافات (نام، قیمت + حفظ enabled) ------------------
    private fun showEditExtraDialog(title: String, oldPrice: Long) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
        val etTitle = view.findViewById<EditText>(R.id.etItemTitle)
        val etPrice = view.findViewById<EditText>(R.id.etItemPrice)

        etTitle.setText(title)
        val price = if (oldPrice > 0L) oldPrice else PrefsHelper.getLong(this, "اضافات_price_$title", 0L)
        if (price > 0L) etPrice.setText(FormatUtils.formatTomanPlain(price))
        etPrice.addTextChangedListener(ThousandSeparatorTextWatcher(etPrice))

        val dialog = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(view)
            .create()

        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveItem)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelItem)

        btnSave.setOnClickListener {
            val newTitle = etTitle.text.toString().trim()
            val newPrice = FormatUtils.parseTomanInput(etPrice.text.toString())

            if (newTitle.isEmpty() || newPrice < 0L) {
                Toast.makeText(this, "عنوان و قیمت معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val oldEnabledKey = "extra_enabled_$title"
            val newEnabledKey = "extra_enabled_$newTitle"
            val enabled = PrefsHelper.getBool(this, oldEnabledKey)

            PrefsHelper.renameOption(this, "اضافات", title, newTitle)
            PrefsHelper.putLong(this, "اضافات_price_$newTitle", newPrice)
            PrefsHelper.removeKey(this, "اضافات_price_$title")

            PrefsHelper.saveBool(this, newEnabledKey, enabled)
            PrefsHelper.removeKey(this, oldEnabledKey)

            refreshCategory("اضافات")
            Toast.makeText(this, "گزینه اضافی ویرایش شد ✅", Toast.LENGTH_SHORT).show()
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

    override fun onDestroy() {
        super.onDestroy()
        uiScope.cancel()
    }

    // ------------------ متدهای مشخصات برای دیالوگ ویرایش ------------------
    private fun getSlatSpecs(title: String): SlatSpecs {
        val s = PrefsHelper.getSlatSpecs(this, title)
        return if (s != null) SlatSpecs(s.width, s.thickness) else SlatSpecs(0f, 0f)
    }

    private fun getShaftSpecs(title: String): ShaftSpecs {
        val s = PrefsHelper.getShaftSpecs(this, title)
        return if (s != null) ShaftSpecs(s.diameter) else ShaftSpecs(0f)
    }

    data class SlatSpecs(val width: Float, val thickness: Float)
    data class ShaftSpecs(val diameter: Float)
}
