package com.morteza.shuttercalculator

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        val btnSave = findViewById<MaterialButton>(R.id.btnSave)
        val btnCancel = findViewById<MaterialButton>(R.id.btnCancel)

        btnSave.setOnClickListener {
            Toast.makeText(this, "ذخیره شد ✅", Toast.LENGTH_SHORT).show()
        }

        btnCancel.setOnClickListener {
            Toast.makeText(this, "لغو شد ❌", Toast.LENGTH_SHORT).show()
        }
    }
}
