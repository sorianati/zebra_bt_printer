import '../models/media_calibration_profile.dart';

/// Perfiles de media alineados con Fenicia Móvil (12up / 24up, marca negra).
abstract final class SorianaMediaProfiles {
  SorianaMediaProfiles._();

  static const fenicia24Up = MediaCalibrationProfile(
    printWidthDots: 600,
    labelLengthDots: 250,
    mediaSense: MediaSenseMode.bar,
  );

  static const fenicia12Up = MediaCalibrationProfile(
    printWidthDots: 575,
    labelLengthDots: 565,
    mediaSense: MediaSenseMode.bar,
  );
}
