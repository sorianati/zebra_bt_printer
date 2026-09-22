package com.soriana.zebra_bt_printer

import com.soriana.zebra_bt_printer.bridge.CalibrateTimeoutException
import com.soriana.zebra_bt_printer.bridge.CalibrationDefaults
import com.soriana.zebra_bt_printer.bridge.NativeErrorCodes
import com.soriana.zebra_bt_printer.bridge.PaperOutException
import com.soriana.zebra_bt_printer.bridge.PluginArguments
import com.soriana.zebra_bt_printer.bridge.PluginDiagnosticMessages
import com.soriana.zebra_bt_printer.bridge.PluginResultKeys
import com.soriana.zebra_bt_printer.bridge.PrinterStatusWaiter
import com.soriana.zebra_bt_printer.bridge.PrinterTiming
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
    val skipped: Boolean = false,
    val settingsApplied: Boolean = false,
    val sensorCalibrated: Boolean = false,
) {
    fun toResultMap(): Map<String, Any?> = mapOf(
        PluginResultKeys.IS_SUCCESS to isSuccess,
        PluginResultKeys.ERROR_CODE to errorCode,
        PluginResultKeys.ERROR_MESSAGE to errorMessage,
        PluginResultKeys.DETECTED_LABEL_LENGTH_DOTS to detectedLabelLengthDots,
        PluginResultKeys.APPLIED_PRINT_WIDTH_DOTS to appliedPrintWidthDots,
        PluginResultKeys.ELAPSED_MS to elapsedMs,
        PluginResultKeys.SKIPPED to skipped,
        PluginResultKeys.SETTINGS_APPLIED to settingsApplied,
        PluginResultKeys.SENSOR_CALIBRATED to sensorCalibrated,
    )
}

