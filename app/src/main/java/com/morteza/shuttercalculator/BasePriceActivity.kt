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
import com.morteza.shuttercalculator.utils.FormatUtils
import com.morteza.shuttercalculator.utils.PrefsHelper
import com.morteza.shuttercalculator.utils.ThousandSeparatorTextWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BasePriceActivity : AppCompatActivity() {

    private lateinit var rvItems: RecyclerView
    private lateinit var btnAddItem: View
    private lateinit var spinnerCategory: androidx.appcompat.widget.AppCompatSpinner
    private lateinit var categories: List<String>
    private lateinit var adapter: BasePriceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_price)

        // bind views (IDs must match your activity_base_price.xml)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        rvItems = findViewById(R.id.rvItems)
        btnAddItem = findViewById(R.id.btnAddItem)

        // دسته‌بندی‌ها — اگر فارسی یا انگلیسی متفاوتی استفاده می‌کنی، اینجا تغییر بده
        categories = listOf("تیغه", "موتور", "شفت", "قوطی", "اضافات")

        spinnerCategory.adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        rvItems.layoutManager = LinearLayoutManager(this)

        // adapter: callbacks explicit typed to avoid "Cannot infer a type"
        adapter = BasePriceAdapter(
            items = emptyList(),
            onDelete = { category: String, title: String ->
                val cat = spinnerCategory.selectedItem?.toString() ?: categories.first()
                CoroutineScope(Dispatchers.IO).launch {
                    PrefsHelper.removeOption(this@BasePriceActivity, cat, title)
                    withContext(Dispatchers.Main) { refreshList() }
                }
            },
            onRename = { category: String, oldTitle: String ->
                val cat = spinnerCategory.selectedItem?.toString() ?: categories.first()
                showRenameDialog(cat, oldTitle)
            },
            onEdit = { category: String, title: String ->
                val cat = spinnerCategory.selectedItem?.toString() ?: categories.first()
                showEditPriceDialog(cat, title)
            }
        )
        rvItems.adapter = adapter

        btnAddItem.setOnClickListener { showAddItemDialog() }

        spinnerCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                refreshList()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        refreshList()
    }

    private fun refreshList() {
        val category = spinnerCategory.selectedItem?.toString() ?: categories.first()
        CoroutineScope(Dispatchers.IO).launch {
            val list = PrefsHelper.getSortedOptionList(this@BasePriceActivity, category)
            val items = list.map { title ->
                val key = "${category}_$title"
                val price = PrefsHelper.getFloat(this@BasePriceActivity, key, 0f)
                Pair(title, price)
            }
            withContext(Dispatchers.Main) {
                adapter.update(items)
            }
        }
    }

    private fun showAddItemDialog() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_add_item, null)
        val etTitle = view.findViewById<EditText>(R.id.etItemTitle)
        val etPrice = view.findViewById<EditText>(R.id.etItemPrice)
        etPrice.addTextChangedListener(ThousandSeparatorTextWatcher(etPrice))

        AlertDialog.Builder(this)
            .setTitle("افزودن کالا")
            .setView(view)
            .setPositiveButton("افزودن") { dialog, _ ->
                val title = etTitle.text.toString().trim()
                val rawPrice = etPrice.text?.toString()?.replace(",", "")?.trim().orEmpty()
                val price = FormatUtils.parseTomanInput(rawPrice).coerceAtLeast(0f)

                if (title.isEmpty()) {
                    Toast.makeText(this, "نام کالا را وارد کنید", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (price <= 0f) {
                    Toast.makeText(this, "قیمت معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val category = spinnerCategory.selectedItem?.toString() ?: categories.first()
                CoroutineScope(Dispatchers.IO).launch {
                    PrefsHelper.addOption(this@BasePriceActivity, category, title, price)
                    PrefsHelper.saveMetaTimestamp(this@BasePriceActivity, category, title, System.currentTimeMillis())
                    withContext(Dispatchers.Main) {
                        refreshList()
                        Toast.makeText(this@BasePriceActivity, "کالا افزوده شد", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("لغو", null)
            .show()
    }

    private fun showEditPriceDialog(category: String, title: String) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_add_item, null)
        val etTitle = view.findViewById<EditText>(R.id.etItemTitle)
        val etPrice = view.findViewById<EditText>(R.id.etItemPrice)
        etTitle.setText(title)
        etTitle.isEnabled = false
        etPrice.addTextChangedListener(ThousandSeparatorTextWatcher(etPrice))

        val existing = PrefsHelper.getFloat(this, "${category}_$title", 0f)
        etPrice.setText(FormatUtils.formatTomanPlain(existing))

        AlertDialog.Builder(this)
            .setTitle("ویرایش قیمت")
            .setView(view)
            .setPositiveButton("ذخیره") { dialog, _ ->
                val rawPrice = etPrice.text?.toString()?.replace(",", "")?.trim().orEmpty()
                val price = FormatUtils.parseTomanInput(rawPrice).coerceAtLeast(0f)
                if (price <= 0f) {
                    Toast.makeText(this, "قیمت معتبر وارد کنید", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                CoroutineScope(Dispatchers.IO).launch {
                    PrefsHelper.addOption(this@BasePriceActivity, category, title, price)
                    withContext(Dispatchers.Main) {
                        refreshList()
                        Toast.makeText(this@BasePriceActivity, "قیمت بروزرسانی شد", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("لغو", null)
            .show()
    }

    private fun showRenameDialog(category: String, oldTitle: String) {
        val edit = EditText(this)
        edit.setText(oldTitle)
        edit.hint = "عنوان جدید"

        AlertDialog.Builder(this)
            .setTitle("تغییر نام")
            .setView(edit)
            .setPositiveButton("تایید") { dialog, _ ->
                val newTitle = edit.text.toString().trim()
                if (newTitle.isEmpty()) {
                    Toast.makeText(this, "عنوان جدید را وارد کنید", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                CoroutineScope(Dispatchers.IO).launch {
                    val ok = PrefsHelper.renameOption(this@BasePriceActivity, category, oldTitle, newTitle)
                    withContext(Dispatchers.Main) {
                        if (ok) {
                            Toast.makeText(this@BasePriceActivity, "نام تغییر کرد", Toast.LENGTH_SHORT).show()
                            refreshList()
                        } else {
                            Toast.makeText(this@BasePriceActivity, "تغییر نام ممکن نیست (عنوان جدید ممکن است وجود داشته باشد)", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("انصراف", null)
            .show()
    }
}
