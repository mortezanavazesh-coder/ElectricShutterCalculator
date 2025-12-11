package com.morteza.shuttercalculator

import org.junit.Assert.assertEquals
import org.junit.Test

class ReportModelTest {

    @Test
    fun modelHoldsValues() {
        val report = ReportModel(
            id = 1L,
            customerName = "مرتضی",
            date = "2025-12-10",
            height = 200f,
            width = 150f,
            area = "30m2",
            blade = "تیغه 80",
            motor = "موتور توبولار",
            shaft = "شفت 70",
            box = "جعبه فلزی",
            install = "نصب استاندارد",
            welding = "بدون جوشکاری",
            transport = "ارسال رایگان",
            extras = "ریموت اضافه",
            total = "15000000"
        )
        assertEquals("مرتضی", report.customerName)
        assertEquals("15000000", report.total)
    }
}
