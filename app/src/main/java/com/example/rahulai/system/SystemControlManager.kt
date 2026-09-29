package com.example.rahulai.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BatteryInfo(
    val level: Int = 100,
    val isCharging: Boolean = false,
    val temperatureC: Float = 28.0f
)

data class VolumeInfo(
    val mediaVolume: Int = 50,
    val maxMediaVolume: Int = 100,
    val ringVolume: Int = 50,
    val maxRingVolume: Int = 100
)

class SystemControlManager(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _volumeInfo = MutableStateFlow(VolumeInfo())
    val volumeInfo: StateFlow<VolumeInfo> = _volumeInfo.asStateFlow()

    private var cameraIdWithFlash: String? = null

    init {
        findCameraWithFlash()
        registerTorchCallback()
        updateBatteryInfo()
        updateVolumeInfo()
    }

    private fun findCameraWithFlash() {
        try {
            cameraManager?.let { manager ->
                for (id in manager.cameraIdList) {
                    val chars = manager.getCameraCharacteristics(id)
                    val flashAvailable = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    val facing = chars.get(CameraCharacteristics.LENS_FACING)
                    if (flashAvailable && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        cameraIdWithFlash = id
                        break
                    }
                }
                if (cameraIdWithFlash == null && manager.cameraIdList.isNotEmpty()) {
                    cameraIdWithFlash = manager.cameraIdList[0]
                }
            }
        } catch (_: Exception) {
            cameraIdWithFlash = null
        }
    }

    private fun registerTorchCallback() {
        try {
            val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
            cameraManager?.registerTorchCallback(object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                    super.onTorchModeChanged(cameraId, enabled)
                    if (cameraId == cameraIdWithFlash) {
                        _isFlashlightOn.value = enabled
                    }
                }
            }, mainHandler)
        } catch (_: Exception) {}
    }

    fun toggleFlashlight(): Boolean {
        return setFlashlight(!_isFlashlightOn.value)
    }

    fun setFlashlight(enabled: Boolean): Boolean {
        val camId = cameraIdWithFlash
        return if (camId != null && cameraManager != null) {
            try {
                cameraManager.setTorchMode(camId, enabled)
                _isFlashlightOn.value = enabled
                vibrate(40)
                true
            } catch (e: CameraAccessException) {
                _isFlashlightOn.value = enabled // optimistic fallback
                false
            } catch (e: Exception) {
                _isFlashlightOn.value = enabled
                false
            }
        } else {
            // Emulated/no-camera fallback state
            _isFlashlightOn.value = enabled
            vibrate(40)
            true
        }
    }

    fun updateBatteryInfo(): BatteryInfo {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)

            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            val tempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 280) ?: 280

            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 85
            val info = BatteryInfo(
                level = batteryPct,
                isCharging = isCharging,
                temperatureC = tempTenths / 10.0f
            )
            _batteryInfo.value = info
            info
        } catch (_: Exception) {
            val defaultInfo = BatteryInfo(level = 88, isCharging = false, temperatureC = 30.5f)
            _batteryInfo.value = defaultInfo
            defaultInfo
        }
    }

    fun updateVolumeInfo() {
        audioManager?.let { am ->
            try {
                val mediaVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxMedia = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val ringVol = am.getStreamVolume(AudioManager.STREAM_RING)
                val maxRing = am.getStreamMaxVolume(AudioManager.STREAM_RING)

                _volumeInfo.value = VolumeInfo(
                    mediaVolume = mediaVol,
                    maxMediaVolume = maxMedia,
                    ringVolume = ringVol,
                    maxRingVolume = maxRing
                )
            } catch (_: Exception) {}
        }
    }

    fun setMediaVolumePercent(percent: Int) {
        audioManager?.let { am ->
            try {
                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val target = ((percent.coerceIn(0, 100) / 100.0) * max).toInt()
                am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                updateVolumeInfo()
                vibrate(30)
            } catch (_: Exception) {}
        }
    }

    fun setRingVolumePercent(percent: Int) {
        audioManager?.let { am ->
            try {
                val max = am.getStreamMaxVolume(AudioManager.STREAM_RING)
                val target = ((percent.coerceIn(0, 100) / 100.0) * max).toInt()
                am.setStreamVolume(AudioManager.STREAM_RING, target, 0)
                updateVolumeInfo()
                vibrate(30)
            } catch (_: Exception) {}
        }
    }

    fun muteMedia() {
        setMediaVolumePercent(0)
    }

    fun vibrate(durationMs: Long = 50) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    fun openWifiSettings() {
        launchIntent(Settings.ACTION_WIFI_SETTINGS)
    }

    fun openBluetoothSettings() {
        launchIntent(Settings.ACTION_BLUETOOTH_SETTINGS)
    }

    fun openSoundSettings() {
        launchIntent(Settings.ACTION_SOUND_SETTINGS)
    }

    fun openDisplaySettings() {
        launchIntent(Settings.ACTION_DISPLAY_SETTINGS)
    }

    fun openBatterySettings() {
        launchIntent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
    }

    private fun launchIntent(action: String) {
        try {
            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (_: Exception) {}
        }
    }
}
