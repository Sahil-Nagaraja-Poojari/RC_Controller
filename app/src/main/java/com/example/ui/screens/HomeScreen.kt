package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToController: () -> Unit,
    onNavigateToTelemetry: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val isConnected by viewModel.isConnected.collectAsState()
    val statusText by viewModel.connectionStatusText.collectAsState()
    val presets by viewModel.presets.collectAsState()

    var wifiIp by remember { mutableStateOf("192.168.4.1") }
    var wifiPort by remember { mutableStateOf("80") }
    var showBtDialog by remember { mutableStateOf(false) }

    val pairedDevices = remember {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            viewModel.getPairedBluetoothDevices()
        } else {
            emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CarbonDark, CarbonSurface)
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "App Logo",
                        tint = RacingRed,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "RC CAR COMMAND",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = statusText,
                            color = if (isConnected) SuccessGreen else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onNavigateToTelemetry) {
                        Icon(Icons.Default.Analytics, contentDescription = "Telemetry", tint = CyberCyan)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, contentDescription = "Wi-Fi", tint = CyberCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wi-Fi Control (ESP32/IP)", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        OutlinedTextField(
                            value = wifiIp,
                            onValueChange = { wifiIp = it },
                            label = { Text("Car IP Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { viewModel.connectWifi(wifiIp, wifiPort.toIntOrNull() ?: 80) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CarbonDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Connect Wi-Fi", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bluetooth, contentDescription = "Bluetooth", tint = CyberCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bluetooth Control (HC-05)", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Text(
                            text = if (pairedDevices.isNotEmpty()) "${pairedDevices.size} paired RC modules found" else "No paired devices found. Pair in Android Bluetooth settings.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Button(
                            onClick = { showBtDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RacingRed, contentColor = TextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Select Bluetooth Device", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToController,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) SuccessGreen else RacingRed
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.SportsMotorsports, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isConnected) "OPEN RC COCKPIT (CONNECTED)" else "OPEN RC COCKPIT (SIMULATION / DEMO)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SAVED CONNECTIONS",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CarbonCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (preset.type == "WIFI") Icons.Default.Wifi else Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = CyberCyan
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(preset.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Text("${preset.type} • ${preset.address}", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    if (preset.type == "WIFI") {
                                        viewModel.connectWifi(preset.address, preset.port)
                                    } else {
                                        viewModel.connectBluetooth(preset.address, preset.name)
                                    }
                                }
                            ) {
                                Text("Connect")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBtDialog) {
        AlertDialog(
            onDismissRequest = { showBtDialog = false },
            title = { Text("Select Paired Bluetooth Car") },
            text = {
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(pairedDevices) { device ->
                        TextButton(
                            onClick = {
                                showBtDialog = false
                                viewModel.connectBluetooth(device.address, device.name ?: "RC Car")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(device.name ?: "Unknown Device (${device.address})", color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBtDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
