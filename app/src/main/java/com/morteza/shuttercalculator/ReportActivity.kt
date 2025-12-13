package com.morteza.shuttercalculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morteza.shuttercalculator.utils.ReportStorage

class ReportActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReportAdapter
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        recyclerView = findViewById(R.id.recyclerReports)
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnBack = findViewById(R.id.buttonBackToMain)
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val reports = ReportStorage.loadReports(this)

        adapter = ReportAdapter(
            reports.toMutableList(),
            onDeleteClick = { report ->
                ReportStorage.deleteReport(this, report)
                refreshReports()
                Toast.makeText(this, "گزارش حذف شد", Toast.LENGTH_SHORT).show()
            },
            onItemClick = { report ->
                val intent = Intent(this, ReportDetailActivity::class.java)
                intent.putExtra("report", report) // پاس دادن مدل به صورت Serializable
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
