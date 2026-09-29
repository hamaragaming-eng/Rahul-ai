package com.example.rahulai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rahulai.ui.viewmodel.RahulAiViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPulse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemControlScreen(
    viewModel: RahulAiViewModel,
    modifier: Modifier = Modifier
) {
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val volumeInfo by viewModel.volumeInfo.collectAsState()

    val mediaPct = if (volumeInfo.maxMediaVolume > 0) {
        (volumeInfo.mediaVolume * 100 / volumeInfo.maxMediaVolume)
    } else 50

    val ringPct = if (volumeInfo.maxRingVolume > 0) {
        (volumeInfo.ringVolume * 100 / volumeInfo.maxRingVolume)
    } else 50

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "System Control Center",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Flashlight / Torch Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFlashlightOn) SurfaceVariantDark else SurfaceDark
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isFlashlightOn) AccentAmber else Color(0xFF1F2B42)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("flashlight_control_card")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFlashlightOn) AccentAmber else Color(0xFF1E293B)
                                )
                                .clickable { viewModel.toggleFlashlight() }
                                .testTag("flashlight_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                contentDescription = "Toggle Torch",
                                tint = if (isFlashlightOn) DarkNavy else TextMuted,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Device Flashlight / Torch",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isFlashlightOn) "Active (Glowing)" else "Turned Off",
                                color = if (isFlashlightOn) AccentAmber else TextMuted,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.toggleFlashlight() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFlashlightOn) AccentAmber else SurfaceVariantDark,
                                contentColor = if (isFlashlightOn) DarkNavy else NeonCyan
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isFlashlightOn) "Turn Off" else "Turn On",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 2. Battery Health & Telemetry Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2B42)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AccentEmerald.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                        contentDescription = "Battery",
                                        tint = AccentEmerald,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Battery Status",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (batteryInfo.isCharging) "Charging active" else "Discharging / Normal",
                                        fontSize = 12.sp,
                                        color = if (batteryInfo.isCharging) AccentEmerald else TextMuted
                                    )
                                }
                            }

                            Text(
                                text = "${batteryInfo.level}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentEmerald
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { batteryInfo.level / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = AccentEmerald,
                            trackColor = Color(0xFF1E293B)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Battery Temp: ${batteryInfo.temperatureC}°C",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "Power Saver Settings",
                                fontSize = 12.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { viewModel.systemManager.openBatterySettings() }
                            )
                        }
                    }
                }
            }

            // 3. Audio & Volume Sliders Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2B42)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ElectricBlue.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Volume",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Audio & Volume Controls",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            }

                            Button(
                                onClick = { viewModel.muteMedia() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E293B),
                                    contentColor = AccentRose
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("mute_button")
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeMute,
                                    contentDescription = "Mute",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mute", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Media Volume
                        Text(
                            text = "Media Volume: $mediaPct%",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Slider(
                            value = mediaPct.toFloat(),
                            onValueChange = { viewModel.setMediaVolume(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricBlue,
                                activeTrackColor = ElectricBlue,
                                inactiveTrackColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Ring Volume
                        Text(
                            text = "Ring / Notification Volume: $ringPct%",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Slider(
                            value = ringPct.toFloat(),
                            onValueChange = { viewModel.setRingVolume(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 4. Quick Android Settings Shortcuts
            item {
                Text(
                    text = "System Shortcuts / Setting Launchers:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    SystemShortcutCard(
                        icon = Icons.Default.Wifi,
                        title = "Wi-Fi",
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.systemManager.openWifiSettings() }
                    )
                    SystemShortcutCard(
                        icon = Icons.Default.Bluetooth,
                        title = "Bluetooth",
                        color = NeonCyan,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.systemManager.openBluetoothSettings() }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    SystemShortcutCard(
                        icon = Icons.Default.Tune,
                        title = "Sound Settings",
                        color = VioletPulse,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.systemManager.openSoundSettings() }
                    )
                    SystemShortcutCard(
                        icon = Icons.Default.BrightnessMedium,
                        title = "Display",
                        color = AccentAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.systemManager.openDisplaySettings() }
                    )
                }
            }

            // 5. Haptic feedback test
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = "Vibrate", tint = NeonCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Test Haptic Vibration", color = TextPrimary, fontSize = 14.sp)
                        }
                        Button(
                            onClick = { viewModel.systemManager.vibrate(60) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkNavy),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Vibrate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun SystemShortcutCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f))
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = "Open System", fontSize = 10.sp, color = TextMuted)
            }
        }
    }
}
