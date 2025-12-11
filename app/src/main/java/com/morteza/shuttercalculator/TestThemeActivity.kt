package com.morteza.shuttercalculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

class TestThemeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_theme)

        val title = findViewById<TextView>(R.id.sectionTitle)
        val button = findViewById<Button>(R.id.primaryButton)

        // متن نمونه برای بررسی رنگ‌ها
        title.text = "این یک تیتر تستی است"
        button.text = "دکمه تستی"
    }
}
