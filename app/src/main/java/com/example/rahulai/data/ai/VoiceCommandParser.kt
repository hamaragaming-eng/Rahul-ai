package com.example.rahulai.data.ai

import com.example.rahulai.data.local.DeviceEntity

enum class ActionType {
    FLASHLIGHT_ON,
    FLASHLIGHT_OFF,
    QUERY_BATTERY,
    VOLUME_UP,
    VOLUME_DOWN,
    VOLUME_MUTE,
    SET_VOLUME,
    DEVICE_POWER,
    DEVICE_BRIGHTNESS,
    DEVICE_TEMP,
    DEVICE_COLOR,
    ALL_DEVICES_OFF,
    ALL_DEVICES_ON,
    ROUTINE,
    CONVERSATION
}

data class ParsedCommand(
    val actionType: ActionType,
    val targetDeviceId: String? = null,
    val targetRoutineId: String? = null,
    val numericValue: Int? = null,
    val stringValue: String? = null,
    val boolValue: Boolean? = null,
    val immediateSpeechResponse: String? = null
)

object VoiceCommandParser {

    fun parse(input: String, devices: List<DeviceEntity>): ParsedCommand {
        val text = input.lowercase().trim()

        // 1. Flashlight / Torch
        if (text.contains("torch") || text.contains("flashlight") || (text.contains("flash") && text.contains("light"))) {
            return if (text.contains("off") || text.contains("band") || text.contains("bujhao")) {
                ParsedCommand(
                    actionType = ActionType.FLASHLIGHT_OFF,
                    immediateSpeechResponse = "Flashlight band kar di gayi hai."
                )
            } else {
                ParsedCommand(
                    actionType = ActionType.FLASHLIGHT_ON,
                    immediateSpeechResponse = "Flashlight chalu kar di gayi hai."
                )
            }
        }

        // 2. Battery status
        if (text.contains("battery") || text.contains("charge") || text.contains("charging")) {
            return ParsedCommand(
                actionType = ActionType.QUERY_BATTERY,
                immediateSpeechResponse = "Battery ka status check kiya ja raha hai."
            )
        }

        // 3. Audio / Volume Controls
        if (text.contains("volume") || text.contains("sound") || text.contains("awaaz") || text.contains("awaz")) {
            if (text.contains("mute") || text.contains("silent") || text.contains("chup")) {
                return ParsedCommand(
                    actionType = ActionType.VOLUME_MUTE,
                    immediateSpeechResponse = "Media volume mute kar diya gaya hai."
                )
            }
            if (text.contains("kam") || text.contains("down") || text.contains("ghatao") || text.contains("low")) {
                return ParsedCommand(
                    actionType = ActionType.VOLUME_DOWN,
                    immediateSpeechResponse = "Volume kam kar diya gaya hai."
                )
            }
            if (text.contains("badhao") || text.contains("up") || text.contains("tez") || text.contains("high")) {
                return ParsedCommand(
                    actionType = ActionType.VOLUME_UP,
                    immediateSpeechResponse = "Volume badha diya gaya hai."
                )
            }
            // Parse number for volume
            val percentMatch = Regex("(\\d+)\\s*%?").find(text)
            if (percentMatch != null) {
                val pct = percentMatch.groupValues[1].toIntOrNull()
                if (pct != null) {
                    return ParsedCommand(
                        actionType = ActionType.SET_VOLUME,
                        numericValue = pct,
                        immediateSpeechResponse = "Volume $pct percent set kar diya gaya hai."
                    )
                }
            }
        }

        // 4. Routines
        if (text.contains("good night") || text.contains("shubh ratri") || text.contains("so jao") || text.contains("sone ja raha")) {
            return ParsedCommand(
                actionType = ActionType.ROUTINE,
                targetRoutineId = "routine_night",
                immediateSpeechResponse = "Shubh Ratri! Good night routine activate kar raha hoon."
            )
        }
        if (text.contains("good morning") || text.contains("suprabhat") || text.contains("subah ho gayi")) {
            return ParsedCommand(
                actionType = ActionType.ROUTINE,
                targetRoutineId = "routine_morning",
                immediateSpeechResponse = "Suprabhat! Good morning routine execute ho raha hai."
            )
        }
        if (text.contains("party") || text.contains("jashn") || text.contains("party mode")) {
            return ParsedCommand(
                actionType = ActionType.ROUTINE,
                targetRoutineId = "routine_party",
                immediateSpeechResponse = "Party mode activated! Lighting aur audio adjust ho gaye hain."
            )
        }
        if (text.contains("leaving") || text.contains("bahar ja raha") || text.contains("ghar se nikal")) {
            return ParsedCommand(
                actionType = ActionType.ROUTINE,
                targetRoutineId = "routine_leaving",
                immediateSpeechResponse = "Leaving home mode active. Sabhi devices safe aur locked hain."
            )
        }

        // 5. Global All Devices Turn On / Off
        if ((text.contains("all") || text.contains("sab") || text.contains("saare") || text.contains("sabhi")) &&
            (text.contains("device") || text.contains("light") || text.contains("appliances"))
        ) {
            return if (text.contains("off") || text.contains("band")) {
                ParsedCommand(
                    actionType = ActionType.ALL_DEVICES_OFF,
                    immediateSpeechResponse = "Ghar ke sabhi devices band kar diye gaye hain."
                )
            } else {
                ParsedCommand(
                    actionType = ActionType.ALL_DEVICES_ON,
                    immediateSpeechResponse = "Sabhi devices on kar diye gaye hain."
                )
            }
        }

        // 6. AC temperature
        if (text.contains("ac") || text.contains("air conditioner") || text.contains("thermostat")) {
            val num = Regex("(\\d+)\\s*(degree|c|deg)?").find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()
            if (num != null && num in 16..30) {
                return ParsedCommand(
                    actionType = ActionType.DEVICE_TEMP,
                    targetDeviceId = "ac_living",
                    numericValue = num,
                    immediateSpeechResponse = "AC temperature $num°C par set kar diya gaya hai."
                )
            }
            if (text.contains("off") || text.contains("band")) {
                return ParsedCommand(
                    actionType = ActionType.DEVICE_POWER,
                    targetDeviceId = "ac_living",
                    boolValue = false,
                    immediateSpeechResponse = "Living Room AC band kar diya gaya hai."
                )
            }
            if (text.contains("on") || text.contains("chalao") || text.contains("chalu")) {
                return ParsedCommand(
                    actionType = ActionType.DEVICE_POWER,
                    targetDeviceId = "ac_living",
                    boolValue = true,
                    immediateSpeechResponse = "Living Room AC on kar diya gaya hai."
                )
            }
        }

        // 7. Door Lock / Unlock
        if (text.contains("door") || text.contains("darwaza") || text.contains("lock")) {
            if (text.contains("unlock") || text.contains("kholo") || text.contains("open")) {
                return ParsedCommand(
                    actionType = ActionType.DEVICE_POWER,
                    targetDeviceId = "lock_front",
                    boolValue = false, // Unlocked
                    immediateSpeechResponse = "Front door unlock kar diya gaya hai."
                )
            }
            if (text.contains("lock") || text.contains("band") || text.contains("close")) {
                return ParsedCommand(
                    actionType = ActionType.DEVICE_POWER,
                    targetDeviceId = "lock_front",
                    boolValue = true, // Locked
                    immediateSpeechResponse = "Front door securely lock kar diya gaya hai."
                )
            }
        }

        // 8. TV Controls
        if (text.contains("tv") || text.contains("television")) {
            val isOff = text.contains("off") || text.contains("band")
            return ParsedCommand(
                actionType = ActionType.DEVICE_POWER,
                targetDeviceId = "tv_living",
                boolValue = !isOff,
                immediateSpeechResponse = if (isOff) "TV band kar di gayi hai." else "Living room TV on kar di gayi hai."
            )
        }

        // 9. Fan Controls
        if (text.contains("fan") || text.contains("pankha")) {
            val isOff = text.contains("off") || text.contains("band")
            return ParsedCommand(
                actionType = ActionType.DEVICE_POWER,
                targetDeviceId = "fan_bed",
                boolValue = !isOff,
                immediateSpeechResponse = if (isOff) "Bedroom fan band kar diya gaya hai." else "Bedroom fan on kar diya gaya hai."
            )
        }

        // 10. Coffee Maker / Plug
        if (text.contains("coffee") || text.contains("plug") || text.contains("kettle")) {
            val isOff = text.contains("off") || text.contains("band")
            return ParsedCommand(
                actionType = ActionType.DEVICE_POWER,
                targetDeviceId = "plug_kitchen",
                boolValue = !isOff,
                immediateSpeechResponse = if (isOff) "Coffee maker band kar diya gaya hai." else "Kitchen coffee maker start kar diya gaya hai."
            )
        }

        // 11. Generic / Specific Light control matching existing devices
        for (device in devices) {
            val devName = device.name.lowercase()
            val roomName = device.room.lowercase()
            val matchesDevice = text.contains(device.id.lowercase()) ||
                    (text.contains(roomName) && (text.contains("light") || text.contains("roshni") || text.contains("bulb"))) ||
                    text.contains(devName)

            if (matchesDevice) {
                // Check color
                if (text.contains("red") || text.contains("laal")) {
                    return ParsedCommand(
                        actionType = ActionType.DEVICE_COLOR,
                        targetDeviceId = device.id,
                        stringValue = "#F43F5E",
                        immediateSpeechResponse = "${device.name} ka color red kar diya gaya hai."
                    )
                }
                if (text.contains("blue") || text.contains("neela")) {
                    return ParsedCommand(
                        actionType = ActionType.DEVICE_COLOR,
                        targetDeviceId = device.id,
                        stringValue = "#38BDF8",
                        immediateSpeechResponse = "${device.name} ka color blue kar diya gaya hai."
                    )
                }
                if (text.contains("green") || text.contains("hara")) {
                    return ParsedCommand(
                        actionType = ActionType.DEVICE_COLOR,
                        targetDeviceId = device.id,
                        stringValue = "#34D399",
                        immediateSpeechResponse = "${device.name} ka color green kar diya gaya hai."
                    )
                }
                if (text.contains("cyan") || text.contains("warm")) {
                    return ParsedCommand(
                        actionType = ActionType.DEVICE_COLOR,
                        targetDeviceId = device.id,
                        stringValue = "#00F5D4",
                        immediateSpeechResponse = "${device.name} ka color bright cyan kar diya gaya hai."
                    )
                }

                // Check brightness
                val num = Regex("(\\d+)\\s*%?").find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()
                if (num != null && (text.contains("brightness") || text.contains("roshni") || text.contains("percent"))) {
                    return ParsedCommand(
                        actionType = ActionType.DEVICE_BRIGHTNESS,
                        targetDeviceId = device.id,
                        numericValue = num,
                        immediateSpeechResponse = "${device.name} ki brightness $num percent set kar di hai."
                    )
                }

                val isOff = text.contains("off") || text.contains("band") || text.contains("bujha")
                return ParsedCommand(
                    actionType = ActionType.DEVICE_POWER,
                    targetDeviceId = device.id,
                    boolValue = !isOff,
                    immediateSpeechResponse = "${device.name} ${if (isOff) "band" else "on"} kar di gayi hai."
                )
            }
        }

        // Generic "light on" / "light off" default to living room
        if (text.contains("light") || text.contains("batti") || text.contains("roshni")) {
            val isOff = text.contains("off") || text.contains("band") || text.contains("bujhao")
            return ParsedCommand(
                actionType = ActionType.DEVICE_POWER,
                targetDeviceId = "light_living",
                boolValue = !isOff,
                immediateSpeechResponse = "Living Room light ${if (isOff) "band" else "on"} kar di gayi hai."
            )
        }

        return ParsedCommand(actionType = ActionType.CONVERSATION)
    }
}
