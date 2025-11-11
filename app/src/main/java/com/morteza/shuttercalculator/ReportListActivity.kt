package com.morteza.shuttercalculator

import android.content.Intent
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
    private val dao by lazy { AppDatabase.getInstance(this).reportDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_list)

        recycler = findViewById(R.id.recyclerReports)
        buttonClearAll = findViewById(R.id.buttonClearAll)

        adapter = ReportAdapter(mutableListOf(),
            onDelete = { report ->
                AlertDialog.Builder(this)
                    .setTitle("حذف")
                    .setMessage("آیا می‌خواهی این گزارش حذف شود؟")
                    .setPositiveButton("حذف") { _, _ ->
                        lifecycleScope.launch {
                            dao.deleteById(report.id)
                            Toast.makeText(this@ReportListActivity, "حذف شد", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("انصراف", null)
                    .show()
            },
            onItemClick = { report ->
                val i = Intent(this, ReportActivity::class.java)
                i.putExtra("report_id", report.id)
                startActivity(i)
            }
        )

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        lifecycleScope.launch {
            dao.allReportsFlow().collectLatest { list ->
                adapter.setAll(list)
            }
        }

        buttonClearAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("حذف همه")
                .setMessage("آیا می‌خواهی همهٔ گزارش‌ها حذف شوند؟")
                .setPositiveButton("حذف") { _, _ ->
                    lifecycleScope.launch {
                        dao.deleteAll()
                        Toast.makeText(this@ReportListActivity, "همه حذف شدند", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("انصراف", null)
                .show()
        }
    }
}
