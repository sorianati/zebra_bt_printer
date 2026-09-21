## 1.6.1

* Android: fix Kotlin compile error — call `ZebraPrinter.calibrate()` (SDK
  `ToolsUtil`) instead of invalid `toolsUtil` property.

## 1.6.0

* Internal: centralized MethodChannel contract (`lib/src/bridge/`), printer
  timing, Zebra SGD/ZPL keys, and diagnostic messages (no public API change).
* Added `ZebraBtPrinter.calibrateMedia` with `CalibrateMediaOptions`,
  `MediaCalibrationProfile`, and typed `CalibrateMediaResult`.
* Added `ZebraBtPrinter.getMediaSnapshot` for SGD diagnostics
  (`zpl.label_length`, `ezpl.print_width`, `media.*`).
* Android: media calibration via Link-OS `toolsUtil.calibrate()` with ZPL
  fallback (`~JC^XA^JUS^XZ`), optional SGD profile sync, idle polling, and
  `LABEL_LENGTH_MISMATCH` / `CALIBRATE_TIMEOUT` codes.
* Presets `SorianaMediaProfiles.fenicia12Up` / `fenicia24Up`.
* `calibratePrinter` remains but is deprecated; it now uses the improved
  native flow (no fixed 3 s sleep).
* Design reference: `doc/DISENO_CALIBRACION_MEDIA.md`.

## 1.5.0

* Breaking: `PrintResult.errorCode` is now a typed `PrintErrorCode` enum
  instead of a raw `String?`.
* Added stable `userMessage` for UI and kept `errorMessage` / `rawErrorCode`
  for technical logging.
* Unknown native codes map to `PrintErrorCode.unknown`.
* Added `PrintErrorCode.paperOut` and a pre-print paper check via Zebra
  `getCurrentStatus().isPaperOut` (Bluetooth image/IP/label).
* End-of-batch confirmation: after `write`×N, poll status until the printer is
  ready, reports paper out, or times out (`PrintErrorCode.printTimeout`).
  Does not confirm label-by-label or report “k of N”.
* `PAPER_OUT` and `PRINT_TIMEOUT` are not retried by the native retry loop.

## 1.0.0

* Print base64 images to Zebra printers over Bluetooth and TCP/IP (auto-scaled
  and centered).
* Print plain-text labels as basic ZPL over Bluetooth.
* Runtime Bluetooth permission handling for Android 12+ and legacy devices.
* Typed `PrintResult` return values and a `PrinterConfig` for label sizing.
* Graceful iOS stub: unsupported operations fail or return `false` instead of
  throwing.
* Integration guide for consuming teams (`doc/INTEGRATION.md`).
