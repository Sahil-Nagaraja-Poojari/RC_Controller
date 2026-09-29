package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telemetry_logs")
data class TelemetryLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val speed: Float,
    val batteryPercent: Int,
    val motorTemp: Float,
    val rssiOrPing: Int,
    val eventNote: String
)
