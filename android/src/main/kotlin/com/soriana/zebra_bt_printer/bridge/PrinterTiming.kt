package com.soriana.zebra_bt_printer.bridge

internal object PrinterTiming {
    const val STATUS_POLL_INTERVAL_MS = 400L
    const val MIN_IDLE_SETTLE_MS = 500L
}

internal object PrintRetryPolicy {
    const val MAX_RETRIES = 3
    const val RETRY_DELAY_MS = 1500L
}

internal object BatchSettleTiming {
    private const val MAX_DEADLINE_MS = 120_000L
    private const val BASE_DEADLINE_MS = 8_000L
    private const val PER_COPY_MS = 4_000L

    fun deadlineMs(copies: Int): Long =
        minOf(MAX_DEADLINE_MS, BASE_DEADLINE_MS + copies.toLong() * PER_COPY_MS)
}

internal object CalibrationDefaults {
    const val DEFAULT_TIMEOUT_MS = 45_000L
    const val MIN_TIMEOUT_MS = 5_000L
    const val LABEL_LENGTH_TOLERANCE_DOTS = 40
    const val LEGACY_LABEL_LENGTH_TOLERANCE_DOTS = Int.MAX_VALUE

    /** Tras leer longitud OK, espera idle corta antes de devolver éxito. */
    const val POST_LENGTH_MATCH_IDLE_CAP_MS = 10_000L
}

internal object PrintLimits {
    const val MIN_COPIES = 1
    const val MAX_COPIES = 999
}

internal object LabelDefaults {
    const val DEFAULT_LABEL_WIDTH_DOTS = 600
    const val DEFAULT_LABEL_HEIGHT_DOTS = 240
    const val MAX_LABEL_LENGTH_MULTIPLIER = 2
    const val DEFAULT_LABEL_TOP_OFFSET = 0
}

internal object PluginPermissions {
    const val REQUEST_CODE = 2001
}
