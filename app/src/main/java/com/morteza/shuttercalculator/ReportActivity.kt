package com.morteza.shuttercalculator

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morteza.shuttercalculator.utils.ReportStorage
import com.morteza.shuttercalculator.ReportModel

class ReportActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        recyclerView = findViewById(R.id.recyclerReports)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val reports = ReportStorage.loadReports(this)

        adapter = ReportAdapter(
            reports.toMutableList(),
            onDeleteClick = { report ->
                ReportStorage.deleteReport(this, report)
                refreshReports()
                Toast.makeText(this, "گزارش حذف شد", Toast.LENGTH_SHORT).show()
            },
            onItemClick = { report ->
                val intent = ReportDetailActivity.newIntent(this, report)
                startActivity(intent)
            }
        )

        recyclerView.adapter = adapter
    }

    private fun refreshReports() {
        val reports = ReportStorage.loadReports(this)
        adapter.updateReports(reports)
    }
}
