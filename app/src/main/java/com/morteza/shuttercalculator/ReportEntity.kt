package com.morteza.shuttercalculator

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val heightCm: Double,
    val widthCm: Double,
    val breakdown: String,
    val totalPriceToman: Long,
    val createdAt: Long = System.currentTimeMillis()
)
