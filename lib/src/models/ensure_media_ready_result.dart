import '../bridge/plugin_result_keys.dart';
import 'calibrate_media_error_code.dart';

/// Resultado de [ZebraBtPrinter.ensureMediaReadyForProfile].
class EnsureMediaReadyResult {
  final bool isSuccess;
  final bool skipped;
  final bool settingsApplied;
  final bool sensorCalibrated;
  final CalibrateMediaErrorCode? errorCode;
  final String? errorMessage;
  final int? detectedLabelLengthDots;
  final int? appliedPrintWidthDots;
  final Duration elapsed;

  const EnsureMediaReadyResult._({
    required this.isSuccess,
    this.skipped = false,
    this.settingsApplied = false,
    this.sensorCalibrated = false,
    this.errorCode,
    this.errorMessage,
    this.detectedLabelLengthDots,
    this.appliedPrintWidthDots,
    required this.elapsed,
  });

  const EnsureMediaReadyResult.success({
    bool skipped = false,
    bool settingsApplied = false,
    bool sensorCalibrated = false,
    int? detectedLabelLengthDots,
    int? appliedPrintWidthDots,
    Duration elapsed = Duration.zero,
  }) : this._(
          isSuccess: true,
          skipped: skipped,
          settingsApplied: settingsApplied,
          sensorCalibrated: sensorCalibrated,
          detectedLabelLengthDots: detectedLabelLengthDots,
          appliedPrintWidthDots: appliedPrintWidthDots,
          elapsed: elapsed,
        );

  EnsureMediaReadyResult.failure({
    CalibrateMediaErrorCode? errorCode,
    String? errorMessage,
    int? detectedLabelLengthDots,
    int? appliedPrintWidthDots,
    Duration elapsed = Duration.zero,
    String? rawErrorCode,
  }) : this._(
          isSuccess: false,
          errorCode: CalibrateMediaErrorCode.fromNative(
            rawErrorCode ?? errorCode?.nativeCode,
          ),
          errorMessage: errorMessage,
          detectedLabelLengthDots: detectedLabelLengthDots,
          appliedPrintWidthDots: appliedPrintWidthDots,
          elapsed: elapsed,
        );

  String? get userMessage => errorCode?.userMessage;

  static EnsureMediaReadyResult fromNativeMap(Map<dynamic, dynamic> map) {
    final isSuccess = map[PluginResultKeys.isSuccess] == true;
    final elapsedMs = map[PluginResultKeys.elapsedMs];
    final elapsed = elapsedMs is int
        ? Duration(milliseconds: elapsedMs)
        : Duration.zero;
    final skipped = map[PluginResultKeys.skipped] == true;
    final settingsApplied = map[PluginResultKeys.settingsApplied] == true;
    final sensorCalibrated = map[PluginResultKeys.sensorCalibrated] == true;

    if (isSuccess) {
      return EnsureMediaReadyResult.success(
        skipped: skipped,
        settingsApplied: settingsApplied,
        sensorCalibrated: sensorCalibrated,
        detectedLabelLengthDots:
            map[PluginResultKeys.detectedLabelLengthDots] as int?,
        appliedPrintWidthDots:
            map[PluginResultKeys.appliedPrintWidthDots] as int?,
        elapsed: elapsed,
      );
    }

    return EnsureMediaReadyResult.failure(
      errorCode: CalibrateMediaErrorCode.fromNative(
        map[PluginResultKeys.errorCode] as String?,
      ),
      errorMessage: map[PluginResultKeys.errorMessage] as String?,
      detectedLabelLengthDots:
          map[PluginResultKeys.detectedLabelLengthDots] as int?,
      appliedPrintWidthDots:
          map[PluginResultKeys.appliedPrintWidthDots] as int?,
      elapsed: elapsed,
      rawErrorCode: map[PluginResultKeys.errorCode] as String?,
    );
  }
}
