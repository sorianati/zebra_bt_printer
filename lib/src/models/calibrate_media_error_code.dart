/// Códigos de error para [CalibrateMediaResult] (calibración de media).
enum CalibrateMediaErrorCode {
  invalidArgs(
    'INVALID_ARGS',
    'Parámetros de calibración inválidos.',
  ),
  permissionDenied(
    'PERMISSION_DENIED',
    'Faltan permisos Bluetooth para calibrar la impresora.',
  ),
  connectError(
    'CONNECT_ERROR',
    'No se pudo conectar con la impresora para calibrar.',
  ),
  calibrateError(
    'CALIBRATE_ERROR',
    'No se pudo calibrar la impresora. Verifica la conexión e inténtalo de nuevo.',
  ),
  calibrateTimeout(
    'CALIBRATE_TIMEOUT',
    'La impresora no terminó la calibración a tiempo.',
  ),
  labelLengthMismatch(
    'LABEL_LENGTH_MISMATCH',
    'La longitud detectada no coincide con el perfil esperado. '
        'Verifica el rollo o calibra manualmente.',
  ),
  unsupportedPrinter(
    'UNSUPPORTED_PRINTER',
    'La impresora no soporta calibración por SDK; el intento alternativo falló.',
  ),
  unsupportedPlatform(
    'UNSUPPORTED_PLATFORM',
    'La calibración de media no está disponible en esta plataforma.',
  ),
  unknown(
    'UNKNOWN',
    'Error desconocido al calibrar la impresora.',
  );

  const CalibrateMediaErrorCode(this.nativeCode, this.userMessage);

  final String nativeCode;
  final String userMessage;

  static CalibrateMediaErrorCode fromNative(String? code) {
    if (code == null || code.isEmpty) {
      return CalibrateMediaErrorCode.unknown;
    }
    for (final value in CalibrateMediaErrorCode.values) {
      if (value.nativeCode == code) {
        return value;
      }
    }
    return CalibrateMediaErrorCode.unknown;
  }
}
