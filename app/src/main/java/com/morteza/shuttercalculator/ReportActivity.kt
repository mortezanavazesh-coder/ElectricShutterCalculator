package com.morteza.shuttercalculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morteza.shuttercalculator.utils.PrefsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportActivity : AppCompatActivity() {

    private lateinit var rvReports: RecyclerView
    private lateinit var btnAddReport: Button
    private lateinit var etReportTitle: EditText
    private lateinit var emptyReports: TextView
    private lateinit var adapter: ReportsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        rvReports = findViewById(R.id.rvReports)
        btnAddReport = findViewById(R.id.btnAddReport)
        etReportTitle = findViewById(R.id.etReportTitle)
        emptyReports = findViewById(R.id.emptyReports)

        rvReports.layoutManager = LinearLayoutManager(this)
        adapter = ReportsAdapter(emptyList(), onDelete = { title ->
            AlertDialog.Builder(this)
                .setTitle("حذف گزارش")
                .setMessage("آیا از حذف \"$title\" مطمئن هستید؟")
                .setPositiveButton("حذف") { dialog, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        PrefsHelper.removeOption(this@ReportActivity, "گزارش", title)
                        withContext(Dispatchers.Main) { refreshList() }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("انصراف", null)
                .show()
        })
        rvReports.adapter = adapter

        btnAddReport.setOnClickListener {
            val title = etReportTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "عنوان گزارش را وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.IO).launch {
                val exists = PrefsHelper.optionExists(this@ReportActivity, "گزارش", title)
                withContext(Dispatchers.Main) {
                    if (exists) {
                        Toast.makeText(this@ReportActivity, "این عنوان قبلاً وجود دارد", Toast.LENGTH_SHORT).show()
                    } else {
                        PrefsHelper.addOption(this@ReportActivity, "گزارش", title, 0f)
                        PrefsHelper.saveMetaTimestamp(this@ReportActivity, "گزارش", title, System.currentTimeMillis())
                        etReportTitle.setText("")
                        refreshList()
                        Toast.makeText(this@ReportActivity, "گزارش ذخیره شد", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        refreshList()
    }

    private fun refreshList() {
        CoroutineScope(Dispatchers.IO).launch {
            val list = PrefsHelper.getSortedOptionList(this@ReportActivity, "گزارش")
            withContext(Dispatchers.Main) {
                adapter.update(list)
                emptyReports.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
