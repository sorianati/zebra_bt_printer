import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'src/bridge/calibration_defaults.dart';
import 'src/bridge/plugin_arguments.dart';
import 'src/bridge/plugin_channel.dart';
import 'src/bridge/plugin_client_messages.dart';
import 'src/bridge/plugin_methods.dart';
import 'src/bridge/print_limits.dart';
import 'src/models/calibrate_media_error_code.dart';
import 'src/models/calibrate_media_options.dart';
import 'src/models/calibrate_media_result.dart';
import 'src/models/print_error_code.dart';
import 'src/models/print_result.dart';
import 'src/models/printer_config.dart';
import 'src/models/printer_media_snapshot.dart';
import 'zebra_bt_printer_platform_interface.dart';

class MethodChannelZebraBtPrinter extends ZebraBtPrinterPlatform {
  @visibleForTesting
  final methodChannel = const MethodChannel(PluginChannel.name);

  PrintResult _failureFrom(PlatformException e) {
    return PrintResult.failure(
      errorCode: PrintErrorCode.fromNative(e.code),
      errorMessage: e.message ?? PluginClientMessages.unknownPlatformError(),
      rawErrorCode: e.code,
    );
  }

  @override
  Future<PrintResult> printImageBluetooth({
    required String mac,
    required String imageBase64,
    PrinterConfig config = const PrinterConfig(),
    int copies = 1,
  }) async {
    try {
      await methodChannel.invokeMethod<void>(PluginMethods.printImageBluetooth, {
        PluginArguments.mac: mac,
        PluginArguments.imageBase64: imageBase64,
        PluginArguments.copies: copies.clamp(PrintLimits.minCopies, PrintLimits.maxCopies),
        ...config.toMap(),
      });
      return const PrintResult.success();
    } on PlatformException catch (e) {
      return _failureFrom(e);
    }
  }

  @override
  Future<PrintResult> printImageIP({
    required String ip,
    required String imageBase64,
    PrinterConfig config = const PrinterConfig(),
  }) async {
    try {
      await methodChannel.invokeMethod<void>(PluginMethods.printImageIP, {
        PluginArguments.ip: ip,
        PluginArguments.imageBase64: imageBase64,
        ...config.toMap(),
      });
      return const PrintResult.success();
    } on PlatformException catch (e) {
      return _failureFrom(e);
    }
  }

  @override
  Future<PrintResult> printLabelBluetooth({
    required String mac,
    required String zplText,
  }) async {
    try {
      await methodChannel.invokeMethod<void>(PluginMethods.printLabelBluetooth, {
        PluginArguments.mac: mac,
        PluginArguments.zplText: zplText,
      });
      return const PrintResult.success();
    } on PlatformException catch (e) {
      return _failureFrom(e);
    }
  }

  @override
  Future<bool> requestPermissions() async {
    final granted = await methodChannel.invokeMethod<bool>(
      PluginMethods.requestPermissions,
    );
    return granted ?? false;
  }

  @override
  Future<bool> isBluetoothEnabled() async {
    final enabled = await methodChannel.invokeMethod<bool>(
      PluginMethods.isBluetoothEnabled,
    );
    return enabled ?? false;
  }

  @override
  Future<bool> connectBluetooth({required String mac}) async {
    final ok = await methodChannel.invokeMethod<bool>(
      PluginMethods.connectBluetooth,
      {PluginArguments.mac: mac},
    );
    return ok ?? false;
  }

  @override
  Future<bool> disconnectBluetooth({required String mac}) async {
    final ok = await methodChannel.invokeMethod<bool>(
      PluginMethods.disconnectBluetooth,
      {PluginArguments.mac: mac},
    );
    return ok ?? false;
  }

  @override
  Future<bool> calibratePrinter({required String mac}) async {
    final ok = await methodChannel.invokeMethod<bool>(
      PluginMethods.calibratePrinter,
      {PluginArguments.mac: mac},
    );
    return ok ?? false;
  }

  @override
  Future<CalibrateMediaResult> calibrateMedia({
    required String mac,
    CalibrateMediaOptions options = const CalibrateMediaOptions(),
  }) async {
    try {
      final map = await methodChannel.invokeMethod<Map<dynamic, dynamic>>(
        PluginMethods.calibrateMedia,
        {
          PluginArguments.mac: mac,
          ...options.toMap(),
        },
      );
      if (map == null) {
        return CalibrateMediaResult.failure(
          errorCode: CalibrateMediaErrorCode.unknown,
          errorMessage: PluginClientMessages.emptyNativeCalibrationResponse,
        );
      }
      return CalibrateMediaResult.fromNativeMap(map);
    } on PlatformException catch (e) {
      return CalibrateMediaResult.failure(
        errorCode: CalibrateMediaErrorCode.fromNative(e.code),
        errorMessage: e.message,
        rawErrorCode: e.code,
      );
    }
  }

  @override
  Future<PrinterMediaSnapshot?> getMediaSnapshot({required String mac}) async {
    try {
      final map = await methodChannel.invokeMethod<Map<dynamic, dynamic>>(
        PluginMethods.getMediaSnapshot,
        {PluginArguments.mac: mac},
      );
      return PrinterMediaSnapshot.fromMap(map);
    } on PlatformException {
      return null;
    }
  }
}
