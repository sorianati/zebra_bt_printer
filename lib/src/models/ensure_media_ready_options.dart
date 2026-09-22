import '../bridge/calibration_defaults.dart';
import '../bridge/plugin_arguments.dart';
import 'media_calibration_profile.dart';

/// Opciones para [ZebraBtPrinter.ensureMediaReadyForProfile].
class EnsureMediaReadyOptions {
  const EnsureMediaReadyOptions({
    required this.profile,
    this.forceSensorCalibration = false,
    this.saveSettingsToNvm = true,
    this.timeout = CalibrationDefaults.defaultTimeout,
    this.labelLengthToleranceDots = CalibrationDefaults.labelLengthToleranceDots,
    this.closeConnectionAfter = false,
  });

  final MediaCalibrationProfile profile;
  final bool forceSensorCalibration;
  final bool saveSettingsToNvm;
  final Duration timeout;
  final int labelLengthToleranceDots;

  /// Por defecto mantiene la sesión BT abierta para imprimir después.
  final bool closeConnectionAfter;

  Map<String, dynamic> toMap() => {
        PluginArguments.profile: profile.toMap(),
        PluginArguments.applyPersistentSettings: true,
        PluginArguments.runSensorCalibration: true,
        PluginArguments.saveSettingsToNvm: saveSettingsToNvm,
        PluginArguments.timeoutMs: timeout.inMilliseconds,
        PluginArguments.labelLengthToleranceDots: labelLengthToleranceDots,
        PluginArguments.closeConnectionAfter: closeConnectionAfter,
        PluginArguments.forceSensorCalibration: forceSensorCalibration,
      };
}
