package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun ControllerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val isConnected by viewModel.isConnected.collectAsState()
    val statusText by viewModel.connectionStatusText.collectAsState()
    val speed by viewModel.speed.collectAsState()
    val battery by viewModel.batteryPercent.collectAsState()
    val motorTemp by viewModel.motorTemp.collectAsState()
    val signalRssi by viewModel.signalRssi.collectAsState()
    val gear by viewModel.currentGear.collectAsState()
    val headlights by viewModel.headlightsOn.collectAsState()
    val siren by viewModel.sirenOn.collectAsState()
    val turbo by viewModel.turboOn.collectAsState()
    val streamEnabled by viewModel.streamEnabled.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()

    var isForwardPressed by remember { mutableStateOf(false) }
    var isBackwardPressed by remember { mutableStateOf(false) }
    var isLeftPressed by remember { mutableStateOf(false) }
    var isRightPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isForwardPressed, isBackwardPressed, isLeftPressed, isRightPressed) {
        while (true) {
            if (isForwardPressed) {
                viewModel.setThrottle(100f)
                viewModel.triggerHaptic(15)
            } else if (isBackwardPressed) {
                viewModel.setThrottle(-100f)
                viewModel.triggerHaptic(15)
            } else {
                if (!isLeftPressed && !isRightPressed) {
                    viewModel.setThrottle(0f)
                }
            }

            if (isLeftPressed) {
                viewModel.setSteering(-100f)
                viewModel.triggerHaptic(15)
            } else if (isRightPressed) {
                viewModel.setSteering(100f)
                viewModel.triggerHaptic(15)
            } else {
                viewModel.setSteering(0f)
            }
            delay(100)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
    ) {
        if (streamEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0C10))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    for (x in 0..w.toInt() step 100) {
                        drawLine(Color(0x1500E5FF), Offset(x.toFloat(), 0f), Offset(x.toFloat(), h), 1f)
                    }
                    for (y in 0..h.toInt() step 100) {
                        drawLine(Color(0x1500E5FF), Offset(0f, y.toFloat()), Offset(w, y.toFloat()), 1f)
                    }

                    val cx = w / 2f
                    val cy = h / 2f
                    drawCircle(Color(0x4000E5FF), radius = 40f, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
                    drawLine(Color(0x6000E5FF), Offset(cx - 20f, cy), Offset(cx + 20f, cy), 2f)
                    drawLine(Color(0x6000E5FF), Offset(cx, cy - 20f), Offset(cx, cy + 20f), 2f)
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0x80121417),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SuccessGreen))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("FPV LIVE STREAM 720P 60FPS", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isRecording) {
                        Surface(
                            color = Color(0x80C70000),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RacingRed))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("REC", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(CarbonSurface, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }

                    Surface(
                        color = CarbonSurface,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isConnected) SuccessGreen else RacingRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(statusText, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TelemetryBadge(label = "SPD", value = "${String.format("%.1f", speed)} km/h", color = CyberCyan)
                    TelemetryBadge(label = "BAT", value = "$battery%", color = if (battery > 20) SuccessGreen else RacingRed)
                    TelemetryBadge(label = "TMP", value = "${String.format("%.1f", motorTemp)}°C", color = if (motorTemp < 50f) SuccessGreen else WarningYellow)
                    TelemetryBadge(label = "SIG", value = "$signalRssi dBm", color = CyberCyan)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.toggleStream() },
                        modifier = Modifier.size(40.dp).background(if (streamEnabled) CarbonSurface else CarbonCard, CircleShape)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Toggle Stream", tint = if (streamEnabled) CyberCyan else TextSecondary)
                    }
                    IconButton(
                        onClick = { viewModel.toggleRecording() },
                        modifier = Modifier.size(40.dp).background(if (isRecording) RacingRedDark else CarbonSurface, CircleShape)
                    ) {
                        Icon(Icons.Default.FiberSmartRecord, contentDescription = "Record", tint = TextPrimary)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccessoryButton(
                        icon = if (headlights) Icons.Default.WbSunny else Icons.Default.Brightness2,
                        label = "Lights",
                        active = headlights,
                        onClick = { viewModel.toggleHeadlights() }
                    )
                    AccessoryButton(
                        icon = Icons.Default.VolumeUp,
                        label = "Siren",
                        active = siren,
                        onClick = { viewModel.toggleSiren() }
                    )
                    AccessoryButton(
                        icon = Icons.Default.FlashOn,
                        label = "Turbo",
                        active = turbo,
                        onClick = { viewModel.toggleTurbo() }
                    )
                }

                Surface(
                    color = Color(0xCC121417),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("P", "R", "N", "D").forEach { g ->
                            val selected = gear == g
                            Button(
                                onClick = { viewModel.setGear(g) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) RacingRed else CarbonCard,
                                    contentColor = TextPrimary
                                ),
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(g, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.setGear("P")
                        viewModel.setThrottle(0f)
                        viewModel.setSteering(0f)
                        viewModel.triggerHaptic(50)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RacingRedDark, contentColor = TextPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("E-STOP", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ArcadeButton(
                        icon = Icons.Default.KeyboardArrowLeft,
                        label = "LEFT",
                        pressed = isLeftPressed,
                        onTouchChanged = { isLeftPressed = it }
                    )
                    ArcadeButton(
                        icon = Icons.Default.KeyboardArrowRight,
                        label = "RIGHT",
                        pressed = isRightPressed,
                        onTouchChanged = { isRightPressed = it }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ArcadeButton(
                        icon = Icons.Default.KeyboardArrowUp,
                        label = "FWD",
                        pressed = isForwardPressed,
                        onTouchChanged = { isForwardPressed = it }
                    )
                    ArcadeButton(
                        icon = Icons.Default.KeyboardArrowDown,
                        label = "REV",
                        pressed = isBackwardPressed,
                        onTouchChanged = { isBackwardPressed = it }
                    )
                }
            }
        }
    }
}

@Composable
fun TelemetryBadge(label: String, value: String, color: Color) {
    Surface(
        color = Color(0xCC121417),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AccessoryButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (active) CyberCyan else Color(0xCC121417),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .size(width = 64.dp, height = 48.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = if (active) CarbonDark else TextPrimary, modifier = Modifier.size(20.dp))
            Text(label, color = if (active) CarbonDark else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ArcadeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    pressed: Boolean,
    onTouchChanged: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(if (pressed) RacingRed else CarbonSurface)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onTouchChanged(true)
                        tryAwaitRelease()
                        onTouchChanged(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (pressed) TextPrimary else CyberCyan,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = label,
                color = if (pressed) TextPrimary else TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
