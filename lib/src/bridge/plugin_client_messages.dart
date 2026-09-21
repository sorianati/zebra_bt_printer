import '../models/print_error_code.dart';

/// Mensajes generados en Dart cuando el canal no devuelve detalle nativo.
abstract final class PluginClientMessages {
  PluginClientMessages._();

  static String unknownPlatformError() =>
      PrintErrorCode.unknown.userMessage;

  static const emptyNativeCalibrationResponse =
      'Respuesta vacía del canal nativo';
}
