package com.morteza.shuttercalculator

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.morteza.shuttercalculator.utils.ReportStorage

class ReportActivity : AppCompatActivity() {

    private lateinit var listViewReports: ListView
    private lateinit var adapter: ReportListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        listViewReports = findViewById(R.id.listViewReports)

        val reports = ReportStorage.loadReports(this)

        if (reports.isEmpty()) {
            Toast.makeText(this, "هیچ گزارشی ذخیره نشده است", Toast.LENGTH_SHORT).show()
        }

        adapter = ReportListAdapter(this, reports)
        listViewReports.adapter = adapter

        listViewReports.setOnItemClickListener { _, _, position, _ ->
            val report = reports[position]
            val intent = Intent(this, ReportDetailActivity::class.java)
            intent.putExtra("report", report)
            startActivity(intent)
        }

        listViewReports.setOnItemLongClickListener { _, _, position, _ ->
            val report = reports[position]
            ReportStorage.deleteReport(this, report)
            Toast.makeText(this, "گزارش حذف شد", Toast.LENGTH_SHORT).show()
            adapter.remove(report)
            adapter.notifyDataSetChanged()
            true
        }
    }
}
