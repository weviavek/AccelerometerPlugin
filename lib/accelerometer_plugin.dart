import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class AccelerometerData {
  final double x;
  final double y;
  final double z;
  final DateTime timestamp;

  AccelerometerData({
    required this.x,
    required this.y,
    required this.z,
    required this.timestamp,
  });

  @override
  String toString() => 'AccelerometerData(x: $x, y: $y, z: $z, timestamp: $timestamp)';

  factory AccelerometerData.fromMap(Map<dynamic, dynamic> map) {
    return AccelerometerData(
      x: map['x'],
      y: map['y'],
      z: map['z'],
      timestamp: DateTime.fromMillisecondsSinceEpoch(map['timestamp']),
    );
  }
}

enum SensorUpdateRate {
  normal, // ~100ms between updates (10 Hz)
  game, // ~50ms between updates (20 Hz)
  fastest, // ~10ms between updates (100 Hz)
}

class AccelerometerPlugin {
  static const MethodChannel _methodChannel = MethodChannel('com.accelerometer.accelerometer_plugin/flutter_accelerometer/methods');
  static const EventChannel _eventChannel = EventChannel('com.accelerometer.accelerometer_plugin/flutter_accelerometer/events');

  static Stream<AccelerometerData>? _accelerometerStream;

  /// Returns a stream of accelerometer data
  static Stream<AccelerometerData> get accelerometerData {
    _accelerometerStream ??= _eventChannel.receiveBroadcastStream().map((dynamic event) => AccelerometerData.fromMap(event));
    return _accelerometerStream!;
  }

  /// Starts the accelerometer with the specified update rate
  static Future<bool> startAccelerometer(SensorUpdateRate rate) async {
    print('object');
    try {
      final result = await _methodChannel.invokeMethod<bool>(
        'startAccelerometer',
        {'rate': rate.toString().split('.').last},
      );
      return result ?? false;
    } on PlatformException catch (e) {
      debugPrint('Error starting accelerometer: ${e.message}');
      return false;
    }
  }

  /// Stops the accelerometer
  static Future<bool> stopAccelerometer() async {
    try {
      final result = await _methodChannel.invokeMethod<bool>('stopAccelerometer');
      return result ?? false;
    } on PlatformException catch (e) {
      debugPrint('Error stopping accelerometer: ${e.message}');
      return false;
    }
  }

  /// Returns true if the device has an accelerometer
  static Future<bool> isAccelerometerAvailable() async {
    try {
      final result = await _methodChannel.invokeMethod<bool>('isAccelerometerAvailable');
      return result ?? false;
    } on PlatformException catch (e) {
      debugPrint('Error checking accelerometer availability: ${e.message}');
      return false;
    }
  }
}
