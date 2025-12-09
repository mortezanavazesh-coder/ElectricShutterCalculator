package com.morteza.shuttercalculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morteza.shuttercalculator.utils.PrefsHelper
import kotlinx.coroutines.*

class BasePriceActivity : AppCompatActivity() {

    private lateinit var rvSlats: RecyclerView
    private lateinit var rvMotors: RecyclerView
    private lateinit var rvShafts: RecyclerView
    private lateinit var rvBoxes: RecyclerView
    private lateinit var rvExtras: RecyclerView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_price)

        // bind views
        rvSlats = findViewById(R.id.rvSlats)
        rvMotors = findViewById(R.id.rvMotors)
        rvShafts = findViewById(R.id.rvShafts)
        rvBoxes = findViewById(R.id.rvBoxes)
        rvExtras = findViewById(R.id.rvExtras)

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

        // setup adapters
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

        // add buttons
        buttonAddSlat.setOnClickListener { showAddSlatDialog() }
        buttonAddMotor.setOnClickListener { showAddItemDialog("موتور") }
        buttonAddShaft.setOnClickListener { showAddShaftDialog() }
        buttonAddBox.setOnClickListener { showAddItemDialog("قوطی") }
        buttonAddExtra.setOnClickListener { showAddItemDialog("اضافات") }

        buttonSaveAll.setOnClickListener { saveCosts() }
        buttonBack.setOnClickListener { finish() }

        refreshAll()
    }

    private fun createAdapter(category: String): BasePriceAdapter {
        return BasePriceAdapter(
            items = emptyList(),
            onDelete = { title ->
                CoroutineScope(Dispatchers.IO).launch {
                    PrefsHelper.removeOption(this@BasePriceActivity, category, title)
                    withContext(Dispatchers.Main) { refreshCategory(category) }
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
        CoroutineScope(Dispatchers.IO).launch {
            val list = PrefsHelper.getSortedOptionList(this@BasePriceActivity, category)
            val items = list.map { title ->
                val key = "${category}_price_$title"
                val price = PrefsHelper.getFloat(this@BasePriceActivity, key)
                Pair(title, price)
            }
            withContext(Dispatchers.Main) {
                when (category) {
                    "تیغه" -> adapterSlats.update(items)
                    "موتور" -> adapterMotors.update(items)
                    "شفت" -> adapterShafts.update(items)
                    "قوطی" -> adapterBoxes.update(items)
                    "اضافات" -> adapterExtras.update(items)
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

        AlertDialog.Builder(this)
            .setTitle("افزودن تیغه")
            .setView(view)
            .setPositiveButton("افزودن") { dialog, _ ->
                val title = etTitle.text.toString().trim()
                val price = etPrice.text.toString().toFloatOrNull() ?: 0f
                val width = etWidth.text.toString().toFloatOrNull() ?: 0f
                val thickness = etThickness.text.toString().toFloatOrNull() ?: 0f

                if (title.isEmpty() || price <= 0f || width <= 0f || thickness <= 0f) {
                    Toast.makeText(this, "اطلاعات معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                PrefsHelper.addOption(this, "تیغه", title, price)
                PrefsHelper.saveSlatSpecs(this, title, width, thickness)
                refreshCategory("تیغه")
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
        val etDiameter = view.findViewById<EditText>(R.id.etShaftDiameter)

        AlertDialog.Builder(this)
            .setTitle("افزودن شفت")
            .setView(view)
            .setPositiveButton("افزودن") { dialog, _ ->
                val title = etTitle.text.toString().trim()
                val price = etPrice.text.toString().toFloatOrNull() ?: 0f
                val diameter = etDiameter.text.toString().toFloatOrNull() ?: 0f

                if (title.isEmpty() || price <= 0f || diameter <= 0f) {
                    Toast.makeText(this, "اطلاعات معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                PrefsHelper.addOption(this, "شفت", title, price)
                PrefsHelper.saveShaftSpecs(this, title, diameter)
                refreshCategory("شفت")
                dialog.dismiss()
            }
            .setNegativeButton("لغو", null)
            .show()
    }

    // ------------------ افزودن آیتم‌های دیگر ------------------
    private fun showAddItemDialog(category: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
        val etTitle = view.findViewById<EditText>(R.id.etItemTitle)
        val etPrice = view.findViewById<EditText>(R.id.etItemPrice)

        AlertDialog.Builder(this)
            .setTitle("افزودن $category")
            .