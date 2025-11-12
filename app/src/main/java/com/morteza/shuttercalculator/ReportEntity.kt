package com.morteza.shuttercalculator

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "customerName")
    val customerName: String,

    @ColumnInfo(name = "heightCm")
    val heightCm: Int,

    @ColumnInfo(name = "widthCm")
    val widthCm: Int,

    @ColumnInfo(name = "breakdown")
    val breakdown: String,

    @ColumnInfo(name = "totalPriceToman")
    val totalPriceToman: Long,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long
)
