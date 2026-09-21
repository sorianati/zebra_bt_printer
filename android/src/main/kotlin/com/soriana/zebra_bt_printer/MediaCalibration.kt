package com.soriana.zebra_bt_printer

import com.soriana.zebra_bt_printer.bridge.CalibrateTimeoutException
import com.soriana.zebra_bt_printer.bridge.CalibrationDefaults
import com.soriana.zebra_bt_printer.bridge.NativeErrorCodes
import com.soriana.zebra_bt_printer.bridge.PaperOutException
import com.soriana.zebra_bt_printer.bridge.PluginArguments
import com.soriana.zebra_bt_printer.bridge.PluginDiagnosticMessages
import com.soriana.zebra_bt_printer.bridge.PluginResultKeys
import com.soriana.zebra_bt_printer.bridge.PrinterStatusWaiter
import com.soriana.zebra_bt_printer.bridge.LabelDefaults
import com.soriana.zebra_bt_printer.zebra.MediaSenseValues
import com.soriana.zebra_bt_printer.zebra.MediaTypeValues
import com.soriana.zebra_bt_printer.zebra.ZebraSgdKeys
import com.soriana.zebra_bt_printer.zebra.ZebraZplCommands
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.printer.SGD
import com.zebra.sdk.printer.ZebraPrinterFactory
import io.flutter.plugin.common.MethodCall

internal data class MediaCalibrationProfileParsed(
    val printWidthDots: Int,
    val labelLengthDots: Int,
    val mediaSense: String,
    val mediaType: String,
    val maxLabelLengthDots: Int?,
)

internal data class MediaCalibrationOptionsParsed(
    val profile: MediaCalibrationProfileParsed?,
    val applyPersistentSettings: Boolean,
    val runSensorCalibration: Boolean,
    val saveSettingsToNvm: Boolean,
    val timeoutMs: Long,
    val labelLengthToleranceDots: Int,
)

internal data class MediaCalibrationOutcome(
    val isSuccess: Boolean,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val detectedLabelLengthDots: Int? = null,
    val appliedPrintWidthDots: Int? = null,
    val elapsedMs: Long = 0,
) {
    fun toResultMap(): Map<String, Any?> = mapOf(
        PluginResultKeys.IS_SUCCESS to isSuccess,
        PluginResultKeys.ERROR_CODE to errorCode,
        PluginResultKeys.ERROR_MESSAGE to errorMessage,
        PluginResultKeys.DETECTED_LABEL_LENGTH_DOTS to detectedLabelLengthDots,
        PluginResultKeys.APPLIED_PRINT_WIDTH_DOTS to appliedPrintWidthDots,
        PluginResultKeys.ELAPSED_MS to elapsedMs,
    )
}

internal data class MediaSnapshotParsed(
    val labelLengthDots: Int?,
    val printWidthDots: Int?,
    val mediaType: String?,
    val mediaSenseMode: String?,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        PluginResultKeys.LABEL_LENGTH_DOTS to labelLengthDots,
        PluginResultKeys.PRINT_WIDTH_DOTS to printWidthDots,
        PluginResultKeys.MEDIA_TYPE to mediaType,
        PluginResultKeys.MEDIA_SENSE_MODE to mediaSenseMode,
    )
}

internal object MediaCalibrationSupport {
    fun legacyCalibrateOptions(): MediaCalibrationOptionsParsed =
        MediaCalibrationOptionsParsed(
            profile = null,
            applyPersistentSettings = false,
            runSensorCalibration = true,
            saveSettingsToNvm = true,
            timeoutMs = CalibrationDefaults.DEFAULT_TIMEOUT_MS,
            labelLengthToleranceDots = CalibrationDefaults.LEGACY_LABEL_LENGTH_TOLERANCE_DOTS,
        )

