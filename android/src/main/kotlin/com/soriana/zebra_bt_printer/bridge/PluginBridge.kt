package com.soriana.zebra_bt_printer.bridge

internal object PluginChannel {
    const val NAME = "zebra_bt_printer"
}

internal object PluginMethods {
    const val CONNECT_BLUETOOTH = "connectBluetooth"
    const val DISCONNECT_BLUETOOTH = "disconnectBluetooth"
    const val CALIBRATE_PRINTER = "calibratePrinter"
    const val CALIBRATE_MEDIA = "calibrateMedia"
    const val GET_MEDIA_SNAPSHOT = "getMediaSnapshot"
    const val PRINT_IMAGE_BLUETOOTH = "printImageBluetooth"
    const val PRINT_IMAGE_IP = "printImageIP"
    const val PRINT_LABEL_BLUETOOTH = "printLabelBluetooth"
    const val REQUEST_PERMISSIONS = "requestPermissions"
    const val IS_BLUETOOTH_ENABLED = "isBluetoothEnabled"
}

internal object PluginArguments {
    const val MAC = "mac"
    const val IP = "ip"
    const val IMAGE_BASE64 = "imageBase64"
    const val ZPL_TEXT = "zplText"
    const val COPIES = "copies"

    const val LABEL_WIDTH_DOTS = "labelWidthDots"
    const val LABEL_HEIGHT_DOTS = "labelHeightDots"
    const val USE_SMOOTH_SCALING = "useSmoothScaling"
    const val PRINTER_TYPE = "printerType"
    const val MEDIA_TYPE = "mediaType"
    const val ALLOW_UPSCALE = "allowUpscale"
    const val MAX_LABEL_LENGTH_DOTS = "maxLabelLengthDots"
    const val LABEL_TOP_OFFSET = "labelTopOffset"

    const val PROFILE = "profile"
    const val PRINT_WIDTH_DOTS = "printWidthDots"
    const val LABEL_LENGTH_DOTS = "labelLengthDots"
    const val MEDIA_SENSE = "mediaSense"
    const val APPLY_PERSISTENT_SETTINGS = "applyPersistentSettings"
    const val RUN_SENSOR_CALIBRATION = "runSensorCalibration"
    const val SAVE_SETTINGS_TO_NVM = "saveSettingsToNvm"
    const val TIMEOUT_MS = "timeoutMs"
    const val LABEL_LENGTH_TOLERANCE_DOTS = "labelLengthToleranceDots"
    const val CLOSE_CONNECTION_AFTER = "closeConnectionAfter"
}

internal object PluginResultKeys {
    const val IS_SUCCESS = "isSuccess"
    const val ERROR_CODE = "errorCode"
    const val ERROR_MESSAGE = "errorMessage"
    const val DETECTED_LABEL_LENGTH_DOTS = "detectedLabelLengthDots"
    const val APPLIED_PRINT_WIDTH_DOTS = "appliedPrintWidthDots"
    const val ELAPSED_MS = "elapsedMs"

    const val LABEL_LENGTH_DOTS = "labelLengthDots"
    const val PRINT_WIDTH_DOTS = "printWidthDots"
    const val MEDIA_TYPE = "mediaType"
    const val MEDIA_SENSE_MODE = "mediaSenseMode"
}

internal object NativeErrorCodes {
    const val INVALID_ARGS = "INVALID_ARGS"
    const val PERMISSION_DENIED = "PERMISSION_DENIED"
    const val CONNECT_ERROR = "CONNECT_ERROR"
    const val CALIBRATE_ERROR = "CALIBRATE_ERROR"
    const val CALIBRATE_TIMEOUT = "CALIBRATE_TIMEOUT"
    const val LABEL_LENGTH_MISMATCH = "LABEL_LENGTH_MISMATCH"
    const val PRINT_ERROR = "PRINT_ERROR"
    const val PRINT_TIMEOUT = "PRINT_TIMEOUT"
    const val PAPER_OUT = "PAPER_OUT"
    const val DISCONNECT_ERROR = "DISCONNECT_ERROR"
    const val NO_ACTIVITY = "NO_ACTIVITY"
    const val PERMISSION_REQUEST_IN_PROGRESS = "PERMISSION_REQUEST_IN_PROGRESS"
}
