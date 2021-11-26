
import 'dart:async';

import 'package:flutter/services.dart';

class BlueSimple {
  static const MethodChannel _channel = MethodChannel('blue_simple');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  Future<bool> connect({required String mac}) async {
    bool result = false;
    try {
      result = await _channel.invokeMethod('connect', mac);
    } on PlatformException catch (e) {}
    return result;
  }

  Future<bool> writeBytes ({required List<int> bytes}) async {
    bool result = false;
    try {
      result = await _channel.invokeMethod('writeBytes', bytes);
    } on PlatformException catch (e) {}
    return result;
  }

  Future<bool> isBluetoothEnabled() {
    return _channel.invokeMethod('isBluetoothEnabled') as Future<bool>;
  }

  void closeOutputStream () {
    try {
      _channel.invokeMethod('closeOutputStream');
    } on PlatformException catch (e) {}
  }
}
