
import 'dart:async';

import 'package:flutter/services.dart';

class BlueDimple {
  static const MethodChannel _channel = MethodChannel('blue_dimple');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  void connect() async {
    await _channel.invokeMethod('connect');
  }

  Future<bool> isBluetoothEnabled() async {
    bool result = false;
    try {
      result = await _channel.invokeMethod('isBluetoothEnabled');
    } on PlatformException catch (e) {}
    return result;
  }

  void pair(String mac) async {
    await _channel.invokeMethod('pair');
  }

  void closeOutputStream() async {
    await _channel.invokeMethod('closeOutputStream');
  }

  Future<bool> writeBytes() async {
    final bool res = await _channel.invokeMethod('getPlatformVersion');
    return res;
  }
}
