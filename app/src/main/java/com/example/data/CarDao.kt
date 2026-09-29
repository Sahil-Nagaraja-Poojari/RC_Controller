package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Query("SELECT * FROM connection_presets ORDER BY lastConnected DESC")
    fun getAllPresets(): Flow<List<ConnectionPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: ConnectionPresetEntity)

    @Delete
    suspend fun deletePreset(preset: ConnectionPresetEntity)

    @Query("SELECT * FROM telemetry_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentTelemetry(): Flow<List<TelemetryLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelemetry(log: TelemetryLogEntity)

    @Query("SELECT * FROM car_profiles")
    fun getAllProfiles(): Flow<List<CarProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: CarProfileEntity)

    @Update
    suspend fun updateProfile(profile: CarProfileEntity)
}
