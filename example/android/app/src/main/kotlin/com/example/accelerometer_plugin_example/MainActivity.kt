package com.accelerometer.accelerometer_plugin

import androidx.annotation.NonNull

import android.app.ActivityManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.widget.Toast
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import android.provider.Settings
import android.os.Build
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import androidx.annotation.RequiresApi/** AccelerometerPlugin */
class AccelerometerPlugin: FlutterPlugin, MethodCallHandler {
    private val channelName = "magnetometer"
    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null

    private var gyro: Sensor? = null
    private val sensorDelay = 0;
    private val magneticValues = FloatArray(3)

    private val levelValues = FloatArray(3)

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        val channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)

        channel.setMethodCallHandler { call: MethodCall, result: MethodChannel.Result ->
            if (call.method == "streamMagnetometer") {
                startMagnetometerStream()
                result.success(null)
            }
            if (call.method == "setFlashlightOn") {
                setFlashlightOn();
                result.success(true)

            } else if(call.method=="setFlashlightOff"){
                setFlashlightOff()
                result.success(false)
            }
            if(call.method=="gyro"){
                startGyroStream()
                result.success(null)
            }
        }
    }

    private fun startMagnetometerStream() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, 16700)
        }
    }
    private fun setFlashlightOn(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIdList = cameraManager.cameraIdList
            for (cameraId in cameraIdList) {
                try {
                    cameraManager.setTorchMode(cameraId,true)
                } catch (e: Exception) {
                }
            }
        }
    }
    private fun setFlashlightOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIdList = cameraManager.cameraIdList
            for (cameraId in cameraIdList) {
                try {
                    cameraManager.setTorchMode(cameraId,false)
                } catch (e: Exception) {
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something when sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor == magnetometer) {
            magneticValues[0] = event.values[0]
            magneticValues[1] = event.values[1]
            magneticValues[2] = event.values[2]
            val channel = MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger, channelName)
            channel.invokeMethod("magnetometerData", magneticValues)
        }
        if(event!!.sensor==gyro){

            levelValues[0] = event.values[0]
            levelValues[1] = event.values[1]
            levelValues[2] = event.values[2]
            val channel = MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger, channelName)
            channel.invokeMethod("gyroData", levelValues)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
    private fun startGyroStream() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (gyro != null) {
            sensorManager.registerListener(this, gyro, 16700)
        }
    }
}