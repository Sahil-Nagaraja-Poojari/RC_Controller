package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "car_profiles")
data class CarProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val maxSpeed: Int = 100,
    val steeringSensitivity: Int = 50,
    val hapticEnabled: Boolean = true,
    val invertSteering: Boolean = false,
    val isSelected: Boolean = false
)
