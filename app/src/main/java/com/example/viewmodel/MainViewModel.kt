package com.example.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.connection.BluetoothConnectionHelper
import com.example.connection.WifiConnectionHelper
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.ConnectionPresetEntity
import com.example.data.TelemetryLogEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    val presets: StateFlow<List<ConnectionPresetEntity>>
    val telemetryLogs: StateFlow<List<TelemetryLogEntity>>

    private val bluetoothHelper = BluetoothConnectionHelper(application)
    private val wifiHelper = WifiConnectionHelper()
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _activeConnectionType = MutableStateFlow("WIFI")
    val activeConnectionType: StateFlow<String> = _activeConnectionType.asStateFlow()

    private val _connectionStatusText = MutableStateFlow("Disconnected")
    val connectionStatusText: StateFlow<String> = _connectionStatusText.asStateFlow()

    private val _throttle = MutableStateFlow(0f)
    val throttle: StateFlow<Float> = _throttle.asStateFlow()

    private val _steering = MutableStateFlow(0f)
    val steering: StateFlow<Float> = _steering.asStateFlow()

    private val _currentGear = MutableStateFlow("P")
    val currentGear: StateFlow<String> = _currentGear.asStateFlow()

    private val _headlightsOn = MutableStateFlow(false)
    val headlightsOn: StateFlow<Boolean> = _headlightsOn.asStateFlow()

    private val _sirenOn = MutableStateFlow(false)
    val sirenOn: StateFlow<Boolean> = _sirenOn.asStateFlow()

    private val _turboOn = MutableStateFlow(false)
    val turboOn: StateFlow<Boolean> = _turboOn.asStateFlow()

    private val _speed = MutableStateFlow(0f)
    val speed: StateFlow<Float> = _speed.asStateFlow()

    private val _batteryPercent = MutableStateFlow(88)
    val batteryPercent: StateFlow<Int> = _batteryPercent.asStateFlow()

    private val _motorTemp = MutableStateFlow(34.5f)
    val motorTemp: StateFlow<Float> = _motorTemp.asStateFlow()

    private val _signalRssi = MutableStateFlow(-42)
    val signalRssi: StateFlow<Int> = _signalRssi.asStateFlow()

    private val _streamUrl = MutableStateFlow("http://192.168.4.1:81/stream")
    val streamUrl: StateFlow<String> = _streamUrl.asStateFlow()

    private val _streamEnabled = MutableStateFlow(true)
    val streamEnabled: StateFlow<Boolean> = _streamEnabled.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(true)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private var telemetryJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AppRepository(db.carDao())
        presets = repository.presets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        telemetryLogs = repository.telemetryLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        startTelemetrySimulation()
    }

    fun triggerHaptic(milliseconds: Long = 30) {
        if (!_hapticEnabled.value) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(milliseconds)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun connectWifi(ip: String, port: Int) {
        viewModelScope.launch {
            _connectionStatusText.value = "Connecting to Wi-Fi $ip:$port..."
            _activeConnectionType.value = "WIFI"
            val success = wifiHelper.testConnection(ip, port)
            if (success) {
                _isConnected.value = true
                _connectionStatusText.value = "Connected (Wi-Fi: $ip)"
                triggerHaptic(60)
                repository.savePreset(ConnectionPresetEntity(name = "Wi-Fi Car ($ip)", type = "WIFI", address = ip, port = port))
            } else {
                _isConnected.value = false
                _connectionStatusText.value = "Connection Failed"
            }
        }
    }

    fun connectBluetooth(deviceAddress: String, deviceName: String) {
        viewModelScope.launch {
            _connectionStatusText.value = "Connecting to BT $deviceName..."
            _activeConnectionType.value = "BLUETOOTH"
            val success = bluetoothHelper.connect(deviceAddress)
            if (success) {
                _isConnected.value = true
                _connectionStatusText.value = "Connected (BT: $deviceName)"
                triggerHaptic(60)
                repository.savePreset(ConnectionPresetEntity(name = deviceName, type = "BLUETOOTH", address = deviceAddress))
            } else {
                _isConnected.value = false
                _connectionStatusText.value = "Bluetooth Connection Failed"
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            if (_activeConnectionType.value == "BLUETOOTH") {
                bluetoothHelper.close()
            }
            _isConnected.value = false
            _connectionStatusText.value = "Disconnected"
            _throttle.value = 0f
            _steering.value = 0f
            triggerHaptic(40)
        }
    }

    fun sendControlCommand(cmd: String) {
        triggerHaptic(20)
        viewModelScope.launch {
            if (_activeConnectionType.value == "BLUETOOTH") {
                bluetoothHelper.sendCommand(cmd)
            } else {
                wifiHelper.sendCommand("192.168.4.1", 80, cmd)
            }
        }
    }

    fun setGear(gear: String) {
        _currentGear.value = gear
        triggerHaptic(30)
        when (gear) {
            "P" -> {
                _throttle.value = 0f
                sendControlCommand("S")
            }
            "R" -> sendControlCommand("R")
            "N" -> sendControlCommand("N")
            "D" -> sendControlCommand("F")
        }
    }

    fun setThrottle(value: Float) {
        _throttle.value = value
        if (value > 0) {
            sendControlCommand("F")
        } else if (value < 0) {
            sendControlCommand("B")
        } else {
            sendControlCommand("S")
        }
    }

    fun setSteering(value: Float) {
        _steering.value = value
        if (value < -20f) {
            sendControlCommand("L")
        } else if (value > 20f) {
            sendControlCommand("R")
        }
    }

    fun toggleHeadlights() {
        _headlightsOn.update { !it }
        triggerHaptic(25)
        sendControlCommand(if (_headlightsOn.value) "H_ON" else "H_OFF")
    }

    fun toggleSiren() {
        _sirenOn.update { !it }
        triggerHaptic(25)
        sendControlCommand(if (_sirenOn.value) "SIREN_ON" else "SIREN_OFF")
    }

    fun toggleTurbo() {
        _turboOn.update { !it }
        triggerHaptic(35)
    }

    fun toggleRecording() {
        _isRecording.update { !it }
        triggerHaptic(40)
    }

    fun toggleStream() {
        _streamEnabled.update { !it }
        triggerHaptic(20)
    }

    @SuppressLint("MissingPermission")
    fun getPairedBluetoothDevices() = bluetoothHelper.getPairedDevices()

    private fun startTelemetrySimulation() {
        telemetryJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isConnected.value) {
                    val currentThrottle = _throttle.value
                    val targetSpeed = kotlin.math.abs(currentThrottle) * 0.25f
                    _speed.value = (_speed.value * 0.7f) + (targetSpeed * 0.3f)
                    
                    if (currentThrottle != 0f) {
                        _motorTemp.update { (it + 0.05f).coerceAtMost(65.0f) }
                        if (_batteryPercent.value > 5 && System.currentTimeMillis() % 15000 == 0L) {
                            _batteryPercent.update { (it - 1).coerceAtLeast(0) }
                        }
                    } else {
                        _motorTemp.update { (it - 0.02f).coerceAtLeast(30.0f) }
                    }

                    if (System.currentTimeMillis() % 10000 < 1000) {
                        repository.logTelemetry(
                            TelemetryLogEntity(
                                speed = _speed.value,
                                batteryPercent = _batteryPercent.value,
                                motorTemp = _motorTemp.value,
                                rssiOrPing = _signalRssi.value,
                                eventNote = "Nominal Drive"
                            )
                        )
                    }
                }
            }
        }
    }
}
