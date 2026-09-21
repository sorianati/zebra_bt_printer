/// Valores por defecto de calibración de media (alineados con capa nativa).
abstract final class CalibrationDefaults {
  CalibrationDefaults._();

  static const defaultTimeout = Duration(seconds: 45);
  static const defaultTimeoutMs = 45000;
  static const minTimeoutMs = 5000;
  static const labelLengthToleranceDots = 40;

  /// Legacy [calibratePrinter]: no validar longitud sin perfil.
  static const legacyLabelLengthToleranceDots = 0x7FFFFFFF;
}
