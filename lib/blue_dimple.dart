
import 'dart:async';

import 'package:flutter/services.dart';

class BlueDimple {
  static const MethodChannel _channel = MethodChannel('blue_dimple');

  Future<bool> isBluetoothEnabled() async {
    bool result = false;
    try {
      result = await _channel.invokeMethod('isBluetoothEnabled');
    } on PlatformException catch (e) {}
    return result;
  }

  Future<bool> sendDataToDevice(String mac, List<int> data) async {
    bool result = false;
    try {
      result = await _channel.invokeMethod('sendDataToDevice');
    } on PlatformException catch (e) {}
    return result;
  }
}
