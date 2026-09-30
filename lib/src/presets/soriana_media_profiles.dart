import '../models/media_calibration_profile.dart';

/// Perfiles de media alineados con Fenicia Móvil (12up / 24up, marca negra).
abstract final class SorianaMediaProfiles {
  SorianaMediaProfiles._();

  /// Tear off en dots (`ezpl.tear_off`) por tamaño en tienda.
  static const fenicia12UpTearOffDots = 0;
  static const fenicia24UpTearOffDots = 13;

  static const fenicia24Up = MediaCalibrationProfile(
    printWidthDots: 600,
    labelLengthDots: 250,
    mediaSense: MediaSenseMode.bar,
    tearOffDots: fenicia24UpTearOffDots,
  );

  static const fenicia12Up = MediaCalibrationProfile(
    printWidthDots: 575,
    labelLengthDots: 565,
    mediaSense: MediaSenseMode.bar,
    tearOffDots: fenicia12UpTearOffDots,
  );
}
