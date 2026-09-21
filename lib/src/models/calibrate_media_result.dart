import '../bridge/plugin_result_keys.dart';
import 'calibrate_media_error_code.dart';

/// Resultado de [ZebraBtPrinter.calibrateMedia].
class CalibrateMediaResult {
  final bool isSuccess;
  final CalibrateMediaErrorCode? errorCode;
  final String? errorMessage;
  final int? detectedLabelLengthDots;
  final int? appliedPrintWidthDots;
  final Duration elapsed;

  const CalibrateMediaResult._({
    required this.isSuccess,
    this.errorCode,
    this.errorMessage,
    this.detectedLabelLengthDots,
    this.appliedPrintWidthDots,
    required this.elapsed,
  });

  const CalibrateMediaResult.success({
    int? detectedLabelLengthDots,
    int? appliedPrintWidthDots,
    Duration elapsed = Duration.zero,
  }) : this._(
          isSuccess: true,
          detectedLabelLengthDots: detectedLabelLengthDots,
          appliedPrintWidthDots: appliedPrintWidthDots,
          elapsed: elapsed,
        );

  CalibrateMediaResult.failure({
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

  static CalibrateMediaResult fromNativeMap(Map<dynamic, dynamic> map) {
    final isSuccess = map[PluginResultKeys.isSuccess] == true;
    final elapsedMs = map[PluginResultKeys.elapsedMs];
    final elapsed = elapsedMs is int
        ? Duration(milliseconds: elapsedMs)
        : Duration.zero;

    if (isSuccess) {
      return CalibrateMediaResult.success(
        detectedLabelLengthDots:
            map[PluginResultKeys.detectedLabelLengthDots] as int?,
        appliedPrintWidthDots:
            map[PluginResultKeys.appliedPrintWidthDots] as int?,
        elapsed: elapsed,
      );
    }

    return CalibrateMediaResult.failure(
      errorCode: CalibrateMediaErrorCode.fromNative(
        map[PluginResultKeys.errorCode] as String?,
      ),
      errorMessage: map[PluginResultKeys.errorMessage] as String?,
      detectedLabelLengthDots:
          map[PluginResultKeys.detectedLabelLengthDots] as int?,
      appliedPrintWidthDots: map[PluginResultKeys.appliedPrintWidthDots] as int?,
      elapsed: elapsed,
      rawErrorCode: map[PluginResultKeys.errorCode] as String?,
    );
  }

  @override
  String toString() => isSuccess
      ? 'CalibrateMediaResult(success, elapsed=$elapsed)'
      : 'CalibrateMediaResult(failure: [${errorCode?.name}] $userMessage'
          '${errorMessage != null ? ' | $errorMessage' : ''})';
}
