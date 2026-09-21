package com.soriana.zebra_bt_printer.zebra

internal object ZebraSgdKeys {
    const val ZPL_LABEL_LENGTH = "zpl.label_length"
    const val EZPL_PRINT_WIDTH = "ezpl.print_width"
    const val MEDIA_TYPE = "media.type"
    const val MEDIA_SENSE_MODE = "media.sense_mode"
    const val EZPL_LABEL_LENGTH_MAX = "ezpl.label_length_max"
}

internal object ZebraZplCommands {
    const val SAVE_TO_NVM = "^XA^JUS^XZ"
    const val CALIBRATE_AND_SAVE = "~JC^XA^JUS^XZ"
}

internal object MediaSenseValues {
    const val GAP = "gap"
    const val BAR = "bar"
}

internal object MediaTypeValues {
    const val LABEL = "label"
    const val JOURNAL = "journal"
}

internal object LabelMediaWireValues {
    const val GAP = "gap"
    const val MARK = "mark"
    const val NONE = "none"
}

internal object ZplMediaCommands {
    const val GAP = "^MNA"
    const val MARK = "^MNB"
    const val NONE = "^MNN"
}

internal object PrinterTypeValues {
    const val ZEBRA = "zebra"
}
