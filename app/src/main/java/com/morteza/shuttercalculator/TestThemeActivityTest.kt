package com.morteza.shuttercalculator

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestThemeActivityTest {

    @Rule
    @JvmField
    val rule = ActivityTestRule(TestThemeActivity::class.java)

    @Test
    fun testThemeActivityLaunches() {
        // اگر اکتیویتی کرش کند، تست fail می‌شود
        rule.activity
    }
}