    fun parseOptions(call: MethodCall): MediaCalibrationOptionsParsed {
        @Suppress("UNCHECKED_CAST")
        val profileMap = call.argument<Map<String, Any?>>(PluginArguments.PROFILE)
        val profile = profileMap?.let { map ->
            val printWidth = map[PluginArguments.PRINT_WIDTH_DOTS] as? Int
            val labelLength = map[PluginArguments.LABEL_LENGTH_DOTS] as? Int
            val sense = map[PluginArguments.MEDIA_SENSE] as? String ?: MediaSenseValues.GAP
            val type = map[PluginArguments.MEDIA_TYPE] as? String ?: MediaTypeValues.LABEL
            if (printWidth == null || labelLength == null) {
                null
            } else {
                MediaCalibrationProfileParsed(
                    printWidthDots = printWidth,
                    labelLengthDots = labelLength,
                    mediaSense = sense,
                    mediaType = type,
                    maxLabelLengthDots = map[PluginArguments.MAX_LABEL_LENGTH_DOTS] as? Int,
                )
            }
        }

        return MediaCalibrationOptionsParsed(
            profile = profile,
            applyPersistentSettings =
                call.argument<Boolean>(PluginArguments.APPLY_PERSISTENT_SETTINGS) ?: true,
            runSensorCalibration =
                call.argument<Boolean>(PluginArguments.RUN_SENSOR_CALIBRATION) ?: true,
            saveSettingsToNvm =
                call.argument<Boolean>(PluginArguments.SAVE_SETTINGS_TO_NVM) ?: true,
            timeoutMs = (call.argument<Int>(PluginArguments.TIMEOUT_MS)
                ?: CalibrationDefaults.DEFAULT_TIMEOUT_MS.toInt())
                .toLong()
                .coerceAtLeast(CalibrationDefaults.MIN_TIMEOUT_MS),
            labelLengthToleranceDots = call.argument<Int>(
                PluginArguments.LABEL_LENGTH_TOLERANCE_DOTS,
            ) ?: CalibrationDefaults.LABEL_LENGTH_TOLERANCE_DOTS,
        )
    }

    fun readSnapshot(conn: Connection): MediaSnapshotParsed {
        return MediaSnapshotParsed(
            labelLengthDots = sgdGetInt(conn, ZebraSgdKeys.ZPL_LABEL_LENGTH),
            printWidthDots = sgdGetInt(conn, ZebraSgdKeys.EZPL_PRINT_WIDTH),
            mediaType = sgdGetString(conn, ZebraSgdKeys.MEDIA_TYPE),
            mediaSenseMode = sgdGetString(conn, ZebraSgdKeys.MEDIA_SENSE_MODE),
        )
    }

