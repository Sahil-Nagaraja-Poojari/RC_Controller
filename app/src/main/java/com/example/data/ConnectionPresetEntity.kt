package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_presets")
data class ConnectionPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val type: String, // "WIFI" or "BLUETOOTH"
    val address: String, // IP address or Bluetooth MAC address
    val port: Int = 80,
    val isDefault: Boolean = false,
    val lastConnected: Long = System.currentTimeMillis()
)
