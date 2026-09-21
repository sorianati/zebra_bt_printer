/// Claves del mapa de argumentos del [MethodChannel] (contrato puente).
abstract final class PluginArguments {
  PluginArguments._();

  static const mac = 'mac';
  static const ip = 'ip';
  static const imageBase64 = 'imageBase64';
  static const zplText = 'zplText';
  static const copies = 'copies';

  static const labelWidthDots = 'labelWidthDots';
  static const labelHeightDots = 'labelHeightDots';
  static const useSmoothScaling = 'useSmoothScaling';
  static const printerType = 'printerType';
  static const mediaType = 'mediaType';
  static const allowUpscale = 'allowUpscale';
  static const maxLabelLengthDots = 'maxLabelLengthDots';
  static const labelTopOffset = 'labelTopOffset';

  static const profile = 'profile';
  static const printWidthDots = 'printWidthDots';
  static const labelLengthDots = 'labelLengthDots';
  static const mediaSense = 'mediaSense';
  static const applyPersistentSettings = 'applyPersistentSettings';
  static const runSensorCalibration = 'runSensorCalibration';
  static const saveSettingsToNvm = 'saveSettingsToNvm';
  static const timeoutMs = 'timeoutMs';
  static const labelLengthToleranceDots = 'labelLengthToleranceDots';
  static const closeConnectionAfter = 'closeConnectionAfter';
}
