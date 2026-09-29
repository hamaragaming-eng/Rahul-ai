package com.example.rahulai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DeviceEntity::class, CommandLogEntity::class, RoutineEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rahul_ai_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                scope.launch(Dispatchers.IO) {
                    ensureSeedData(instance.appDao())
                }
                instance
            }
        }

        suspend fun ensureSeedData(dao: AppDao) {
            val existing = dao.getDeviceListSnapshot()
            if (existing.isEmpty()) {
                populateInitialData(dao)
            }
        }

        suspend fun populateInitialData(dao: AppDao) {
            val initialDevices = listOf(
                DeviceEntity(
                    id = "light_living",
                    name = "Living Room Light",
                    room = "Living Room",
                    type = "LIGHT",
                    isPowered = true,
                    brightness = 85,
                    colorHex = "#00F5D4"
                ),
                DeviceEntity(
                    id = "ac_living",
                    name = "Living Room AC",
                    room = "Living Room",
                    type = "AC",
                    isPowered = true,
                    temperature = 23,
                    mode = "COOL"
                ),
                DeviceEntity(
                    id = "tv_living",
                    name = "Living Room 4K TV",
                    room = "Living Room",
                    type = "TV",
                    isPowered = false,
                    mode = "HDMI 1"
                ),
                DeviceEntity(
                    id = "lock_front",
                    name = "Front Door Smart Lock",
                    room = "Entrance",
                    type = "LOCK",
                    isPowered = true // Locked
                ),
                DeviceEntity(
                    id = "light_bed",
                    name = "Bedroom Ambient Light",
                    room = "Bedroom",
                    type = "LIGHT",
                    isPowered = false,
                    brightness = 60,
                    colorHex = "#A78BFA"
                ),
                DeviceEntity(
                    id = "fan_bed",
                    name = "Bedroom Smart Fan",
                    room = "Bedroom",
                    type = "FAN",
                    isPowered = true,
                    brightness = 3 // Speed level
                ),
                DeviceEntity(
                    id = "plug_kitchen",
                    name = "Kitchen Coffee Maker",
                    room = "Kitchen",
                    type = "PLUG",
                    isPowered = false
                ),
                DeviceEntity(
                    id = "cam_yard",
                    name = "Front Yard Security Camera",
                    room = "Outdoor",
                    type = "CAMERA",
                    isPowered = true
                )
            )
            dao.insertDevices(initialDevices)

            val initialRoutines = listOf(
                RoutineEntity(
                    id = "routine_night",
                    title = "Good Night / Shubh Ratri",
                    triggerPhrase = "good night",
                    description = "Turns off all lights, sets AC to 22°C, and locks front door",
                    iconKey = "NIGHT",
                    actionJson = "NIGHT_ALL_OFF"
                ),
                RoutineEntity(
                    id = "routine_morning",
                    title = "Good Morning / Suprabhat",
                    triggerPhrase = "good morning",
                    description = "Powers bedroom light, starts coffee maker, and unlocks door",
                    iconKey = "MORNING",
                    actionJson = "MORNING_START"
                ),
                RoutineEntity(
                    id = "routine_party",
                    title = "Party Mode",
                    triggerPhrase = "party mode",
                    description = "Pulsing neon lights, turns on 4K TV, and cools AC to 20°C",
                    iconKey = "PARTY",
                    actionJson = "PARTY_ON"
                ),
                RoutineEntity(
                    id = "routine_leaving",
                    title = "Leaving Home",
                    triggerPhrase = "leaving home",
                    description = "Turns off all devices, locks all doors, and activates cameras",
                    iconKey = "LEAVING",
                    actionJson = "LEAVING_SECURE"
                )
            )
            dao.insertRoutines(initialRoutines)

            // Add welcoming log
            dao.insertLog(
                CommandLogEntity(
                    query = "System Initialized",
                    response = "Namaste! Rahul AI engine is online and ready for voice commands.",
                    actionType = "SYSTEM_CONTROL",
                    targetDeviceOrFeature = "Engine",
                    isSuccess = true
                )
            )
        }
    }
}
