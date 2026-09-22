import 'package:flutter_test/flutter_test.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:zebra_bt_printer/src/bridge/plugin_arguments.dart';
import 'package:zebra_bt_printer/src/bridge/plugin_result_keys.dart';
import 'package:zebra_bt_printer/zebra_bt_printer.dart';
import 'package:zebra_bt_printer/zebra_bt_printer_method_channel.dart';

/// Plataforma falsa que registra las llamadas recibidas para poder verificarlas.
class MockZebraBtPrinterPlatform
    with MockPlatformInterfaceMixin
    implements ZebraBtPrinterPlatform {
  String? lastMethod;
  Map<String, dynamic> lastArgs = {};

  @override
  Future<PrintResult> printImageBluetooth({
    required String mac,
    required String imageBase64,
    PrinterConfig config = const PrinterConfig(),
    int copies = 1,
  }) async {
    lastMethod = 'printImageBluetooth';
    lastArgs = {
      'mac': mac,
      'imageBase64': imageBase64,
      'config': config,
      'copies': copies,
    };
    return const PrintResult.success();
  }

  @override
  Future<PrintResult> printImageIP({
    required String ip,
    required String imageBase64,
    PrinterConfig config = const PrinterConfig(),
  }) async {
    lastMethod = 'printImageIP';
    lastArgs = {'ip': ip, 'imageBase64': imageBase64, 'config': config};
    return const PrintResult.failure(
      errorCode: PrintErrorCode.printError,
      errorMessage: 'boom',
      rawErrorCode: 'PRINT_ERROR',
    );
  }

  @override
  Future<PrintResult> printLabelBluetooth({
    required String mac,
    required String zplText,
  }) async {
    lastMethod = 'printLabelBluetooth';
    lastArgs = {'mac': mac, 'zplText': zplText};
    return const PrintResult.success();
  }

  @override
  Future<bool> requestPermissions() async => true;

  @override
  Future<bool> isBluetoothEnabled() async => true;

  @override
  Future<bool> connectBluetooth({required String mac}) async => true;

  @override
  Future<bool> disconnectBluetooth({required String mac}) async => true;

  @override
  Future<bool> calibratePrinter({required String mac}) async => true;

  @override
  Future<CalibrateMediaResult> calibrateMedia({
    required String mac,
    CalibrateMediaOptions options = const CalibrateMediaOptions(),
  }) async {
    lastMethod = 'calibrateMedia';
    lastArgs = {'mac': mac, 'options': options};
    return const CalibrateMediaResult.success(
      detectedLabelLengthDots: 565,
      appliedPrintWidthDots: 575,
    );
  }

  @override
  Future<EnsureMediaReadyResult> ensureMediaReadyForProfile({
    required String mac,
    required EnsureMediaReadyOptions options,
  }) async {
    lastMethod = 'ensureMediaReadyForProfile';
    lastArgs = {'mac': mac, 'options': options};
    return const EnsureMediaReadyResult.success(skipped: true);
  }

  @override
  Future<PrinterMediaSnapshot?> getMediaSnapshot({required String mac}) async {
    lastMethod = 'getMediaSnapshot';
    lastArgs = {'mac': mac};
    return const PrinterMediaSnapshot(
      labelLengthDots: 250,
      printWidthDots: 600,
      mediaType: 'label',
      mediaSenseMode: 'bar',
    );
  }
}

