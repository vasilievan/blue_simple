package com.kauri_iot.blue_simple

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import android.bluetooth.BluetoothManager
import android.content.Context
import java.util.*
import java.io.OutputStream
import java.io.InputStream
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.lang.IllegalArgumentException
import java.io.IOException

/** BlueSimplePlugin */

class BlueSimplePlugin: FlutterPlugin, MethodCallHandler {
  private val kauriUUID: String = "04c6093b-0000-1000-8000-00805f9b34fb"
  private lateinit var mac: String
  private var outputStream : OutputStream? = null
  private var inputStream : InputStream? = null
  private lateinit var channel : MethodChannel
  private lateinit var context : Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "blue_simple")
    context = flutterPluginBinding.applicationContext
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
    } else if (call.method == "writeBytes") {
      val list: List<Int> = call.arguments as List<Int>
      val bytes: ByteArray = ByteArray(list.size)
      for (index in list.indices) {
        bytes[index] = list[index].toByte()
      }
      if (outputStream != null) {
        writeBytes(bytes)
        result.success(true)
      }
      result.success(false)
    } else if (call.method == "isBluetoothEnabled") {
      result.success(isBluetoothEnabled())
    } else if (call.method == "closeOutputStream") {
      closeOutputStream()
    } else if (call.method == "closeInputStream") {
      closeInputStream()
    } else if (call.method == "readBytes") {
      val bytes = readBytes()
      result.success(bytes)
    } else {
      result.notImplemented()
    }
  }

  private fun isBluetoothEnabled(): Boolean {
    val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    if (manager == null) {
      return false;
    } else if (manager.adapter.isEnabled) {
      return true;
    } else {
      return false;
    }
  }

  private fun connect(): Boolean {
    val manager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val adapter = manager.adapter
    var device: BluetoothDevice? = null;
    try {
      device = adapter.getRemoteDevice(mac)
    } catch (e: java.lang.IllegalArgumentException) {
    }
    if (device == null) {
      return false
    }
    var socket: BluetoothSocket? = null
    try {
      socket = device.createRfcommSocketToServiceRecord(UUID.fromString(kauriUUID))
    } catch (e: IOException) {
    }
    if (socket == null) {
      return false
    }
    socket.connect()
    outputStream = socket.outputStream
    inputStream = socket.inputStream
    return true;
  }

  private fun writeBytes(bytes: ByteArray) {
    outputStream!!.write(bytes)
    outputStream!!.flush()
  }

  private fun readBytes(): ByteArray {
    return inputStream!!.readBytes();
  }

  private fun closeInputStream() {
    inputStream!!.close()
  }

  private fun closeOutputStream() {
    outputStream!!.close()
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}


