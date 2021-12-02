
import 'dart:async';

import 'package:flutter/services.dart';

class BlueDimple {
  static const MethodChannel _channel = MethodChannel('blue_dimple');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static void connect() async {
    await _channel.invokeMethod('connect');
  }

  static void pair(String mac) async {
    await _channel.invokeMethod('pair');
  }

  static void closeOutputStream() async {
    await _channel.invokeMethod('closeOutputStream');
  }

  static Future<bool> writeBytes() async {
    final bool res = await _channel.invokeMethod('getPlatformVersion');
    return res;
  }
}
