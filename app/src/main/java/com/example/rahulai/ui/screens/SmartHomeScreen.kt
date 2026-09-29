package com.example.rahulai.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rahulai.data.local.DeviceEntity
import com.example.rahulai.ui.viewmodel.RahulAiViewModel
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
fun SmartHomeScreen(
    viewModel: RahulAiViewModel,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsState()
    val selectedRoom by viewModel.selectedRoomFilter.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val rooms = listOf("All", "Living Room", "Bedroom", "Kitchen", "Entrance", "Outdoor")

    val filteredDevices = if (selectedRoom == "All") {
        devices
    } else {
        devices.filter { it.room.equals(selectedRoom, ignoreCase = true) }
    }

    val activeCount = devices.count { it.isPowered }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Smart Home Center",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "$activeCount of ${devices.size} devices active",
                            fontSize = 12.sp,
                            color = NeonCyan
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.setAllDevices(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan.copy(alpha = 0.2f),
                            contentColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("all_on_btn")
                    ) {
                        Text("All On", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.setAllDevices(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentRose.copy(alpha = 0.2f),
                            contentColor = AccentRose
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("all_off_btn")
                    ) {
                        Text("All Off", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Room Filter Tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(rooms) { room ->
                    val isSelected = room == selectedRoom
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setRoomFilter(room) },
                        label = {
                            Text(
                                text = room,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = DarkNavy,
                            containerColor = SurfaceVariantDark,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = SurfaceVariantDark,
                            selectedBorderColor = NeonCyan
                        ),
                        modifier = Modifier.testTag("room_chip_$room")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Devices List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredDevices, key = { it.id }) { device ->
                    SmartDeviceCard(
                        device = device,
                        onToggle = { viewModel.toggleDevicePower(device.id, device.isPowered) },
                        onBrightnessChange = { viewModel.setDeviceBrightness(device.id, it) },
                        onTempChange = { viewModel.setDeviceTemperature(device.id, it) },
                        onColorChange = { viewModel.setDeviceColor(device.id, it) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }

        // Add Device FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = NeonCyan,
            contentColor = DarkNavy,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_device_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Smart Device")
        }
    }

    if (showAddDialog) {
        AddDeviceDialog(
            rooms = rooms.filter { it != "All" },
            onDismiss = { showAddDialog = false },
            onConfirm = { newDevice ->
                viewModel.addCustomDevice(newDevice)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SmartDeviceCard(
    device: DeviceEntity,
    onToggle: () -> Unit,
    onBrightnessChange: (Int) -> Unit,
    onTempChange: (Int) -> Unit,
    onColorChange: (String) -> Unit
) {
    val isPowered = device.isPowered
    val icon = getDeviceIcon(device.type)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPowered) SurfaceVariantDark else SurfaceDark
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPowered) NeonCyan.copy(alpha = 0.4f) else Color(0xFF1F2B42)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("device_card_${device.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon, Title & Room, Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isPowered) NeonCyan.copy(alpha = 0.2f) else Color(0xFF1E293B)
                        )
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = device.type,
                        tint = if (isPowered) NeonCyan else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${device.room} • ${if (isPowered) "Online (Active)" else "Standby"}",
                        color = if (isPowered) NeonCyan else TextMuted,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = isPowered,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkNavy,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceDark
                    ),
                    modifier = Modifier.testTag("switch_${device.id}")
                )
            }

            // Controls based on device type
            when (device.type) {
                "LIGHT" -> {
                    AnimatedVisibility(visible = isPowered) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Brightness",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${device.brightness}%",
                                    fontSize = 12.sp,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = device.brightness.toFloat(),
                                onValueChange = { onBrightnessChange(it.toInt()) },
                                valueRange = 5f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonCyan,
                                    inactiveTrackColor = Color(0xFF1F2B42)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick color preset circles
                            Text(
                                text = "Ambient Color:",
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            val colorPresets = listOf(
                                "#00F5D4" to "Cyan",
                                "#38BDF8" to "Blue",
                                "#F43F5E" to "Rose",
                                "#FBBF24" to "Warm",
                                "#A78BFA" to "Violet",
                                "#34D399" to "Green"
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                colorPresets.forEach { (hex, name) ->
                                    val isColorSelected = device.colorHex.equals(hex, ignoreCase = true)
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(parseHexColor(hex))
                                            .border(
                                                if (isColorSelected) 2.dp else 0.dp,
                                                Color.White,
                                                CircleShape
                                            )
                                            .clickable { onColorChange(hex) }
                                    ) {
                                        if (isColorSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = name,
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "AC" -> {
                    AnimatedVisibility(visible = isPowered) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = "Target Temperature",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${device.temperature}°C",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onTempChange(device.temperature - 1) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF1E293B), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Remove, "Decrease Temp", tint = TextPrimary)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    IconButton(
                                        onClick = { onTempChange(device.temperature + 1) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF1E293B), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Add, "Increase Temp", tint = TextPrimary)
                                    }
                                }
                            }

                            // Mode chips
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("COOL", "HEAT", "ECO", "FAN").forEach { modeName ->
                                    val isMode = device.mode == modeName
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isMode) ElectricBlue.copy(alpha = 0.25f) else Color(0xFF1E293B),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isMode) ElectricBlue else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = modeName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isMode) ElectricBlue else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                "LOCK" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPowered) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock Status",
                                tint = if (isPowered) AccentEmerald else AccentRose,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPowered) "Secured & Locked" else "Unlocked",
                                color = if (isPowered) AccentEmerald else AccentRose,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { onToggle() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPowered) Color(0xFF1E293B) else AccentEmerald
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isPowered) "Unlock Door" else "Lock Door",
                                fontSize = 12.sp,
                                color = if (isPowered) TextPrimary else DarkNavy
                            )
                        }
                    }
                }
                "TV" -> {
                    AnimatedVisibility(visible = isPowered) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            listOf("Streaming", "HDMI 1", "Gaming").forEach { source ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = source, fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
                "FAN" -> {
                    AnimatedVisibility(visible = isPowered) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = "Speed Level: ${device.brightness}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Slider(
                                value = device.brightness.toFloat(),
                                onValueChange = { onBrightnessChange(it.toInt()) },
                                valueRange = 1f..5f,
                                steps = 3,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonCyan
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddDeviceDialog(
    rooms: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (DeviceEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedRoom by remember { mutableStateOf(rooms.firstOrNull() ?: "Living Room") }
    var selectedType by remember { mutableStateOf("LIGHT") }

    val deviceTypes = listOf("LIGHT", "AC", "LOCK", "TV", "FAN", "PLUG", "CAMERA")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Smart Device", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Device Name (e.g. Balcony Light)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Room:", fontSize = 12.sp, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(rooms) { r ->
                        FilterChip(
                            selected = r == selectedRoom,
                            onClick = { selectedRoom = r },
                            label = { Text(r, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Device Type:", fontSize = 12.sp, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(deviceTypes) { t ->
                        FilterChip(
                            selected = t == selectedType,
                            onClick = { selectedType = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val id = "dev_${System.currentTimeMillis()}"
                        onConfirm(
                            DeviceEntity(
                                id = id,
                                name = name,
                                room = selectedRoom,
                                type = selectedType,
                                isPowered = false
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkNavy),
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = SurfaceDark
    )
}

fun getDeviceIcon(type: String): ImageVector {
    return when (type) {
        "LIGHT" -> Icons.Default.Lightbulb
        "AC" -> Icons.Default.AcUnit
        "LOCK" -> Icons.Default.Lock
        "TV" -> Icons.Default.Tv
        "FAN" -> Icons.Default.WindPower
        "PLUG" -> Icons.Default.Coffee
        "CAMERA" -> Icons.Default.Videocam
        else -> Icons.Default.Sensors
    }
}

fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        NeonCyan
    }
}
