/// Nombres de métodos invocados por el [MethodChannel].
abstract final class PluginMethods {
  PluginMethods._();

  static const connectBluetooth = 'connectBluetooth';
  static const disconnectBluetooth = 'disconnectBluetooth';
  static const calibratePrinter = 'calibratePrinter';
  static const calibrateMedia = 'calibrateMedia';
  static const getMediaSnapshot = 'getMediaSnapshot';
  static const printImageBluetooth = 'printImageBluetooth';
  static const printImageIP = 'printImageIP';
  static const printLabelBluetooth = 'printLabelBluetooth';
  static const requestPermissions = 'requestPermissions';
  static const isBluetoothEnabled = 'isBluetoothEnabled';
}
