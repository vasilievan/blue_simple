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

import java.lang.StringBuilder
import java.lang.System.currentTimeMillis
import java.util.concurrent.Executors
import java.util.UUID

import kotlin.concurrent.thread

/** BlueSimplePlugin */

class BlueSimplePlugin: FlutterPlugin, MethodCallHandler {
  private lateinit var  uuid: String
  private lateinit var  mac: String
  private lateinit var outputStream : OutputStream
  private lateinit var inputStream : InputStream
  private lateinit var channel : MethodChannel
  private lateinit var context : Context
  private lateinit var socket: BluetoothSocket

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "blue_simple")
    context = flutterPluginBinding.applicationContext
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "connect" -> {
        mac = call.argument<String>("mac")!!
        uuid = call.argument<String>("uuid")!!
        val connect = connect()
        result.success(connect)
      }
      "writeBytes" -> {
        val bytes: ByteArray = call.arguments as ByteArray
        var myResult = false
        if (this::outputStream.isInitialized) {
          writeBytes(bytes)
          myResult = true
        }
        result.success(myResult)
      }
      "isBluetoothEnabled" -> result.success(isBluetoothEnabled())
      "closeOutputStream" -> closeOutputStream()
      "closeInputStream" -> closeInputStream()
      "closeSocket" -> closeSocket()
      "readBytesFromSocket" -> result.success(readBytesFromSocket())
      else -> result.notImplemented()
    }
  }

  private fun connect(): Boolean {
    var result = false
    val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    var device: BluetoothDevice? = null
    try {
      device = manager.adapter.getRemoteDevice(mac!!)
    } catch (e: IllegalArgumentException) {
    }
    try {
      socket = device!!.createInsecureRfcommSocketToServiceRecord(UUID.fromString(uuid!!))
      socket.connect()
      outputStream = socket!!.outputStream
      inputStream = socket!!.inputStream
      result = true
    } catch (e: Exception) {
      result = false
    }
    return result
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
    try {
      outputStream.write(bytes)
      outputStream.flush()
    } catch (e: IOException) {
      println("Sending failed.")
    }
  }

  private fun readBytesFromSocket(): String {
    if (this::inputStream.isInitialized == false) {
      return ""
    }
    var readByte: Int
    val sb = StringBuilder()
    try {
      while (inputStream.read().also { readByte = it } != -1) {
        if (readByte == 255) {
          break
        }
        sb.append(readByte.toChar())
      }
    } catch (e: IOException) {
      println("Reading failed.")
    }
    return sb.toString()
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


