
import 'dart:async';
import 'dart:developer';

import 'package:flutter/services.dart';

class BlueSimple {
  static const MethodChannel _channel = MethodChannel('blue_simple');

  Future<bool> connect({required String mac, required String uuid}) async {
    bool result = false;
    try {
      result = await _channel.invokeMethod('connect', {mac, uuid});
    } on PlatformException catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<bool> writeBytes ({required List<int> bytes}) async {
    bool result = false;
    try {
      result = await _channel.invokeMethod('writeBytes', bytes);
    } on PlatformException catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<List<int>> readBytes () async {
    List<int> result = List.empty();
    try {
      var preresult = await _channel.invokeMethod('readBytes');
      result = preresult;
    } on PlatformException catch (e) {
      log(e.toString());
    }
    return result;
  }

  Future<bool> isBluetoothEnabled() async {
    bool result = false;
    try {
      result = await _channel.invokeMethod('isBluetoothEnabled');
    } on PlatformException catch (e) {
      log(e.toString());
    }
    return result;
  }

  void closeOutputStream () {
    try {
      _channel.invokeMethod('closeOutputStream');
    } on PlatformException catch (e) {
      log(e.toString());
    }
  }

  void closeInputStream () {
    try {
      _channel.invokeMethod('closeInputStream');
    } on PlatformException catch (e) {
      log(e.toString());
    }
  }

  void closeSocket () {
    try {
      _channel.invokeMethod('closeSocket');
    } on PlatformException catch (e) {
      log(e.toString());
    }
  }
}
