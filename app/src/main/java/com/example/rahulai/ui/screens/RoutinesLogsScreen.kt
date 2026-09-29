package com.example.rahulai.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rahulai.data.local.CommandLogEntity
import com.example.rahulai.data.local.RoutineEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesLogsScreen(
    viewModel: RahulAiViewModel,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.routines.collectAsState()
    val logs by viewModel.recentLogs.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "Automations & Logs",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            },
            actions = {
                IconButton(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier.testTag("clear_logs_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = "Clear Logs",
                        tint = TextSecondary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Routines Header
            item {
                Text(
                    text = "Smart Home Voice Routines:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NeonCyan
                )
            }

            // Routines List
            items(routines, key = { it.id }) { routine ->
                RoutineCard(
                    routine = routine,
                    onExecute = { viewModel.executeRoutine(routine.id) }
                )
            }

            // Logs Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Voice & Command History:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ElectricBlue
                    )
                    Text(
                        text = "${logs.size} recorded",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Activity Logs
            if (logs.isEmpty()) {
                item {
                    Text(
                        text = "Abhi koi commands execute nahi hue hain. Voice commands try karein!",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(logs, key = { it.id }) { log ->
                    CommandLogItem(log = log)
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun RoutineCard(
    routine: RoutineEntity,
    onExecute: () -> Unit
) {
    val icon = when (routine.iconKey) {
        "NIGHT" -> Icons.Default.DarkMode
        "MORNING" -> Icons.Default.WbSunny
        "PARTY" -> Icons.Default.Celebration
        else -> Icons.AutoMirrored.Filled.ExitToApp
    }

    val iconColor = when (routine.iconKey) {
        "NIGHT" -> VioletPulse
        "MORNING" -> AccentAmber
        "PARTY" -> AccentRose
        else -> ElectricBlue
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2B42)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f))
            ) {
                Icon(icon, contentDescription = routine.title, tint = iconColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = routine.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
                Text(
                    text = "Say: \"Rahul, ${routine.triggerPhrase}\"",
                    fontSize = 11.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onExecute,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkNavy),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("run_routine_${routine.id}")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Run", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CommandLogItem(log: CommandLogEntity) {
    val timeFormatted = remember(log.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                when (log.actionType) {
                                    "SMART_HOME" -> NeonCyan.copy(alpha = 0.2f)
                                    "SYSTEM_CONTROL" -> AccentAmber.copy(alpha = 0.2f)
                                    "ROUTINE" -> VioletPulse.copy(alpha = 0.2f)
                                    else -> ElectricBlue.copy(alpha = 0.2f)
                                },
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = log.actionType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (log.actionType) {
                                "SMART_HOME" -> NeonCyan
                                "SYSTEM_CONTROL" -> AccentAmber
                                "ROUTINE" -> VioletPulse
                                else -> ElectricBlue
                            }
                        )
                    }

                    if (log.targetDeviceOrFeature != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = log.targetDeviceOrFeature,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = timeFormatted,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "\"${log.query}\"",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = log.response,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}