internal data class EnsureMediaReadyOptionsParsed(
    val profile: MediaCalibrationProfileParsed,
    val forceSensorCalibration: Boolean,
    val saveSettingsToNvm: Boolean,
    val timeoutMs: Long,
    val labelLengthToleranceDots: Int,
)

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

    fun parseEnsureOptions(call: MethodCall): EnsureMediaReadyOptionsParsed {
        val base = parseOptions(call)
        val profile = base.profile
            ?: throw IllegalArgumentException(PluginDiagnosticMessages.PROFILE_REQUIRED)
        return EnsureMediaReadyOptionsParsed(
            profile = profile,
            forceSensorCalibration =
                call.argument<Boolean>(PluginArguments.FORCE_SENSOR_CALIBRATION) ?: false,
            saveSettingsToNvm = base.saveSettingsToNvm,
            timeoutMs = base.timeoutMs,
            labelLengthToleranceDots = base.labelLengthToleranceDots,
        )
    }

    fun ensureMediaReady(
        conn: Connection,
        options: EnsureMediaReadyOptionsParsed,
    ): MediaCalibrationOutcome {
        val startedAt = System.currentTimeMillis()
        val profile = options.profile

        try {
            ensurePaperLoaded(conn)
            val snapshot = readSnapshot(conn)

            if (
                !options.forceSensorCalibration &&
                profileMatchesSnapshot(
                    snapshot,
                    profile,
                    options.labelLengthToleranceDots,
                )
            ) {
                return MediaCalibrationOutcome(
                    isSuccess = true,
                    skipped = true,
                    detectedLabelLengthDots = snapshot.labelLengthDots,
                    appliedPrintWidthDots = snapshot.printWidthDots,
                    elapsedMs = System.currentTimeMillis() - startedAt,
                )
            }

            val needsSgd = !persistentSettingsMatch(snapshot, profile)
            val lengthOk = labelLengthWithinTolerance(
                snapshot.labelLengthDots,
                profile.labelLengthDots,
                options.labelLengthToleranceDots,
            )
            val runSensor = options.forceSensorCalibration || !lengthOk

            val calOptions = MediaCalibrationOptionsParsed(
                profile = profile,
                applyPersistentSettings = needsSgd || runSensor,
                runSensorCalibration = runSensor,
                saveSettingsToNvm = options.saveSettingsToNvm && (needsSgd || runSensor),
                timeoutMs = options.timeoutMs,
                labelLengthToleranceDots = options.labelLengthToleranceDots,
            )
            val outcome = execute(conn, calOptions)
            if (!outcome.isSuccess) {
                return outcome
            }
            return outcome.copy(
                skipped = false,
                settingsApplied = needsSgd || calOptions.applyPersistentSettings,
                sensorCalibrated = runSensor,
            )
        } catch (e: Exception) {
            return failure(
                NativeErrorCodes.CALIBRATE_ERROR,
                e.message ?: e.toString(),
                startedAt,
            )
        }
    }

    internal fun profileMatchesSnapshot(
        snapshot: MediaSnapshotParsed,
        profile: MediaCalibrationProfileParsed,
        toleranceDots: Int,
    ): Boolean {
        return persistentSettingsMatch(snapshot, profile) &&
            labelLengthWithinTolerance(
                snapshot.labelLengthDots,
                profile.labelLengthDots,
                toleranceDots,
            )
    }

    internal fun persistentSettingsMatch(
        snapshot: MediaSnapshotParsed,
        profile: MediaCalibrationProfileParsed,
    ): Boolean {
        if (snapshot.printWidthDots != profile.printWidthDots) {
            return false
        }
        if (!sgdValueEquals(snapshot.mediaType, profile.mediaType)) {
            return false
        }
        return sgdValueEquals(snapshot.mediaSenseMode, profile.mediaSense)
    }

    internal fun labelLengthWithinTolerance(
        detected: Int?,
        expected: Int,
        toleranceDots: Int,
    ): Boolean {
        if (detected == null) return false
        return kotlin.math.abs(detected - expected) <= toleranceDots
    }

    private fun sgdValueEquals(actual: String?, expected: String): Boolean {
        if (actual == null) return false
        return actual.trim().equals(expected.trim(), ignoreCase = true)
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

            val appliedWidth = when {
                profile != null && options.applyPersistentSettings -> profile.printWidthDots
                else -> readSnapshot(conn).printWidthDots
            }

            val detectedLength = when {
                profile != null && options.runSensorCalibration -> {
                    pollUntilLabelLengthMatches(
                        conn = conn,
                        profile = profile,
                        toleranceDots = options.labelLengthToleranceDots,
                        deadlineMs = options.timeoutMs,
                    )
                }
                else -> {
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
                    readSnapshot(conn).labelLengthDots
                }
            }

            return MediaCalibrationOutcome(
                isSuccess = true,
                detectedLabelLengthDots = detectedLength,
                appliedPrintWidthDots = appliedWidth,
                elapsedMs = System.currentTimeMillis() - startedAt,
            )
        } catch (e: LabelLengthMismatchException) {
            return failure(
                NativeErrorCodes.LABEL_LENGTH_MISMATCH,
                e.message,
                startedAt,
                e.detectedDots,
                options.profile?.printWidthDots,
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

    /**
     * Tras calibrar sensor, muchas ZQ no marcan "ocupada" en status; en su lugar se
     * hace poll de [ZebraSgdKeys.ZPL_LABEL_LENGTH] hasta coincidir con el perfil.
     */
    private fun pollUntilLabelLengthMatches(
        conn: Connection,
        profile: MediaCalibrationProfileParsed,
        toleranceDots: Int,
        deadlineMs: Long,
    ): Int {
        val deadlineAt = System.currentTimeMillis() + deadlineMs
        var lastDetected: Int? = null

        while (System.currentTimeMillis() < deadlineAt) {
            ensurePaperLoaded(conn)
            lastDetected = sgdGetInt(conn, ZebraSgdKeys.ZPL_LABEL_LENGTH)
            if (lastDetected != null) {
                val delta = kotlin.math.abs(lastDetected - profile.labelLengthDots)
                if (delta <= toleranceDots) {
                    val remaining = deadlineAt - System.currentTimeMillis()
                    if (remaining > 0) {
                        try {
                            PrinterStatusWaiter.awaitIdle(
                                conn = conn,
                                deadlineMs = minOf(
                                    remaining,
                                    CalibrationDefaults.POST_LENGTH_MATCH_IDLE_CAP_MS,
                                ),
                                paperOutMessage =
                                    PluginDiagnosticMessages.PAPER_OUT_DURING_CALIBRATION,
                                timeoutException = { ms, _ ->
                                    CalibrateTimeoutException(
                                        PluginDiagnosticMessages.calibrationIdleTimeout(ms),
                                    )
                                },
                            )
                        } catch (_: CalibrateTimeoutException) {
                            // Longitud ya válida; algunas unidades siguen "busy" en status.
                        }
                    }
                    return lastDetected
                }
            }
            Thread.sleep(PrinterTiming.STATUS_POLL_INTERVAL_MS)
        }

        if (lastDetected == null) {
            throw CalibrateTimeoutException(
                PluginDiagnosticMessages.LABEL_LENGTH_READ_FAILED,
            )
        }
        throw LabelLengthMismatchException(
            PluginDiagnosticMessages.labelLengthMismatch(
                lastDetected,
                profile.labelLengthDots,
                toleranceDots,
            ),
            lastDetected,
        )
    }

    private class LabelLengthMismatchException(
        message: String,
        val detectedDots: Int,
    ) : Exception(message)

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
