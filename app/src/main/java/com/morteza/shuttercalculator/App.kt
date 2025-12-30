package com.morteza.shuttercalculator

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // قفل کردن تم روشن برای کل اپ
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
