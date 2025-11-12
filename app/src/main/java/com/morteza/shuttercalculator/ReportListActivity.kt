package com.morteza.shuttercalculator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ReportAdapter
    private lateinit var buttonClearAll: Button
    private lateinit var buttonBackToMain: Button
    private lateinit var textEmpty: TextView

    private val dao by lazy { AppDatabase.getInstance(this).reportDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_list)

        recycler = findViewById(R.id.recyclerReports)
        buttonClearAll = findViewById(R.id.buttonClearAll)
        buttonBackToMain = findViewById(R.id.buttonBackToMain)
        textEmpty = findViewById(R.id.textEmptyReports)

        adapter = ReportAdapter(mutableListOf(),
            onDelete = { report -> confirmAndDelete(report) },
            onItemClick = { report ->
                val i = Intent(this, ReportActivity::class.java)
                i.putExtra("report_id", report.id)
                startActivity(i)
            }
        )

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // Collect flow and update UI
        lifecycleScope.launch {
            dao.allReportsFlow().collectLatest { list ->
                adapter.setAll(list)
                textEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        buttonClearAll.setOnClickListener {
            if (adapter.itemCount == 0) {
                Toast.makeText(this, "گزارشی برای حذف وجود ندارد", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("حذف همه")
                .setMessage("آیا می‌خواهید همهٔ گزارش‌ها حذف شوند؟")
                .setPositiveButton("حذف") { _, _ ->
                    lifecycleScope.launch {
                        try {
                            withContext(Dispatchers.IO) { dao.deleteAll() }
                            Toast.makeText(this@ReportListActivity, "همه حذف شدند", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@ReportListActivity, "خطا در حذف همه: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("انصراف", null)
                .show()
        }

        buttonBackToMain.setOnClickListener { finish() }
    }

    private fun confirmAndDelete(report: ReportEntity) {
        AlertDialog.Builder(this)
            .setTitle("حذف")
            .setMessage("آیا می‌خواهید گزارش ${report.customerName} حذف شود؟")
            .setPositiveButton("حذف") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val deleted = withContext(Dispatchers.IO) { dao.deleteById(report.id) }
                        if (deleted > 0) {
                            Toast.makeText(this@ReportListActivity, "حذف شد", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ReportListActivity, "چیزی حذف نشد", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@ReportListActivity, "خطا هنگام حذف: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("انصراف", null)
            .show()
    }
}
