package com.morteza.shuttercalculator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morteza.shuttercalculator.utils.ReportStorage

class ReportActivity : AppCompatActivity() {

    private lateinit var recyclerReports: RecyclerView
    private lateinit var adapter: ReportAdapter
    private lateinit var reports: MutableList<ReportModel>
    private lateinit var textEmptyReports: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        recyclerReports = findViewById(R.id.recyclerReports)
        textEmptyReports = findViewById(R.id.textEmptyReports)

        reports = ReportStorage.loadReports(this).toMutableList()
        adapter = ReportAdapter(
            reports,
            onDeleteClick = { report ->
                ReportStorage.deleteReport(this, report)
                reports.remove(report)
                updateAdapter()
                updateEmptyState()
                Toast.makeText(this, "گزارش حذف شد", Toast.LENGTH_SHORT).show()
            },
            onItemClick = { report ->
                val intent = Intent(this, ReportDetailActivity::class.java)
                intent.putExtra("report", report)
                startActivity(intent)
            }
        )

        recyclerReports.layoutManager = LinearLayoutManager(this)
        recyclerReports.adapter = adapter

        updateEmptyState()

        findViewById<View>(R.id.buttonBackToMain).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // بارگذاری مجدد برای اطمینان از تازه بودن لیست پس از بازگشت از صفحات دیگر
        reports = ReportStorage.loadReports(this).toMutableList()
        updateAdapter()
        updateEmptyState()
    }

    private fun updateAdapter() {
        adapter.updateReports(reports)
    }

    private fun updateEmptyState() {
        val isEmpty = reports.isEmpty()
        textEmptyReports.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerReports.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
