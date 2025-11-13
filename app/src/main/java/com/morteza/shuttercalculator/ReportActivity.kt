package com.morteza.shuttercalculator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.morteza.shuttercalculator.utils.PrefsHelper
import kotlinx.android.synthetic.main.activity_reports.*
import kotlinx.coroutines.*

class ReportsActivity : AppCompatActivity() {

    private var adapter: ReportsAdapter? = null
    private val TAG = "ReportsActivity"
    private val scope = MainScope() // cancels onDestroy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_reports)
        } catch (e: Exception) {
            Log.e(TAG, "setContentView failed", e)
            finish()
            return
        }

        rvReports.layoutManager = LinearLayoutManager(this)
        adapter = ReportsAdapter(emptyList()) { title ->
            // confirm then delete
            AlertDialog.Builder(this)
                .setTitle("حذف")
                .setMessage("آیا از حذف \"$title\" مطمئنی؟")
                .setPositiveButton("حذف") { dlg, _ ->
                    dlg.dismiss()
                    deleteReport(title)
                }
                .setNegativeButton("انصراف", null)
                .show()
        }
        rvReports.adapter = adapter

        btnAddReport.setOnClickListener {
            val title = etReportTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "عنوان را وارد کنید", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addReport(title)
        }

        refreshList()
    }

    private fun addReport(title: String) {
        scope.launch {
            try {
                val exists = withContext(Dispatchers.IO) {
                    PrefsHelper.optionExists(this@ReportsActivity, "گزارش", title)
                }
                if (exists) {
                    Toast.makeText(this@ReportsActivity, "چنین گزارشی وجود دارد", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    // PrefsHelper.addOption requires price; برای گزارش از 0f استفاده می‌کنیم
                    PrefsHelper.addOption(this@ReportsActivity, "گزارش", title, 0f)
                    // مثلاً ذخیره متا (timestamp) امن:
                    PrefsHelper.saveFloat(this@ReportsActivity, "گزارش_meta_$title", System.currentTimeMillis().toFloat())
                }
                etReportTitle.text.clear()
                refreshList()
                Toast.makeText(this@ReportsActivity, "افزوده شد", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "addReport failed", e)
                Toast.makeText(this@ReportsActivity, "خطا در افزودن", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteReport(title: String) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    PrefsHelper.removeOption(this@ReportsActivity, "گزارش", title)
                    PrefsHelper.saveFloat(this@ReportsActivity, "گزارش_meta_$title", 0f)
                }
                refreshList()
                Toast.makeText(this@ReportsActivity, "حذف شد", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "deleteReport failed", e)
                Toast.makeText(this@ReportsActivity, "خطا در حذف", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshList() {
        scope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    PrefsHelper.getSortedOptionList(this@ReportsActivity, "گزارش")
                }
                adapter?.update(list)
                emptyReports.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                rvReports.post { rvReports.requestLayout() }
            } catch (e: Exception) {
                Log.e(TAG, "refreshList failed", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
