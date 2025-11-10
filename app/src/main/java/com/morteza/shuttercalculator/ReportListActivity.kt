package com.morteza.shuttercalculator

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReportListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ReportAdapter
    private lateinit var buttonClearAll: Button
    private lateinit var buttonAddSample: Button

    private val dao by lazy { AppDatabase.getInstance(this).reportDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_list)

        recycler = findViewById(R.id.recyclerReports)
        buttonClearAll = findViewById(R.id.buttonClearAll)
        buttonAddSample = findViewById(R.id.buttonAddSample)

        adapter = ReportAdapter(mutableListOf()) { report ->
            // حذف تکی با تایید
            AlertDialog.Builder(this)
                .setTitle("حذف گزارش")
                .setMessage("آیا می‌خواهی این گزارش حذف شود؟")
                .setPositiveButton("حذف") { _, _ ->
                    lifecycleScope.launch {
                        dao.deleteById(report.id)
                        Toast.makeText(this@ReportListActivity, "گزارش حذف شد", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("انصراف", null)
                .show()
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        lifecycleScope.launch {
            dao.allReportsFlow().collectLatest { list ->
                adapter.setAll(list)
            }
        }

        buttonClearAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("حذف همه گزارش‌ها")
                .setMessage("آیا مطمئنی که می‌خواهی همه گزارش‌ها حذف شوند؟")
                .setPositiveButton("حذف") { _, _ ->
                    lifecycleScope.launch {
                        dao.deleteAll()
                        Toast.makeText(this@ReportListActivity, "همهٔ گزارش‌ها حذف شدند", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("انصراف", null)
                .show()
        }

        // دکمهٔ نمونه برای تست
        buttonAddSample.setOnClickListener {
            lifecycleScope.launch {
                val sample = ReportEntity(
                    customerName = "مشتری نمونه",
                    heightCm = 200.0,
                    widthCm = 300.0,
                    breakdown = "تیغه: 1,000; موتور: 500; نصب: 300",
                    totalPriceToman = 1800000L
                )
                dao.insert(sample)
                Toast.makeText(this@ReportListActivity, "نمونه اضافه شد", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
