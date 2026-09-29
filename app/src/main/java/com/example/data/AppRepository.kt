package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val carDao: CarDao) {
    val presets: Flow<List<ConnectionPresetEntity>> = carDao.getAllPresets()
    val telemetryLogs: Flow<List<TelemetryLogEntity>> = carDao.getRecentTelemetry()
    val profiles: Flow<List<CarProfileEntity>> = carDao.getAllProfiles()

    suspend fun savePreset(preset: ConnectionPresetEntity) = carDao.insertPreset(preset)
    suspend fun deletePreset(preset: ConnectionPresetEntity) = carDao.deletePreset(preset)
    suspend fun logTelemetry(log: TelemetryLogEntity) = carDao.insertTelemetry(log)
    suspend fun saveProfile(profile: CarProfileEntity) = carDao.insertProfile(profile)
    suspend fun updateProfile(profile: CarProfileEntity) = carDao.updateProfile(profile)
}