void main() {
  final ZebraBtPrinterPlatform initialPlatform =
      ZebraBtPrinterPlatform.instance;

  test('MethodChannelZebraBtPrinter is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelZebraBtPrinter>());
  });

  group('ZebraBtPrinter facade delegates to the platform', () {
    late MockZebraBtPrinterPlatform fake;

    setUp(() {
      fake = MockZebraBtPrinterPlatform();
      ZebraBtPrinterPlatform.instance = fake;
    });

    test('printImageBluetooth forwards args and returns success', () async {
      final result = await ZebraBtPrinter.printImageBluetooth(
        mac: '48:A4:93:DB:04:6F',
        imageBase64: 'AAAA',
      );

      expect(result.isSuccess, isTrue);
      expect(fake.lastMethod, 'printImageBluetooth');
      expect(fake.lastArgs['mac'], '48:A4:93:DB:04:6F');
    });

    test('printImageIP surfaces failures', () async {
      final result = await ZebraBtPrinter.printImageIP(
        ip: '192.168.0.10',
        imageBase64: 'AAAA',
      );

      expect(result.isSuccess, isFalse);
      expect(result.errorCode, PrintErrorCode.printError);
      expect(result.errorMessage, 'boom');
      expect(result.userMessage, PrintErrorCode.printError.userMessage);
      expect(result.rawErrorCode, 'PRINT_ERROR');
    });

    test('requestPermissions / isBluetoothEnabled delegate', () async {
      expect(await ZebraBtPrinter.requestPermissions(), isTrue);
      expect(await ZebraBtPrinter.isBluetoothEnabled(), isTrue);
    });
  });

  group('PrintErrorCode', () {
    test('fromNative maps known codes', () {
      expect(
        PrintErrorCode.fromNative('PRINT_ERROR'),
        PrintErrorCode.printError,
      );
      expect(
        PrintErrorCode.fromNative('PERMISSION_DENIED'),
        PrintErrorCode.permissionDenied,
      );
      expect(
        PrintErrorCode.fromNative('PAPER_OUT'),
        PrintErrorCode.paperOut,
      );
      expect(
        PrintErrorCode.fromNative('PRINT_TIMEOUT'),
        PrintErrorCode.printTimeout,
      );
    });

    test('fromNative maps unknown / null to unknown', () {
      expect(PrintErrorCode.fromNative(null), PrintErrorCode.unknown);
      expect(PrintErrorCode.fromNative(''), PrintErrorCode.unknown);
      expect(PrintErrorCode.fromNative('FOO'), PrintErrorCode.unknown);
    });

    test('userMessage is stable and non-empty', () {
      for (final code in PrintErrorCode.values) {
        expect(code.userMessage, isNotEmpty);
        expect(code.nativeCode, isNotEmpty);
      }
    });
  });

  group('CalibrateMediaResult', () {
    test('fromNativeMap maps success and failure codes', () {
      final ok = CalibrateMediaResult.fromNativeMap({
        PluginResultKeys.isSuccess: true,
        PluginResultKeys.elapsedMs: 1200,
        PluginResultKeys.detectedLabelLengthDots: 565,
      });
      expect(ok.isSuccess, isTrue);
      expect(ok.elapsed.inMilliseconds, 1200);

      final fail = CalibrateMediaResult.fromNativeMap({
        PluginResultKeys.isSuccess: false,
        PluginResultKeys.errorCode: 'CALIBRATE_TIMEOUT',
        PluginResultKeys.errorMessage: 'timeout',
        PluginResultKeys.elapsedMs: 45000,
      });
      expect(fail.isSuccess, isFalse);
      expect(fail.errorCode, CalibrateMediaErrorCode.calibrateTimeout);
    });
  });

  group('MediaCalibrationProfile', () {
    test('toMap and fromMap round-trip', () {
      const profile = MediaCalibrationProfile(
        printWidthDots: 575,
        labelLengthDots: 565,
        mediaSense: MediaSenseMode.bar,
      );
      final restored = MediaCalibrationProfile.fromMap(profile.toMap());
      expect(restored?.printWidthDots, 575);
      expect(restored?.mediaSense, MediaSenseMode.bar);
    });
  });

  group('PrinterConfig', () {
    test('defaults serialize to a map', () {
      const config = PrinterConfig();
      final map = config.toMap();

      expect(map[PluginArguments.labelWidthDots], 600);
      expect(map[PluginArguments.labelHeightDots], 240);
      expect(map[PluginArguments.useSmoothScaling], true);
    });

    test('custom values serialize', () {
      const config = PrinterConfig(
        labelWidthDots: 800,
        labelHeightDots: 400,
        useSmoothScaling: false,
      );
      final map = config.toMap();

      expect(map[PluginArguments.labelWidthDots], 800);
      expect(map[PluginArguments.labelHeightDots], 400);
      expect(map[PluginArguments.useSmoothScaling], false);
    });
  });
}
