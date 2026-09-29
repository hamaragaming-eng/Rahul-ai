package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.rahulai.data.ai.ActionType
import com.example.rahulai.data.ai.VoiceCommandParser
import com.example.rahulai.data.local.DeviceEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Rahul AI", appName)
    }

    @Test
    fun `test voice command parser flashlight`() {
        val devices = listOf(
            DeviceEntity(id = "light_living", name = "Living Room Light", room = "Living Room", type = "LIGHT")
        )
        val cmdOn = VoiceCommandParser.parse("torch jalao", devices)
        assertEquals(ActionType.FLASHLIGHT_ON, cmdOn.actionType)

        val cmdOff = VoiceCommandParser.parse("torch band karo", devices)
        assertEquals(ActionType.FLASHLIGHT_OFF, cmdOff.actionType)
    }

    @Test
    fun `test voice command parser smart devices and routines`() {
        val devices = listOf(
            DeviceEntity(id = "light_living", name = "Living Room Light", room = "Living Room", type = "LIGHT")
        )
        val cmdNight = VoiceCommandParser.parse("Rahul, good night routine chalao", devices)
        assertEquals(ActionType.ROUTINE, cmdNight.actionType)
        assertEquals("routine_night", cmdNight.targetRoutineId)

        val cmdBattery = VoiceCommandParser.parse("battery kitni hai?", devices)
        assertEquals(ActionType.QUERY_BATTERY, cmdBattery.actionType)
    }
}
