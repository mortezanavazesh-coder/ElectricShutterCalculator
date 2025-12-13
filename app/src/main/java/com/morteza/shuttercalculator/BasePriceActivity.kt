package com.morteza.shuttercalculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morteza.shuttercalculator.utils.FormatUtils
import com.morteza.shuttercalculator.utils.PrefsHelper
import com.morteza.shuttercalculator.utils.ThousandSeparatorTextWatcher
import kotlinx.coroutines.*

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

    private lateinit var buttonAddSlat: View
    private lateinit var buttonAddMotor: View
    private lateinit var buttonAddShaft: View
    private lateinit var buttonAddBox: View
    private lateinit var buttonAddExtra: View

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
            PrefsHelper.getFloat(this, "install_base").let { if (it == 0f) "" else FormatUtils.formatTomanPlain(it) }
        )
        inputWeldingBase.setText(
            PrefsHelper.getFloat(this, "welding_base").let { if (it == 0f) "" else FormatUtils.formatTomanPlain(it) }
        )
        inputTransportBase.setText(
            PrefsHelper.getFloat(this, "transport_base").let { if (it == 0f) "" else FormatUtils.formatTomanPlain(it) }
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
            onRename = { title -> showRenameDialog(category, title) },
            onEdit = { title -> showEditPriceDialog(category, title) }
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
                    val price = PrefsHelper.getFloat(this@BasePriceActivity, key, 0f)
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
        val etWidth = view.findViewById<EditText>(R.id.etSlatWidth)         // واحد: سانتی‌متر
        val etThickness = view.findViewById<EditText>(R.id.etSlatThickness) // واحد: سانتی‌متر

        etPrice.addTextChangedListener(ThousandSeparatorTextWatcher(etPrice))

        AlertDialog.Builder(this)
            .setTitle("افزودن تیغه")
            .setView(view)
            .setPositiveButton("افزودن") { dialog, _ ->
                val title = etTitle.text.toString().trim()
                val price = FormatUtils.parseTomanInput(etPrice.text.toString())
                val widthCm = etWidth.text.toString().toFloatOrNull() ?: 0f
                val thicknessCm = etThickness.text.toString().toFloatOrNull() ?: 0f

                if (title.isEmpty() || price <= 0f || widthCm <= 0f || thicknessCm <= 0f) {
                    Toast.makeText(this, "اطلاعات معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // ذخیره با واحد سانتی‌متر
                PrefsHelper.addOption(this, "تیغه", title, price)
                PrefsHelper.saveSlatSpecs(this, title, widthCm, thicknessCm)
                refreshCategory("تیغه")
                Toast.makeText(this, "تیغه اضافه شد ✅", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("لغو", null)
            .show()
    }

    // ------------------ افزودن شفت ------------------
    private fun showAddShaftDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_shaft, null)
        val etTitle = view.findViewById<EditText>(R.id.etShaftTitle)
        val etPrice = view.findViewById<EditText>(R.id.etShaftPrice)
        val etDiameter = view.findViewById<EditText>(R.id.etShaftDiameter) // واحد: سانتی‌متر

        etPrice.addTextChangedListener(ThousandSeparatorTextWatcher(etPrice))

        AlertDialog.Builder(this)
            .setTitle("افزودن شفت")
            .setView(view)
            .setPositiveButton("افزودن") { dialog, _ ->
                val title = etTitle.text.toString().trim()
                val price = FormatUtils.parseTomanInput(etPrice.text.toString())
                val diameterCm = etDiameter.text.toString().toFloatOrNull() ?: 0f

                if (title.isEmpty() || price <= 0f || diameterCm <= 0f) {
                    Toast.makeText(this, "اطلاعات معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // ذخیره با واحد سانتی‌متر
                PrefsHelper.addOption(this, "شفت", title, price)
                PrefsHelper.saveShaftSpecs(this, title, diameterCm)
                refreshCategory("شفت")
                Toast.makeText(this, "شفت اضافه شد ✅", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("لغو", null)
            .show()
    }

    // ------------------ افزودن آیتم‌های عمومی (موتور، قوطی، اضافات) ------------------
    private fun showAddItemDialog(category: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
        val etTitle = view.findViewById<EditText>(R.id.etItemTitle)
        val etPrice = view.findViewById<EditText>(R.id.etItemPrice)

        etPrice.addTextChangedListener(ThousandSeparatorTextWatcher(etPrice))

        AlertDialog.Builder(this)
            .setTitle("افزودن $category")
            .setView(view)
            .setPositiveButton("افزودن") { dialog, _ ->
                val title = etTitle.text.toString().trim()
                val price = FormatUtils.parseTomanInput(etPrice.text.toString())

                if (title.isEmpty() || price <= 0f) {
                    Toast.makeText(this, "عنوان و قیمت معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                PrefsHelper.addOption(this, category, title, price)
                if (category == "اضافات") {
                    // پیش‌فرض فعال باشد تا در صفحه اصلی دیده شود
                    PrefsHelper.saveBool(this, "extra_enabled_$title", true)
                }

                refreshCategory(category)
                Toast.makeText(this, "$category اضافه شد ✅", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("لغو", null)
            .show()
    }

    // ------------------ ذخیره هزینه‌های پایه ------------------
    private fun saveCosts() {
        val install = FormatUtils.parseTomanInput(inputInstallBase.text.toString())
        val welding = FormatUtils.parseTomanInput(inputWeldingBase.text.toString())
        val transport = FormatUtils.parseTomanInput(inputTransportBase.text.toString())

        if (install <= 0f) {
            Toast.makeText(this, "نرخ نصب باید بزرگتر از صفر باشد", Toast.LENGTH_SHORT).show()
            return
        }

        PrefsHelper.putFloat(this, "install_base", install)
        PrefsHelper.putFloat(this, "welding_base", welding)
        PrefsHelper.putFloat(this, "transport_base", transport)

        Toast.makeText(this, "هزینه‌های پایه ذخیره شد ✅", Toast.LENGTH_SHORT).show()
    }

    // ------------------ تغییر نام آیتم ------------------
    private fun showRenameDialog(category: String, oldTitle: String) {
        val input = EditText(this)
        input.setText(oldTitle)

        AlertDialog.Builder(this)
            .setTitle("تغییر نام $category")
            .setView(input)
            .setPositiveButton("ذخیره") { dialog, _ ->
                val newTitle = input.text.toString().trim()
                if (newTitle.isEmpty()) {
                    Toast.makeText(this, "نام معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                uiScope.launch {
                    withContext(Dispatchers.IO) {
                        PrefsHelper.renameOption(this@BasePriceActivity, category, oldTitle, newTitle)

                        // انتقال قیمت
                        val oldKey = "${category}_price_$oldTitle"
                        val newKey = "${category}_price_$newTitle"
                        val price = PrefsHelper.getFloat(this@BasePriceActivity, oldKey, 0f)
                        PrefsHelper.putFloat(this@BasePriceActivity, newKey, price)
                        PrefsHelper.removeKey(this@BasePriceActivity, oldKey)

                        // انتقال وضعیت فعال بودن برای اضافات
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
            .setNegativeButton("لغو", null)
            .show()
    }

    // ------------------ ویرایش قیمت آیتم ------------------
    private fun showEditPriceDialog(category: String, title: String) {
        val input = EditText(this)
        val key = "${category}_price_$title"
        val current = PrefsHelper.getFloat(this, key, 0f)
        if (current > 0f) input.setText(FormatUtils.formatTomanPlain(current))
        input.addTextChangedListener(ThousandSeparatorTextWatcher(input))

        AlertDialog.Builder(this)
            .setTitle("ویرایش قیمت $category")
            .setView(input)
            .setPositiveButton("ذخیره") { dialog, _ ->
                val value = FormatUtils.parseTomanInput(input.text.toString())
                if (value <= 0f) {
                    Toast.makeText(this, "قیمت معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                PrefsHelper.putFloat(this, key, value)
                refreshCategory(category)
                Toast.makeText(this, "قیمت $category بروزرسانی شد ✅", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("لغو", null)
            .show()
    }

    // ------------------ پیش‌فرض فعال‌سازی اضافات ------------------
    private fun refreshCategoryExtrasEnabledDefault() {
        uiScope.launch(Dispatchers.IO) {
            val list = PrefsHelper.getSortedOptionList(this@BasePriceActivity, "اضافات") ?: emptyList()
            for (title in list) {
                val exists = PrefsHelper.containsKey(this@BasePriceActivity, "extra_enabled_$title")
                if (!exists) PrefsHelper.saveBool(this@BasePriceActivity, "extra_enabled_$title", true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        uiScope.cancel()
    }
