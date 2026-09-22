package com.soriana.zebra_bt_printer

import com.soriana.zebra_bt_printer.bridge.CalibrationDefaults
import com.soriana.zebra_bt_printer.bridge.PluginArguments
import com.soriana.zebra_bt_printer.bridge.PluginMethods
import com.soriana.zebra_bt_printer.zebra.MediaSenseValues
import com.soriana.zebra_bt_printer.zebra.MediaTypeValues
import io.flutter.plugin.common.MethodCall
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MediaCalibrationSupportTest {
    @Test
    fun parseOptions_readsProfileAndFlags() {
        val call = MethodCall(
            PluginMethods.CALIBRATE_MEDIA,
            mapOf(
                PluginArguments.MAC to "00:11:22:33:44:55",
                PluginArguments.PROFILE to mapOf(
                    PluginArguments.PRINT_WIDTH_DOTS to 600,
                    PluginArguments.LABEL_LENGTH_DOTS to 250,
                    PluginArguments.MEDIA_SENSE to MediaSenseValues.BAR,
                    PluginArguments.MEDIA_TYPE to MediaTypeValues.LABEL,
                ),
                PluginArguments.APPLY_PERSISTENT_SETTINGS to true,
                PluginArguments.RUN_SENSOR_CALIBRATION to false,
                PluginArguments.SAVE_SETTINGS_TO_NVM to true,
                PluginArguments.TIMEOUT_MS to 30_000,
                PluginArguments.LABEL_LENGTH_TOLERANCE_DOTS to 25,
            ),
        )

        val options = MediaCalibrationSupport.parseOptions(call)

        assertNotNull(options.profile)
        assertEquals(600, options.profile?.printWidthDots)
        assertEquals(250, options.profile?.labelLengthDots)
        assertEquals(MediaSenseValues.BAR, options.profile?.mediaSense)
        assertEquals(false, options.runSensorCalibration)
        assertEquals(30_000L, options.timeoutMs)
        assertEquals(25, options.labelLengthToleranceDots)
    }

    @Test
    fun parseOptions_defaultsWhenOmitted() {
        val call = MethodCall(
            PluginMethods.CALIBRATE_MEDIA,
            mapOf(PluginArguments.MAC to "aa"),
        )
        val options = MediaCalibrationSupport.parseOptions(call)

        assertNull(options.profile)
        assertTrue(options.applyPersistentSettings)
        assertTrue(options.runSensorCalibration)
        assertEquals(CalibrationDefaults.DEFAULT_TIMEOUT_MS, options.timeoutMs)
    }

    @Test
    fun profileMatchesSnapshot_whenSgdAndLengthAlign() {
        val profile = MediaCalibrationProfileParsed(
            printWidthDots = 600,
            labelLengthDots = 250,
            mediaSense = MediaSenseValues.BAR,
            mediaType = MediaTypeValues.LABEL,
            maxLabelLengthDots = null,
        )
        val snapshot = MediaSnapshotParsed(
            labelLengthDots = 248,
            printWidthDots = 600,
            mediaType = MediaTypeValues.LABEL,
            mediaSenseMode = MediaSenseValues.BAR,
        )

        assertTrue(
            MediaCalibrationSupport.profileMatchesSnapshot(snapshot, profile, 25),
        )
    }

    @Test
    fun profileMatchesSnapshot_falseWhenWidthDiffers() {
        val profile = MediaCalibrationProfileParsed(
            printWidthDots = 575,
            labelLengthDots = 565,
            mediaSense = MediaSenseValues.BAR,
            mediaType = MediaTypeValues.LABEL,
            maxLabelLengthDots = null,
        )
        val snapshot = MediaSnapshotParsed(
            labelLengthDots = 565,
            printWidthDots = 600,
            mediaType = MediaTypeValues.LABEL,
            mediaSenseMode = MediaSenseValues.BAR,
        )

        assertEquals(
            false,
            MediaCalibrationSupport.profileMatchesSnapshot(snapshot, profile, 25),
        )
    }
}