    fun execute(
        conn: Connection,
        options: MediaCalibrationOptionsParsed,
    ): MediaCalibrationOutcome {
        val startedAt = System.currentTimeMillis()

        try {
            ensurePaperLoaded(conn)

            val profile = options.profile
            if (profile != null && options.applyPersistentSettings) {
                applyPersistentProfile(conn, profile)
            }

            if (options.runSensorCalibration) {
                runSensorCalibration(conn)
            }

            if (options.saveSettingsToNvm) {
                conn.write(ZebraZplCommands.SAVE_TO_NVM.toByteArray(Charsets.UTF_8))
            }

            PrinterStatusWaiter.awaitIdle(
                conn = conn,
                deadlineMs = options.timeoutMs,
                paperOutMessage = PluginDiagnosticMessages.PAPER_OUT_DURING_CALIBRATION,
                timeoutException = { deadlineMs, _ ->
                    CalibrateTimeoutException(
                        PluginDiagnosticMessages.calibrationIdleTimeout(deadlineMs),
                    )
                },
            )

            val snapshot = readSnapshot(conn)
            var appliedWidth = snapshot.printWidthDots
            if (profile != null && options.applyPersistentSettings) {
                appliedWidth = profile.printWidthDots
            }

            if (profile != null && options.runSensorCalibration) {
                val detected = snapshot.labelLengthDots
                if (detected == null) {
                    return failure(
                        NativeErrorCodes.CALIBRATE_ERROR,
                        PluginDiagnosticMessages.LABEL_LENGTH_READ_FAILED,
                        startedAt,
                        detected,
                        appliedWidth,
                    )
                }
                val delta = kotlin.math.abs(detected - profile.labelLengthDots)
                if (delta > options.labelLengthToleranceDots) {
                    return failure(
                        NativeErrorCodes.LABEL_LENGTH_MISMATCH,
                        PluginDiagnosticMessages.labelLengthMismatch(
                            detected,
                            profile.labelLengthDots,
                            options.labelLengthToleranceDots,
                        ),
                        startedAt,
                        detected,
                        appliedWidth,
                    )
                }
            }

            return MediaCalibrationOutcome(
                isSuccess = true,
                detectedLabelLengthDots = snapshot.labelLengthDots,
                appliedPrintWidthDots = appliedWidth,
                elapsedMs = System.currentTimeMillis() - startedAt,
            )
        } catch (e: CalibrateTimeoutException) {
            return failure(NativeErrorCodes.CALIBRATE_TIMEOUT, e.message, startedAt)
        } catch (e: PaperOutException) {
            return failure(NativeErrorCodes.CALIBRATE_ERROR, e.message, startedAt)
        } catch (e: Exception) {
            return failure(
                NativeErrorCodes.CALIBRATE_ERROR,
                e.message ?: e.toString(),
                startedAt,
            )
        }
    }

    private fun applyPersistentProfile(conn: Connection, profile: MediaCalibrationProfileParsed) {
        SGD.SET(ZebraSgdKeys.MEDIA_TYPE, profile.mediaType, conn)
        SGD.SET(ZebraSgdKeys.MEDIA_SENSE_MODE, profile.mediaSense, conn)
        SGD.SET(ZebraSgdKeys.EZPL_PRINT_WIDTH, profile.printWidthDots.toString(), conn)
        val maxLen = profile.maxLabelLengthDots
            ?: (profile.labelLengthDots * LabelDefaults.MAX_LABEL_LENGTH_MULTIPLIER)
        SGD.SET(ZebraSgdKeys.EZPL_LABEL_LENGTH_MAX, maxLen.toString(), conn)
    }

    private fun runSensorCalibration(conn: Connection) {
        val printer = ZebraPrinterFactory.getInstance(conn)
        try {
            // ZebraPrinter extends ToolsUtil in the Link-OS SDK (calibrate() on printer).
            printer.calibrate()
        } catch (_: Exception) {
            conn.write(ZebraZplCommands.CALIBRATE_AND_SAVE.toByteArray(Charsets.UTF_8))
        }
    }

    private fun ensurePaperLoaded(conn: Connection) {
        val status = ZebraPrinterFactory.getInstance(conn).currentStatus
        if (status.isPaperOut) {
            throw PaperOutException(PluginDiagnosticMessages.PAPER_OUT_PRE_CHECK)
        }
    }

    private fun sgdGetString(conn: Connection, key: String): String? {
        return try {
            val raw = SGD.GET(key, conn)?.trim()
            if (raw.isNullOrEmpty()) null else raw.removeSurrounding("\"")
        } catch (_: Exception) {
            null
        }
    }

    private fun sgdGetInt(conn: Connection, key: String): Int? {
        val raw = sgdGetString(conn, key) ?: return null
        return raw.toIntOrNull()
    }

    private fun failure(
        code: String,
        message: String?,
        startedAt: Long,
        detected: Int? = null,
        appliedWidth: Int? = null,
    ): MediaCalibrationOutcome = MediaCalibrationOutcome(
        isSuccess = false,
        errorCode = code,
        errorMessage = message,
        detectedLabelLengthDots = detected,
        appliedPrintWidthDots = appliedWidth,
        elapsedMs = System.currentTimeMillis() - startedAt,
    )
}
