/// Límites de impresión compartidos Dart ↔ nativo.
abstract final class PrintLimits {
  PrintLimits._();

  static const minCopies = 1;
  static const maxCopies = 999;
}
