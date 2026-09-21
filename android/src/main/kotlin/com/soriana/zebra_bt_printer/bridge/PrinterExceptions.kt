package com.soriana.zebra_bt_printer.bridge

internal class PaperOutException(message: String) : Exception(message)

internal class PrintTimeoutException(message: String) : Exception(message)

internal class CalibrateTimeoutException(message: String) : Exception(message)
