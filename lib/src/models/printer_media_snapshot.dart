import '../bridge/plugin_result_keys.dart';

/// Lectura de configuración de media en la impresora (SGD).
class PrinterMediaSnapshot {
  const PrinterMediaSnapshot({
    this.labelLengthDots,
    this.printWidthDots,
    this.mediaType,
    this.mediaSenseMode,
  });

  final int? labelLengthDots;
  final int? printWidthDots;
  final String? mediaType;
  final String? mediaSenseMode;

  static PrinterMediaSnapshot? fromMap(Map<dynamic, dynamic>? map) {
    if (map == null) return null;
    return PrinterMediaSnapshot(
      labelLengthDots: map[PluginResultKeys.labelLengthDots] as int?,
      printWidthDots: map[PluginResultKeys.printWidthDots] as int?,
      mediaType: map[PluginResultKeys.mediaType] as String?,
      mediaSenseMode: map[PluginResultKeys.mediaSenseMode] as String?,
    );
  }
}
