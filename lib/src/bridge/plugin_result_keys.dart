/// Claves de mapas devueltos por calibración / snapshot (contrato puente).
abstract final class PluginResultKeys {
  PluginResultKeys._();

  static const isSuccess = 'isSuccess';
  static const errorCode = 'errorCode';
  static const errorMessage = 'errorMessage';
  static const detectedLabelLengthDots = 'detectedLabelLengthDots';
  static const appliedPrintWidthDots = 'appliedPrintWidthDots';
  static const elapsedMs = 'elapsedMs';

  static const printWidthDots = 'printWidthDots';
  static const labelLengthDots = 'labelLengthDots';
  static const mediaType = 'mediaType';
  static const mediaSenseMode = 'mediaSenseMode';
}
