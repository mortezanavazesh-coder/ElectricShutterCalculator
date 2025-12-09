package com.morteza.shuttercalculator

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morteza.shuttercalculator.utils.ReportStorage

class ReportActivity : AppCompatActivity() {

    private lateinit var rvReports: RecyclerView
    private lateinit var adapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        rvReports = findViewById(R.id.rvReports)
        rvReports.layoutManager = LinearLayoutManager(this)

        val reports = ReportStorage.loadReports(this)
        adapter = ReportAdapter(reports,
            onDelete = { report ->
                ReportStorage.deleteReport(this, report)
                adapter.updateData(ReportStorage.loadReports(this))
                Toast.makeText(this, "گزارش حذف شد", Toast.LENGTH_SHORT).show()
            },
            onClick = { report ->
                // باز کردن صفحه جزئیات کامل گزارش
                val intent = ReportDetailActivity.newIntent(this, report)
                startActivity(intent)
            }
        )
        rvReports.adapter = adapter
    }
}
