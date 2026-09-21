import '../bridge/plugin_arguments.dart';

/// Modo de detección de media persistente (SGD `media.sense_mode`).
enum MediaSenseMode {
  gap,
  bar,
}

/// Tipo de media persistente (SGD `media.type`).
enum MediaType {
  label,
  journal,
}

/// Perfil de calibración al cambiar rollo o tipo de etiqueta lógico.
///
/// El plugin traduce a SGD + calibración de sensor; la app no envía ZPL crudo.
class MediaCalibrationProfile {
  const MediaCalibrationProfile({
    required this.printWidthDots,
    required this.labelLengthDots,
    required this.mediaSense,
    this.mediaType = MediaType.label,
    this.maxLabelLengthDots,
  });

  /// Ancho de impresión persistente → SGD `ezpl.print_width`.
  final int printWidthDots;

  /// Longitud esperada (dots) → validar contra `zpl.label_length` tras calibrar.
  final int labelLengthDots;

  /// Gap entre etiquetas vs marca negra en reverso.
  final MediaSenseMode mediaSense;

  final MediaType mediaType;

  /// Opcional → `ezpl.label_length_max` / alineación con ZPL `^ML`.
  final int? maxLabelLengthDots;

  Map<String, dynamic> toMap() => {
        PluginArguments.printWidthDots: printWidthDots,
        PluginArguments.labelLengthDots: labelLengthDots,
        PluginArguments.mediaSense: mediaSense.name,
        PluginArguments.mediaType: mediaType.name,
        if (maxLabelLengthDots != null)
          PluginArguments.maxLabelLengthDots: maxLabelLengthDots,
      };

  static MediaCalibrationProfile? fromMap(Map<dynamic, dynamic>? map) {
    if (map == null) return null;
    final printWidth = map[PluginArguments.printWidthDots];
    final labelLength = map[PluginArguments.labelLengthDots];
    final sense = map[PluginArguments.mediaSense] as String?;
    if (printWidth is! int || labelLength is! int || sense == null) {
      return null;
    }
    return MediaCalibrationProfile(
      printWidthDots: printWidth,
      labelLengthDots: labelLength,
      mediaSense: MediaSenseMode.values.firstWhere(
        (e) => e.name == sense,
        orElse: () => MediaSenseMode.gap,
      ),
      mediaType: MediaType.values.firstWhere(
        (e) => e.name == (map[PluginArguments.mediaType] as String?),
        orElse: () => MediaType.label,
      ),
      maxLabelLengthDots: map[PluginArguments.maxLabelLengthDots] as int?,
    );
  }
}
