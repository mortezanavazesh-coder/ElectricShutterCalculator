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

        if (reports.isEmpty()) {
            textEmptyReports.visibility = View.VISIBLE
            recyclerReports.visibility = View.GONE
        } else {
            textEmptyReports.visibility = View.GONE
            recyclerReports.visibility = View.VISIBLE
        }

        adapter = ReportAdapter(
            reports,
            onDeleteClick = { report ->
                ReportStorage.deleteReport(this, report)
                reports.remove(report)
                adapter.updateReports(reports)

                // مدیریت حالت خالی بعد از حذف
                if (reports.isEmpty()) {
                    textEmptyReports.visibility = View.VISIBLE
                    recyclerReports.visibility = View.GONE
                }

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

        // دکمه بازگشت به صفحه اصلی
        findViewById<View>(R.id.buttonBackToMain).setOnClickListener {
            finish()
        }
    }
}
