package com.example.rahulai.data.repository

import com.example.rahulai.data.local.AppDao
import com.example.rahulai.data.local.CommandLogEntity
import com.example.rahulai.data.local.DeviceEntity
import com.example.rahulai.data.local.RoutineEntity
import kotlinx.coroutines.flow.Flow

class SmartHomeRepository(private val dao: AppDao) {
    val allDevices: Flow<List<DeviceEntity>> = dao.getAllDevices()
    val recentLogs: Flow<List<CommandLogEntity>> = dao.getRecentLogs()
    val allRoutines: Flow<List<RoutineEntity>> = dao.getAllRoutines()

    suspend fun getDeviceList(): List<DeviceEntity> = dao.getDeviceListSnapshot()

    suspend fun togglePower(id: String, currentState: Boolean) {
        dao.setPower(id, !currentState)
    }

    suspend fun setPower(id: String, isPowered: Boolean) {
        dao.setPower(id, isPowered)
    }

    suspend fun setBrightness(id: String, brightness: Int) {
        dao.setBrightness(id, brightness.coerceIn(0, 100))
    }

    suspend fun setTemperature(id: String, temp: Int) {
        dao.setTemperature(id, temp.coerceIn(16, 30))
    }

    suspend fun setColor(id: String, colorHex: String) {
        dao.setColor(id, colorHex)
    }

    suspend fun setMode(id: String, mode: String) {
        dao.setMode(id, mode)
    }

    suspend fun setAllPower(isPowered: Boolean) {
        dao.setAllDevicesPower(isPowered)
    }

    suspend fun addDevice(device: DeviceEntity) {
        dao.insertOrUpdateDevice(device)
    }

    suspend fun deleteDevice(id: String) {
        dao.deleteDevice(id)
    }

    suspend fun logCommand(
        query: String,
        response: String,
        actionType: String,
        target: String? = null,
        isSuccess: Boolean = true
    ) {
        dao.insertLog(
            CommandLogEntity(
                query = query,
                response = response,
                actionType = actionType,
                targetDeviceOrFeature = target,
                isSuccess = isSuccess
            )
        )
    }

    suspend fun clearLogs() {
        dao.clearLogs()
    }

    suspend fun executeRoutine(routineId: String): String {
        val routine = dao.getRoutineById(routineId) ?: return "Routine nahi mili."
        when (routine.actionJson) {
            "NIGHT_ALL_OFF" -> {
                dao.setPower("light_living", false)
                dao.setPower("light_bed", false)
                dao.setPower("tv_living", false)
                dao.setPower("lock_front", true) // lock
                dao.setTemperature("ac_living", 22)
                dao.setPower("ac_living", true)
                logCommand("Routine: ${routine.title}", "Good night routine executed: lights turned off, door secured, AC at 22°C", "ROUTINE", routine.title)
                return "Good Night routine active ho gaya. Sabhi lights off, door lock aur AC 22°C pe set kar diya hai."
            }
            "MORNING_START" -> {
                dao.setPower("light_bed", true)
                dao.setBrightness("light_bed", 70)
                dao.setPower("plug_kitchen", true)
                dao.setPower("lock_front", false)
                logCommand("Routine: ${routine.title}", "Good morning routine executed: bedroom lights on, coffee maker started", "ROUTINE", routine.title)
                return "Good morning! Bedroom light on ho gayi hai, coffee maker chalu kar diya hai."
            }
            "PARTY_ON" -> {
                dao.setPower("light_living", true)
                dao.setColor("light_living", "#F43F5E")
                dao.setBrightness("light_living", 100)
                dao.setPower("tv_living", true)
                dao.setTemperature("ac_living", 20)
                logCommand("Routine: ${routine.title}", "Party mode active: vibrant lighting, AC cooled, TV turned on", "ROUTINE", routine.title)
                return "Party mode on! Vibrant lights, AC 20°C cooling aur TV start ho chuki hai."
            }
            "LEAVING_SECURE" -> {
                dao.setAllDevicesPower(false)
                dao.setPower("lock_front", true)
                dao.setPower("cam_yard", true)
                logCommand("Routine: ${routine.title}", "Leaving home: All appliances off, front door locked, security cameras active", "ROUTINE", routine.title)
                return "Leaving home mode active! Sabhi appliances off, front door securely locked aur camera active hai."
            }
            else -> {
                logCommand("Routine: ${routine.title}", "Executed routine", "ROUTINE", routine.title)
                return "${routine.title} safalta-purvak execute ho gaya."
            }
        }
    }
}
