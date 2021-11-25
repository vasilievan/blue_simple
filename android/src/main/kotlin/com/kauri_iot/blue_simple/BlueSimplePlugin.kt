package com.kauri_iot.blue_simple

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** BlueSimplePlugin */

class BlueSimplePlugin: FlutterPlugin, MethodCallHandler {
  private val kauriUUID: String = "04c6093b-0000-1000-8000-00805f9b34fb"
  private val channel = "bluetooth_simple/print"
  private lateinit var mac: String
  private lateinit var outputStream : OutputStream
  private lateinit var channel : MethodChannel

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "blue_simple")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if (call.method == "connect") {
      mac = call.arguments.toString();
      val connected = connect()
      if (outputStream != null) {
        result.success(true)
      }
      result.success(false)
    } else {
      result.notImplemented()
    }
  }

  private fun connect() {
    val manager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val adapter = manager.adapter
    val device = adapter.getRemoteDevice(mac)
    val socket = device.createRfcommSocketToServiceRecord(UUID.fromString(kauriUUID))
    socket.connect()
    outputStream = socket.outputStream
  }

  private fun writeBytes(bytes: ByteArray) {
    os.write(bytes)
    os.flush()
  }

  private fun closeOutputStream() {
    outputStream.close()
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}


