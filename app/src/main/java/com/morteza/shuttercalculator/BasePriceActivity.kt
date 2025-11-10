package com.morteza.shuttercalculator

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morteza.shuttercalculator.utils.FormatUtils
import com.morteza.shuttercalculator.utils.PrefsHelper
import com.morteza.shuttercalculator.utils.ThousandSeparatorTextWatcher

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

    private lateinit var buttonAddSlat: Button
    private lateinit var buttonAddMotor: Button
    private lateinit var buttonAddShaft: Button
    private lateinit var buttonAddBox: Button
    private lateinit var buttonAddExtra: Button

    private lateinit var inputInstallBase: EditText
    private lateinit var inputWeldingBase: EditText
    private lateinit var inputTransportBase: EditText
    private lateinit var buttonSaveAll: Button
    private lateinit var buttonBack: Button

    private var adapterSlats: OptionRecyclerAdapter? = null
    private var adapterMotors: OptionRecyclerAdapter? = null
    private var adapterShafts: OptionRecyclerAdapter? = null
    private var adapterBoxes: OptionRecyclerAdapter? = null
    private var adapterExtras: OptionRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_base_price)
        } catch (e: Exception) {
            Log.e("BasePriceActivity", "setContentView failed", e)
            finish()
            return
        }

        // find views
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

        // Thousand separator TextWatchers for editable fields
        inputInstallBase.addTextChangedListener(ThousandSeparatorTextWatcher(inputInstallBase))
        inputWeldingBase.addTextChangedListener(ThousandSeparatorTextWatcher(inputWeldingBase))
        inputTransportBase.addTextChangedListener(ThousandSeparatorTextWatcher(inputTransportBase))

        // initial values displayed as plain grouped numbers (editable)
        inputInstallBase.setText(FormatUtils.formatTomanPlain(PrefsHelper.getFloat(this, "install_base")))
        inputWeldingBase.setText(FormatUtils.formatTomanPlain(PrefsHelper.getFloat(this, "welding_base")))
        inputTransportBase.setText(FormatUtils.formatTomanPlain(PrefsHelper.getFloat(this, "transport_base")))

        // RecyclerView config
        val rvs = listOf(rvSlats, rvMotors, rvShafts, rvBoxes, rvExtras)
        rvs.forEach { rv ->
            val lm = LinearLayoutManager(this)
            lm.isAutoMeasureEnabled = true
            rv.layoutManager = lm
            rv.isNestedScrollingEnabled = false
            rv.setHasFixedSize(false)
        }

        // setup lists
        setupListRecycler("تیغه", rvSlats, buttonAddSlat, emptySlats) { refreshSlats() }
        setupListRecycler("موتور", rvMotors, buttonAddMotor, emptyMotors) { refreshMotors() }
        setupListRecycler("شفت", rvShafts, buttonAddShaft, emptyShafts) { refreshShafts() }
        setupListRecycler("قوطی", rvBoxes, buttonAddBox, emptyBoxes) { refreshBoxes() }
        setupListRecycler("اضافی", rvExtras, buttonAddExtra, emptyExtras) { refreshExtras() }

        buttonSaveAll.setOnClickListener {
            try {
                // parse inputs safely (handles thousand separators)
                val install = FormatUtils.parseTomanInput(inputInstallBase.text.toString())
                val welding = FormatUtils.parseTomanInput(inputWeldingBase.text.toString())
                val transport = FormatUtils.parseTomanInput(inputTransportBase.text.toString())

                if (install < 0f || welding < 0f || transport < 0f) {
                    Toast.makeText(this, "قیمت‌ها نمی‌توانند منفی باشند", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                PrefsHelper.saveFloat(this, "install_base", install)
                PrefsHelper.saveFloat(this, "welding_base", welding)
                PrefsHelper.saveFloat(this, "transport_base", transport)
                Toast.makeText(this, "ذخیره شد", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("BasePriceActivity", "saveAll failed", e)
                Toast.makeText(this, "خطا در ذخیره", Toast.LENGTH_SHORT).show()
            }
        }

        buttonBack.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun setupListRecycler(
        category: String,
        recyclerView: RecyclerView,
        addButton: Button,
        emptyView: TextView,
        onRefreshed: (() -> Unit)? = null
    ) {
        try {
            val items = PrefsHelper.getSortedOptionList(this, category)
            val adapter = OptionRecyclerAdapter(
                category,
                items.toMutableList(),
                onEdit = { name -> showEditPriceDialog(category, name, recyclerView) },
                onDeleteRequest = { name, confirmed -> requestDeleteConfirm(category, name) { confirmed() } }
            )
            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(false)

            when (category) {
                "تیغه" -> adapterSlats = adapter
                "موتور" -> adapterMotors = adapter
                "شفت" -> adapterShafts = adapter
                "قوطی" -> adapterBoxes = adapter
                "اضافی" -> adapterExtras = adapter
            }

            emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE

            addButton.setOnClickListener {
                addButton.isEnabled = false
                showAddOptionDialog(category, recyclerView)
                addButton.postDelayed({ addButton.isEnabled = true }, 400)
            }

            onRefreshed?.invoke()
        } catch (e: Exception) {
            Log.e("BasePriceActivity", "setupListRecycler failed for $category", e)
        }
    }

    private fun showAddOptionDialog(category: String, recyclerView: RecyclerView) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("افزودن به $category")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val inputName = EditText(this)
        inputName.hint = "نام"
        val inputPrice = EditText(this)
        inputPrice.hint = "قیمت (تومان)"
        inputPrice.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        inputPrice.addTextChangedListener(ThousandSeparatorTextWatcher(inputPrice))
        val pad = (16 * resources.displayMetrics.density).toInt()
        layout.setPadding(pad, 8, pad, 8)
        layout.addView(inputName)
        layout.addView(inputPrice)
        builder.setView(layout)

        builder.setPositiveButton("افزودن") { _, _ ->
            val name = inputName.text.toString().trim()
            val price = FormatUtils.parseTomanInput(inputPrice.text.toString())
            if (name.isEmpty()) {
                Toast.makeText(this, "نام را وارد کنید", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            if (PrefsHelper.optionExists(this, category, name)) {
                Toast.makeText(this, "چنین آیتمی قبلاً وجود دارد", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            if (price <= 0f) {
                Toast.makeText(this, "قیمت معتبر نیست", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            PrefsHelper.addOption(this, category, name, price)
            refreshRecycler(category, recyclerView)
            Toast.makeText(this, "افزوده شد", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("انصراف", null)
        builder.show()
    }

    private fun showEditPriceDialog(category: String, name: String, recyclerView: RecyclerView) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("ویرایش $name")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val inputPrice = EditText(this)
        inputPrice.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        inputPrice.addTextChangedListener(ThousandSeparatorTextWatcher(inputPrice))

        val current = PrefsHelper.getFloat(this, "${category}_price_$name")
        inputPrice.setText(FormatUtils.formatTomanPlain(current))
        inputPrice.hint = "قیمت (تومان)"
        val pad = (16 * resources.displayMetrics.density).toInt()
        layout.setPadding(pad, 8, pad, 8)
        layout.addView(inputPrice)
        builder.setView(layout)

        builder.setPositiveButton("ذخیره") { _, _ ->
            val newPrice = FormatUtils.parseTomanInput(inputPrice.text.toString())
            if (newPrice <= 0f) {
                Toast.makeText(this, "قیمت معتبر نیست", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            PrefsHelper.saveFloat(this, "${category}_price_$name", newPrice)
            refreshRecycler(category, recyclerView)
        }

        builder.setNeutralButton("تغییر نام") { _, _ ->
            val nameBuilder = AlertDialog.Builder(this)
            nameBuilder.setTitle("تغییر نام $name")
            val nameInput = EditText(this)
            nameInput.setText(name)
            nameBuilder.setView(nameInput)
            nameBuilder.setPositiveButton("ذخیره") { _, _ ->
                val newName = nameInput.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(this, "نام جدید خالی است", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (PrefsHelper.optionExists(this, category, newName)) {
                    Toast.makeText(this, "چنین نامی وجود دارد", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                PrefsHelper.renameOption(this, category, name, newName)
                refreshRecycler(category, recyclerView)
            }
            nameBuilder.setNegativeButton("انصراف", null)
            nameBuilder.show()
        }

        builder.setNegativeButton("انصراف", null)
        builder.show()
    }

    fun requestDeleteConfirm(category: String, name: String, onConfirmed: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("حذف")
            .setMessage("آیا از حذف \"$name\" از $category مطمئن هستید؟")
            .setPositiveButton("حذف") { _, _ ->
                onConfirmed()
                refreshRecycler(category, findRecyclerForCategory(category))
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    private fun findRecyclerForCategory(category: String): RecyclerView =
        when (category) {
            "تیغه" -> rvSlats
            "موتور" -> rvMotors
            "شفت" -> rvShafts
            "قوطی" -> rvBoxes
            "اضافی" -> rvExtras
            else -> rvSlats
        }

    private fun refreshRecycler(category: String, recyclerView: RecyclerView) {
        try {
            val newItems = PrefsHelper.getSortedOptionList(this, category)
            when (category) {
                "تیغه" -> adapterSlats?.updateItems(newItems)
                "موتور" -> adapterMotors?.updateItems(newItems)
                "شفت" -> adapterShafts?.updateItems(newItems)
                "قوطی" -> adapterBoxes?.updateItems(newItems)
                "اضافی" -> adapterExtras?.updateItems(newItems)
            }
            when (category) {
                "تیغه" -> emptySlats.visibility = if (newItems.isEmpty()) View.VISIBLE else View.GONE
                "موتور" -> emptyMotors.visibility = if (newItems.isEmpty()) View.VISIBLE else View.GONE
                "شفت" -> emptyShafts.visibility = if (newItems.isEmpty()) View.VISIBLE else View.GONE
                "قوطی" -> emptyBoxes.visibility = if (newItems.isEmpty()) View.VISIBLE else View.GONE
                "اضافی" -> emptyExtras.visibility = if (newItems.isEmpty()) View.VISIBLE else View.GONE
            }
            recyclerView.post {
                recyclerView.requestLayout()
            }
        } catch (e: Exception) {
            Log.e("BasePriceActivity", "refreshRecycler failed for $category", e)
        }
    }

    private fun refreshSlats() = refreshRecycler("تیغه", rvSlats)
    private fun refreshMotors() = refreshRecycler("موتور", rvMotors)
    private fun refreshShafts() = refreshRecycler("شفت", rvShafts)
    private fun refreshBoxes() = refreshRecycler("قوطی", rvBoxes)
    private fun refreshExtras() = refreshRecycler("اضافی", rvExtras)
}
