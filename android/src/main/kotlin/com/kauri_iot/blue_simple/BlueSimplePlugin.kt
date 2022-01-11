package com.kauri_iot.blue_simple

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

import android.content.Context

import java.io.OutputStream
import java.io.InputStream

import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothAdapter

import java.io.IOException
import java.lang.IllegalArgumentException

import java.lang.System.currentTimeMillis
import java.util.concurrent.Executors
import java.util.UUID

/** BlueSimplePlugin */

class BlueSimplePlugin: FlutterPlugin, MethodCallHandler {
  private var uuid: String? = null
  private var mac: String? = null
  private lateinit var outputStream : OutputStream
  private lateinit var inputStream : InputStream
  private lateinit var channel : MethodChannel
  private lateinit var context : Context
  private lateinit var socket: BluetoothSocket
  private val timeRestriction = 5000
  private val executorService = Executors.newSingleThreadExecutor {
    val thread = Thread(
            it
    )
    thread.isDaemon = true
    thread
  }

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "blue_simple")
    context = flutterPluginBinding.applicationContext
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "connect") {
      mac = call.argument("mac") as String?;
      uuid = call.argument("uuid") as String?;
      result.success(connect())
    } else if (call.method == "writeBytes") {
      val list: List<Int> = call.arguments as List<Int>
      val bytes: ByteArray = ByteArray(list.size)
      for (index in list.indices) {
        bytes[index] = list[index].toByte()
      }
      if (this::outputStream.isInitialized) {
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
    } else if (call.method == "closeSocket") {
      closeSocket()
    } else if (call.method == "readBytes") {
      result.success(readBytes())
    } else {
      result.notImplemented()
    }
  }

  private fun connect(): Boolean {
    val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val device: BluetoothDevice?
    try {
      device = manager.adapter.getRemoteDevice(mac!!)
    } catch (e: IllegalArgumentException) {
      return false
    }
    try {
      socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(uuid!!))
    } catch (e: IOException) {
      return false
    }
    outputStream = socket.outputStream
    inputStream = socket.inputStream
    return true
  }

  private fun isBluetoothEnabled(): Boolean {
    val manager: BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
    val adapter: BluetoothAdapter? = manager?.adapter
    if (manager == null || adapter == null) {
      return false
    }
    return adapter.isEnabled
  }

  private fun writeBytes(bytes: ByteArray) {
    executorService.submit {
      outputStream.write(bytes)
      outputStream.flush()
    }
  }

  private fun readBytes(): List<Int> {
    return executorService.submit {
      val time = currentTimeMillis()
      val result = mutableListOf<Int>()
      var now: Long
      var nextByte = inputStream!!.read()
      while (true) {
        now = currentTimeMillis()
        result.add(nextByte)
        if (nextByte == -1 || (now - time >= timeRestriction)) {
          break
        }
        nextByte = inputStream!!.read()
      }
    }.get() as List<Int>
  }

  private fun closeInputStream() {
    if (this::inputStream.isInitialized) {
      inputStream.close()
    }
  }

  private fun closeOutputStream() {
    if (this::outputStream.isInitialized) {
      outputStream.close()
    }
  }

  private fun closeSocket() {
    if (this::socket.isInitialized) {
      socket.close()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}


