package com.soriana.zebra_bt_printer.bridge

/** Mensajes técnicos para [MethodChannel.Result.error] / excepciones (no UI). */
internal object PluginDiagnosticMessages {
    const val MAC_REQUIRED = "mac es requerido"
    const val PROFILE_REQUIRED = "profile es requerido para ensureMediaReadyForProfile"
    const val IP_REQUIRED = "ip es requerido"
    const val IMAGE_BASE64_REQUIRED = "imageBase64 es requerido"
    const val ZPL_TEXT_REQUIRED = "zplText es requerido"
    const val BLUETOOTH_PERMISSIONS_REQUIRED =
        "Faltan permisos Bluetooth. Llama a requestPermissions() primero."
    const val BLUETOOTH_PERMISSIONS_CALIBRATE = "Faltan permisos Bluetooth."
    const val BLUETOOTH_PERMISSIONS_PRINT =
        "Faltan permisos Bluetooth (BLUETOOTH_CONNECT/BLUETOOTH_SCAN). " +
            "Llama a requestPermissions() y acéptalos antes de imprimir."
    const val ACTIVITY_UNAVAILABLE = "Activity no disponible"
    const val PERMISSION_REQUEST_IN_PROGRESS = "Solicitud de permisos en curso"

    const val PAPER_OUT_PRE_CHECK = "La impresora reporta sin papel (isPaperOut)"
    const val PAPER_OUT_AFTER_BATCH =
        "La impresora reporta sin papel tras enviar el lote (isPaperOut)"
    const val PAPER_OUT_DURING_CALIBRATION =
        "Sin papel durante la espera de calibración"

    const val LABEL_LENGTH_READ_FAILED =
        "No se pudo leer zpl.label_length tras calibrar"

    fun labelLengthMismatch(detected: Int, expected: Int, tolerance: Int): String =
        "zpl.label_length=$detected, esperado=$expected (±$tolerance)"

    fun batchPrintTimeout(
        deadlineMs: Long,
        copies: Int,
        ready: Boolean,
        buffer: Int,
        remaining: Int,
    ): String =
        "Timeout esperando fin de lote (${deadlineMs}ms, copies=$copies, " +
            "ready=$ready, buffer=$buffer, remaining=$remaining)"

    fun calibrationIdleTimeout(deadlineMs: Long): String =
        "Timeout esperando fin de calibración (${deadlineMs}ms)"
}
