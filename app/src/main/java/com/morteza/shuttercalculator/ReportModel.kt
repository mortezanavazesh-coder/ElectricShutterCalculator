package com.morteza.shuttercalculator

import java.io.Serializable

data class ReportModel(
    val id: Long,
    val title: String,
    val date: String,
    val price: Float,
    val description: String
) : Serializable
