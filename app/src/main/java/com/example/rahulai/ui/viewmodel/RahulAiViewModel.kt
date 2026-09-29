package com.example.rahulai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rahulai.data.ai.ActionType
import com.example.rahulai.data.ai.ChatMessage
import com.example.rahulai.data.ai.GeminiApiClient
import com.example.rahulai.data.ai.MessageSender
import com.example.rahulai.data.ai.VoiceCommandParser
import com.example.rahulai.data.local.AppDatabase
import com.example.rahulai.data.local.CommandLogEntity
import com.example.rahulai.data.local.DeviceEntity
import com.example.rahulai.data.local.RoutineEntity
import com.example.rahulai.data.repository.SmartHomeRepository
import com.example.rahulai.system.BatteryInfo
import com.example.rahulai.system.SpeechManager
import com.example.rahulai.system.SystemControlManager
import com.example.rahulai.system.VolumeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RahulAiViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val repository = SmartHomeRepository(db.appDao())
    val systemManager = SystemControlManager(application)
    private val geminiClient = GeminiApiClient()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    val speechManager = SpeechManager(
        context = application,
        onSpeechRecognized = { recognizedText ->
            processVoiceOrTextInput(recognizedText)
        },
        onErrorOccurred = { errorMsg ->
            viewModelScope.launch {
                val sysMsg = ChatMessage(
                    sender = MessageSender.RAHUL_AI,
                    text = "ℹ️ $errorMsg"
                )
                _messages.value = _messages.value + sysMsg
            }
        },
        onRmsChanged = { rms ->
            _audioRms.value = rms
        }
    )

    val devices: StateFlow<List<DeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<CommandLogEntity>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routines: StateFlow<List<RoutineEntity>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isFlashlightOn: StateFlow<Boolean> = systemManager.isFlashlightOn
    val batteryInfo: StateFlow<BatteryInfo> = systemManager.batteryInfo
    val volumeInfo: StateFlow<VolumeInfo> = systemManager.volumeInfo

    val isListening: StateFlow<Boolean> = speechManager.isListening
    val isSpeaking: StateFlow<Boolean> = speechManager.isSpeaking

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.RAHUL_AI,
                text = "Namaste! Main Rahul AI hoon — aapka smart assistant. Main voice commands se phone controls aur smart home devices manage kar sakta hoon. Boliye, main kya karun?"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _selectedRoomFilter = MutableStateFlow("All")
    val selectedRoomFilter: StateFlow<String> = _selectedRoomFilter.asStateFlow()

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun setRoomFilter(room: String) {
        _selectedRoomFilter.value = room
    }

    fun toggleSpeechOutput(): Boolean {
        speechManager.isVoiceOutputEnabled = !speechManager.isVoiceOutputEnabled
        return speechManager.isVoiceOutputEnabled
    }

    fun startListening() {
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    fun processVoiceOrTextInput(inputText: String) {
        val query = inputText.trim()
        if (query.isBlank()) return

        // 1. Post user message
        val userMsg = ChatMessage(sender = MessageSender.USER, text = query)
        _messages.value = _messages.value + userMsg

        _isProcessing.value = true

        viewModelScope.launch {
            try {
                val currentDeviceList = devices.value.ifEmpty { repository.getDeviceList() }
                val parsed = VoiceCommandParser.parse(query, currentDeviceList)

                when (parsed.actionType) {
                    ActionType.FLASHLIGHT_ON -> {
                        systemManager.setFlashlight(true)
                        respondAndLog(query, "Flashlight on kar di gayi hai.", "SYSTEM_CONTROL", "Flashlight")
                    }
                    ActionType.FLASHLIGHT_OFF -> {
                        systemManager.setFlashlight(false)
                        respondAndLog(query, "Flashlight band kar di gayi hai.", "SYSTEM_CONTROL", "Flashlight")
                    }
                    ActionType.QUERY_BATTERY -> {
                        val batt = systemManager.updateBatteryInfo()
                        val chargingStatus = if (batt.isCharging) "charging ho rahi hai" else "charging nahi ho rahi"
                        val response = "Aapke device ki battery ${batt.level}% hai aur $chargingStatus. Taapmaan ${batt.temperatureC}°C hai."
                        respondAndLog(query, response, "SYSTEM_CONTROL", "Battery")
                    }
                    ActionType.VOLUME_UP -> {
                        val currentPct = if (volumeInfo.value.maxMediaVolume > 0)
                            (volumeInfo.value.mediaVolume * 100 / volumeInfo.value.maxMediaVolume) else 50
                        val newPct = (currentPct + 15).coerceIn(0, 100)
                        systemManager.setMediaVolumePercent(newPct)
                        respondAndLog(query, "Volume badha kar $newPct% kar diya hai.", "SYSTEM_CONTROL", "Volume")
                    }
                    ActionType.VOLUME_DOWN -> {
                        val currentPct = if (volumeInfo.value.maxMediaVolume > 0)
                            (volumeInfo.value.mediaVolume * 100 / volumeInfo.value.maxMediaVolume) else 50
                        val newPct = (currentPct - 15).coerceIn(0, 100)
                        systemManager.setMediaVolumePercent(newPct)
                        respondAndLog(query, "Volume ghata kar $newPct% kar diya hai.", "SYSTEM_CONTROL", "Volume")
                    }
                    ActionType.VOLUME_MUTE -> {
                        systemManager.muteMedia()
                        respondAndLog(query, "Media volume mute kar diya gaya hai.", "SYSTEM_CONTROL", "Volume")
                    }
                    ActionType.SET_VOLUME -> {
                        val pct = parsed.numericValue ?: 50
                        systemManager.setMediaVolumePercent(pct)
                        respondAndLog(query, "Media volume $pct% set kar diya hai.", "SYSTEM_CONTROL", "Volume")
                    }
                    ActionType.ALL_DEVICES_OFF -> {
                        repository.setAllPower(false)
                        respondAndLog(query, "Ghar ke sabhi devices power off kar diye gaye hain.", "SMART_HOME", "All Devices")
                    }
                    ActionType.ALL_DEVICES_ON -> {
                        repository.setAllPower(true)
                        respondAndLog(query, "Ghar ke sabhi devices on kar diye gaye hain.", "SMART_HOME", "All Devices")
                    }
                    ActionType.DEVICE_POWER -> {
                        val devId = parsed.targetDeviceId ?: "light_living"
                        val state = parsed.boolValue ?: true
                        repository.setPower(devId, state)
                        val devName = currentDeviceList.find { it.id == devId }?.name ?: "Device"
                        val reply = parsed.immediateSpeechResponse ?: "$devName ${if (state) "on" else "off"} ho gaya."
                        respondAndLog(query, reply, "SMART_HOME", devName)
                    }
                    ActionType.DEVICE_BRIGHTNESS -> {
                        val devId = parsed.targetDeviceId ?: "light_living"
                        val b = parsed.numericValue ?: 80
                        repository.setBrightness(devId, b)
                        val devName = currentDeviceList.find { it.id == devId }?.name ?: "Light"
                        val reply = "$devName ki brightness $b% set kar di gayi hai."
                        respondAndLog(query, reply, "SMART_HOME", devName)
                    }
                    ActionType.DEVICE_TEMP -> {
                        val devId = parsed.targetDeviceId ?: "ac_living"
                        val temp = parsed.numericValue ?: 22
                        repository.setTemperature(devId, temp)
                        repository.setPower(devId, true)
                        val devName = currentDeviceList.find { it.id == devId }?.name ?: "AC"
                        val reply = "$devName ka taapmaan $temp°C par set kar diya hai."
                        respondAndLog(query, reply, "SMART_HOME", devName)
                    }
                    ActionType.DEVICE_COLOR -> {
                        val devId = parsed.targetDeviceId ?: "light_living"
                        val hex = parsed.stringValue ?: "#00F5D4"
                        repository.setColor(devId, hex)
                        repository.setPower(devId, true)
                        val devName = currentDeviceList.find { it.id == devId }?.name ?: "Light"
                        val reply = "$devName ka ambient color change kar diya gaya hai."
                        respondAndLog(query, reply, "SMART_HOME", devName)
                    }
                    ActionType.ROUTINE -> {
                        val rId = parsed.targetRoutineId ?: "routine_night"
                        val resultText = repository.executeRoutine(rId)
                        respondAndLog(query, resultText, "ROUTINE", rId)
                    }
                    ActionType.CONVERSATION -> {
                        // Pass to Gemini advanced AI engine
                        val aiResponse = geminiClient.generateAiResponse(query, _messages.value)
                        respondAndLog(query, aiResponse, "CONVERSATION", "Gemini AI Engine")
                    }
                }
            } catch (e: Exception) {
                val errorReply = "Aadesh process karne me thodi rukawat aayi: ${e.localizedMessage ?: "Unknown error"}"
                respondAndLog(query, errorReply, "CONVERSATION", null, false)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private suspend fun respondAndLog(
        query: String,
        response: String,
        actionType: String,
        target: String?,
        isSuccess: Boolean = true
    ) {
        val aiMsg = ChatMessage(
            sender = MessageSender.RAHUL_AI,
            text = response,
            actionDetail = target
        )
        _messages.value = _messages.value + aiMsg
        speechManager.speak(response)
        repository.logCommand(query, response, actionType, target, isSuccess)
    }

    // Manual Device controls
    fun toggleDevicePower(id: String, currentState: Boolean) {
        viewModelScope.launch {
            repository.togglePower(id, currentState)
            systemManager.vibrate(25)
        }
    }

    fun setDeviceBrightness(id: String, brightness: Int) {
        viewModelScope.launch {
            repository.setBrightness(id, brightness)
        }
    }

    fun setDeviceTemperature(id: String, temp: Int) {
        viewModelScope.launch {
            repository.setTemperature(id, temp)
        }
    }

    fun setDeviceColor(id: String, hex: String) {
        viewModelScope.launch {
            repository.setColor(id, hex)
        }
    }

    fun setAllDevices(isPowered: Boolean) {
        viewModelScope.launch {
            repository.setAllPower(isPowered)
            systemManager.vibrate(40)
            val reply = if (isPowered) "Sabhi devices on kar diye gaye hain." else "Sabhi devices band kar diye gaye hain."
            speechManager.speak(reply)
        }
    }

    fun executeRoutine(routineId: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            val reply = repository.executeRoutine(routineId)
            _isProcessing.value = false
            val aiMsg = ChatMessage(sender = MessageSender.RAHUL_AI, text = reply, actionDetail = "Routine")
            _messages.value = _messages.value + aiMsg
            speechManager.speak(reply)
            systemManager.vibrate(50)
        }
    }

    fun toggleFlashlight() {
        val newState = systemManager.toggleFlashlight()
        val speech = if (newState) "Flashlight on" else "Flashlight band"
        speechManager.speak(speech)
    }

    fun setMediaVolume(percent: Int) {
        systemManager.setMediaVolumePercent(percent)
    }

    fun setRingVolume(percent: Int) {
        systemManager.setRingVolumePercent(percent)
    }

    fun muteMedia() {
        systemManager.muteMedia()
        speechManager.speak("Muted")
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearLogs()
            _messages.value = listOf(
                ChatMessage(
                    sender = MessageSender.RAHUL_AI,
                    text = "Conversation history saaf kar di gayi hai. Rahul AI aapki seva ke liye tayar hai."
                )
            )
        }
    }

    fun addCustomDevice(device: DeviceEntity) {
        viewModelScope.launch {
            repository.addDevice(device)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.release()
    }
}
