package com.soriana.zebra_bt_printer.bridge

import com.zebra.sdk.comm.Connection
import com.zebra.sdk.printer.PrinterStatus
import com.zebra.sdk.printer.ZebraPrinterFactory

internal object PrinterStatusWaiter {
    /**
     * @param requireObservedBusy Si true, no acepta "listo" hasta haber visto la
     * impresora ocupada al menos una vez (calibración / feed de media). Evita leer
     * SGD con la longitud del rollo anterior tras un falso idle inicial.
     */
    fun awaitIdle(
        conn: Connection,
        deadlineMs: Long,
        paperOutMessage: String,
        timeoutException: (deadlineMs: Long, status: PrinterStatus) -> Exception,
        requireObservedBusy: Boolean = false,
    ) {
        val startedAt = System.currentTimeMillis()
        val deadlineAt = startedAt + deadlineMs
        val printer = ZebraPrinterFactory.getInstance(conn)
        var sawBusy = false

        while (true) {
            val status = printer.currentStatus
            if (status.isPaperOut) {
                throw PaperOutException(paperOutMessage)
            }

            val bufferEmpty = status.numberOfFormatsInReceiveBuffer <= 0
            val batchEmpty = status.labelsRemainingInBatch <= 0
            val idle = status.isReadyToPrint && bufferEmpty && batchEmpty

            if (!idle) {
                sawBusy = true
            } else {
                val elapsed = System.currentTimeMillis() - startedAt
                val settledWithoutBusy =
                    !requireObservedBusy && elapsed >= PrinterTiming.MIN_IDLE_SETTLE_MS
                if (sawBusy || settledWithoutBusy) {
                    return
                }
            }

            if (System.currentTimeMillis() >= deadlineAt) {
                throw timeoutException(deadlineMs, status)
            }
            Thread.sleep(PrinterTiming.STATUS_POLL_INTERVAL_MS)
        }
    }
}
