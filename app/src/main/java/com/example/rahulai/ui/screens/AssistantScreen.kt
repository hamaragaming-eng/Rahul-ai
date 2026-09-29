package com.example.rahulai.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.rahulai.data.ai.ChatMessage
import com.example.rahulai.data.ai.MessageSender
import com.example.rahulai.ui.components.AiVoiceWaveOrb
import com.example.rahulai.ui.viewmodel.RahulAiViewModel
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
fun AssistantScreen(
    viewModel: RahulAiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val audioRms by viewModel.audioRms.collectAsState()
    val speechRecognitionAvailable by viewModel.speechManager.speechRecognitionAvailable.collectAsState()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var voiceOutputEnabled by remember { mutableStateOf(viewModel.speechManager.isVoiceOutputEnabled) }
    var showVoiceChooserDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    fun handleMicClick() {
        if (isListening) {
            viewModel.stopListening()
        } else {
            if (!speechRecognitionAvailable) {
                showVoiceChooserDialog = true
                return
            }
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                viewModel.startListening()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestionChips = listOf(
        "Living room ki light on karo",
        "Torch jalao",
        "Torch band karo",
        "AC 22 degree pe set karo",
        "Battery status batao",
        "Good Night routine chalao",
        "Front door lock karo",
        "Media volume kam karo",
        "Ek majedaar joke sunao"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(NeonCyan, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rahul AI Voice Engine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        voiceOutputEnabled = viewModel.toggleSpeechOutput()
                    },
                    modifier = Modifier.testTag("toggle_voice_output_btn")
                ) {
                    Icon(
                        imageVector = if (voiceOutputEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Toggle Voice Output",
                        tint = if (voiceOutputEnabled) NeonCyan else TextMuted
                    )
                }
                IconButton(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier.testTag("clear_history_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = TextSecondary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Conversation + Hero Voice Orb
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Hero Voice Wave Orb Header
            item {
                AiVoiceWaveOrb(
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    isProcessing = isProcessing,
                    audioRms = audioRms,
                    onClick = { handleMicClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }

            // Quick suggestion chips
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp)
                ) {
                    Text(
                        text = "Quick Voice Commands / Sujhav:",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Commands List 🎙️",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { showVoiceChooserDialog = true }
                            .padding(4.dp)
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(suggestionChips) { chip ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceVariantDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.processVoiceOrTextInput(chip) }
                                .testTag("chip_${chip.take(10)}")
                        ) {
                            Text(
                                text = chip,
                                color = ElectricBlue,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Messages feed
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(msg = msg)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isProcessing) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .background(SurfaceVariantDark, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(VioletPulse, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rahul AI samajh raha hai aur nirdesh execute kar raha hai...",
                            color = VioletPulse,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Bottom Input Bar
        Surface(
            color = SurfaceDark,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Voice Listening Mic FAB
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (isListening) Color(0xFFEF4444) else NeonCyan)
                        .clickable { handleMicClick() }
                        .testTag("main_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Microphone Voice Input",
                        tint = if (isListening) Color.White else DarkNavy,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Text field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Type or talk in Hindi / English...",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SurfaceVariantDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val txt = inputText
                            inputText = ""
                            viewModel.processVoiceOrTextInput(txt)
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) NeonCyan else TextMuted
                    )
                }
            }
        }
    }

    if (showVoiceChooserDialog) {
        VoiceCommandChooserDialog(
            onDismiss = { showVoiceChooserDialog = false },
            onCommandSelected = { cmd ->
                showVoiceChooserDialog = false
                viewModel.processVoiceOrTextInput(cmd)
            }
        )
    }
}

@Composable
fun VoiceCommandChooserDialog(
    onDismiss: () -> Unit,
    onCommandSelected: (String) -> Unit
) {
    val commandGroups = listOf(
        "💡 Smart Lights" to listOf(
            "Living room ki light on karo",
            "Living room light band karo",
            "Light ki brightness 100 percent karo",
            "Light color blue kar do",
            "Light color red karo"
        ),
        "❄️ Smart AC / Thermostat" to listOf(
            "AC 22 degree pe set karo",
            "AC on kar do",
            "AC band karo",
            "AC 24 degree karo"
        ),
        "🔦 Flashlight & Battery" to listOf(
            "Torch jalao",
            "Torch band karo",
            "Battery kitni hai?",
            "Battery status check karo"
        ),
        "🔊 Audio Controls" to listOf(
            "Media volume kam karo",
            "Volume badhao",
            "Volume mute kar do",
            "Volume 80 percent set karo"
        ),
        "🔒 Doors & Appliances" to listOf(
            "Front door lock karo",
            "Front door unlock karo",
            "Coffee maker on karo",
            "Bedroom fan on karo"
        ),
        "🌙 Smart Routines" to listOf(
            "Good Night routine chalao",
            "Good Morning routine chalao",
            "Party Mode active karo",
            "Leaving Home routine"
        ),
        "🤖 AI Baatcheet" to listOf(
            "Namaste Rahul, kaise ho?",
            "Aap kya kya control kar sakte ho?",
            "Ek mast Hindi joke sunao"
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Select Voice Command",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                commandGroups.forEach { (header, cmds) ->
                    item {
                        Text(
                            text = header,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    items(cmds) { cmd ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCommandSelected(cmd) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    Icons.Default.RecordVoiceOver,
                                    contentDescription = "Speak",
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = cmd,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = SurfaceDark
    )
}

@Composable
fun ChatMessageBubble(msg: ChatMessage) {
    val isUser = msg.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.2f))
                    .border(1.dp, NeonCyan, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Rahul AI",
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) ElectricBlue.copy(alpha = 0.25f) else SurfaceVariantDark
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) ElectricBlue.copy(alpha = 0.5f) else NeonCyan.copy(alpha = 0.25f)
            ),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!isUser && msg.actionDetail != null) {
                    Text(
                        text = "⚡ Action: ${msg.actionDetail}",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = msg.text,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
