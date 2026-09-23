## 1.7.5

* Android: dependencia `jackson-databind` para `PrinterCalibrator` (evita crash
  `NoClassDefFoundError: ObjectMapper` en la app host).
* Fallback de calibración captura `Throwable` (p. ej. linkage) y usa SDK/ZPL.

## 1.7.4

* Android: calibración de sensor prioriza `PrinterCalibrator` (`zpl.calibrate` JSON).
* Calibración sin perfil: no devuelve `PAPER_OUT`/`CALIBRATE_TIMEOUT` por estado
  transitorio tras el sensor (equivalente a calibrar desde el menú de la Zebra).

## 1.7.3

* Android: eliminada recuperación que re-calibraba en bucle ante `isPaperOut`
  (alimentaba papel sin imprimir). Tras una sola calibración se espera 2.5 s y
  se hace poll de estado/longitud; antes de imprimir solo se espera hasta 12 s.

## 1.7.2

* Android: recuperación automática ante `isPaperOut` (revertida en 1.7.3).

## 1.7.1

* Calibración: `PaperOutException` devuelve `PAPER_OUT` (no `CALIBRATE_ERROR`).
* `CalibrateMediaErrorCode.paperOut` con mensaje de tapa de compartimento.
* Calibración 12up: `forceSensorCalibration` y tolerancia de longitud ampliada vía opciones en app.
* Revertido experimento 605 dots (QA 24→12 adhesivo sin mejora).

## 1.7.0

* Added `ZebraBtPrinter.ensureMediaReadyForProfile` with `EnsureMediaReadyOptions`
  and `EnsureMediaReadyResult` (`skipped`, `settingsApplied`, `sensorCalibrated`).
* Android: reads media snapshot before running sensor calibration; skips work when
  SGD and `zpl.label_length` already match the profile.
* Default `closeConnectionAfter: false` on ensure (keeps BT session for print).

## 1.6.3

* Android: after sensor calibration, poll `zpl.label_length` until it matches the
  profile (instead of requiring a busy flag in printer status, which caused
  45s `CALIBRATE_TIMEOUT` on some mobile Zebra models).

## 1.6.2

* Android: calibration waits until the printer reports busy before accepting
  idle, so `zpl.label_length` is not read too early after 12up→24up sensor cal
  (avoids false `LABEL_LENGTH_MISMATCH` on the first attempt). Superseded by
  1.6.3 polling approach.

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
