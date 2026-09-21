import '../bridge/calibration_defaults.dart';
import '../bridge/plugin_arguments.dart';
import 'media_calibration_profile.dart';

/// Opciones para [ZebraBtPrinter.calibrateMedia].
class CalibrateMediaOptions {
  const CalibrateMediaOptions({
    this.profile,
    this.applyPersistentSettings = true,
    this.runSensorCalibration = true,
    this.saveSettingsToNvm = true,
    this.timeout = CalibrationDefaults.defaultTimeout,
    this.labelLengthToleranceDots = CalibrationDefaults.labelLengthToleranceDots,
    this.closeConnectionAfter = true,
  });

  /// Si null, solo calibración de sensor sin cambiar SGD (legacy mejorado).
  final MediaCalibrationProfile? profile;

  final bool applyPersistentSettings;
  final bool runSensorCalibration;
  final bool saveSettingsToNvm;
  final Duration timeout;
  final int labelLengthToleranceDots;

  /// Cierra la conexión BT persistente al terminar (recomendado tras calibrar).
  final bool closeConnectionAfter;

  Map<String, dynamic> toMap() => {
        if (profile != null) PluginArguments.profile: profile!.toMap(),
        PluginArguments.applyPersistentSettings: applyPersistentSettings,
        PluginArguments.runSensorCalibration: runSensorCalibration,
        PluginArguments.saveSettingsToNvm: saveSettingsToNvm,
        PluginArguments.timeoutMs: timeout.inMilliseconds,
        PluginArguments.labelLengthToleranceDots: labelLengthToleranceDots,
        PluginArguments.closeConnectionAfter: closeConnectionAfter,
      };
}
