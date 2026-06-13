package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "CALC" or "GRAPH" or "ODE"
    val expression: String, // main formula or equation e.g. "sin(x) + cos(x)" or "t - y"
    val secondaryExpression: String? = null, // for second graph line e.g. "x^2"
    val initialX: Double? = null, // for ODE starting point or graphs limits
    val initialY: Double? = null, // for ODE initial condition y0
    val resultText: String? = null, // calculation result if applicable
    val timestamp: Long = System.currentTimeMillis()
)
