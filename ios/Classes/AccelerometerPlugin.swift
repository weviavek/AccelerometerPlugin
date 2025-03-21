// ios/Classes/SwiftFlutterAccelerometerPlugin.swift
import Flutter
import UIKit
import CoreMotion

public class SwiftFlutterAccelerometerPlugin: NSObject, FlutterPlugin, FlutterStreamHandler {
  private let motionManager = CMMotionManager()
  private var eventSink: FlutterEventSink?
  private var updateInterval: TimeInterval = 0.1 // Default update interval (10 Hz)
  
  public static func register(with registrar: FlutterPluginRegistrar) {
    let methodChannel = FlutterMethodChannel(name: "com.example/flutter_accelerometer/methods", binaryMessenger: registrar.messenger())
    let eventChannel = FlutterEventChannel(name: "com.example/flutter_accelerometer/events", binaryMessenger: registrar.messenger())
    
    let instance = SwiftFlutterAccelerometerPlugin()
    registrar.addMethodCallDelegate(instance, channel: methodChannel)
    eventChannel.setStreamHandler(instance)
  }
  
  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "startAccelerometer":
      if let args = call.arguments as? [String: Any],
         let rateString = args["rate"] as? String {
        
        switch rateString {
        case "game":
          updateInterval = 0.05 // 20 Hz
        case "fastest":
          updateInterval = 0.01 // 100 Hz
        default:
          updateInterval = 0.1  // 10 Hz
        }
      }
      
      let success = startAccelerometer()
      result(success)
      
    case "stopAccelerometer":
      stopAccelerometer()
      result(true)
      
    case "isAccelerometerAvailable":
      result(motionManager.isAccelerometerAvailable)
      
    default:
      result(FlutterMethodNotImplemented)
    }
  }
  
  private func startAccelerometer() -> Bool {
    if !motionManager.isAccelerometerAvailable {
      return false
    }
    
    if !motionManager.isAccelerometerActive {
      motionManager.accelerometerUpdateInterval = updateInterval
      motionManager.startAccelerometerUpdates(to: .main) { [weak self] (data, error) in
        guard let data = data, error == nil, let eventSink = self?.eventSink else {
          return
        }
        
        let accelerometerData: [String: Any] = [
          "x": data.acceleration.x,
          "y": data.acceleration.y,
          "z": data.acceleration.z,
          "timestamp": Int(Date().timeIntervalSince1970 * 1000)
        ]
        
        eventSink(accelerometerData)
      }
    }
    
    return true
  }
  
  private func stopAccelerometer() {
    if motionManager.isAccelerometerActive {
      motionManager.stopAccelerometerUpdates()
    }
  }
  
  // MARK: - FlutterStreamHandler
  
  public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
    self.eventSink = events
    return nil
  }
  
  public func onCancel(withArguments arguments: Any?) -> FlutterError? {
    self.eventSink = nil
    stopAccelerometer()
    return nil
  }
}