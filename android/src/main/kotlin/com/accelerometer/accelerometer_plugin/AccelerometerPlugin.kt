package com.accelerometer.accelerometer_plugin

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class AccelerometerPlugin: FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler, SensorEventListener {
  private lateinit var methodChannel: MethodChannel
  private lateinit var eventChannel: EventChannel
  private lateinit var context: Context

  // Sensor related variables
  private var sensorManager: SensorManager? = null
  private var accelerometer: Sensor? = null
  private var eventSink: EventChannel.EventSink? = null
  private var isListening = false

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext
    methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "com.accelerometer.accelerometer_plugin/flutter_accelerometer/methods")
    eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "com.accelerometer.accelerometer_plugin/flutter_accelerometer/events")

    methodChannel.setMethodCallHandler(this)
    eventChannel.setStreamHandler(this)

    // Initialize sensor manager
    sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    print("asasd");
    when (call.method) {
      "startAccelerometer" -> {
        val rateString = call.argument<String>("rate") ?: "normal"
        val rate = when (rateString) {
          "game" -> SensorManager.SENSOR_DELAY_GAME
          "fastest" -> SensorManager.SENSOR_DELAY_FASTEST
          else -> SensorManager.SENSOR_DELAY_NORMAL
        }

        val success = startListening(rate)
        result.success(success)
      }
      "stopAccelerometer" -> {
        stopListening()
        result.success(true)
      }
      "isAccelerometerAvailable" -> {
        result.success(accelerometer != null)
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  private fun startListening(rate: Int): Boolean {
    if (accelerometer == null) {
      return false
    }

    if (!isListening) {
      sensorManager?.registerListener(this, accelerometer, rate)
      isListening = true
    }
    return true
  }

  private fun stopListening() {
    if (isListening) {
      sensorManager?.unregisterListener(this)
      isListening = false
    }
  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    eventSink = events
  }

  override fun onCancel(arguments: Any?) {
    eventSink = null
    stopListening()
  }

  override fun onSensorChanged(event: SensorEvent?) {
    if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
      val data = HashMap<String, Any>()
      data["x"] = event.values[0]
      data["y"] = event.values[1]
      data["z"] = event.values[2]
      data["timestamp"] = System.currentTimeMillis()

      eventSink?.success(data)
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    // Not used in this implementation
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    stopListening()
    methodChannel.setMethodCallHandler(null)
    eventChannel.setStreamHandler(null)
  }
}