/*
package com.kauri_iot.blue_dimple

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.logging.Level
import java.util.logging.Logger
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import java.lang.reflect.Method
import com.kauri_iot.blue_dimple.Receiver
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import androidx.lifecycle.LifecycleOwner
import android.content.Intent
import android.app.Activity

*/
/** BlueDimplePlugin *//*


class BlueDimplePlugin: FlutterPlugin, MethodCallHandler, ActivityAware  {
  private lateinit var channel : MethodChannel
  private val logger: Logger = Logger.getLogger(BlueDimplePlugin::javaClass.name)
  private lateinit var manager: BluetoothManager
  private lateinit var adapter: BluetoothAdapter
  private lateinit var context : Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "blue_dimple")
    context = flutterPluginBinding.applicationContext
    manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    adapter = manager.adapter
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "isBluetoothEnabled") {
      result.success(isBluetoothEnabled())
    } else if (call.method == "sendDataToDevice") {
      val mac: String? = call.argument("mac") as String?
      val ip: String? = call.argument("ip") as String?
      val password: String? = call.argument("password") as String?
      result.success(sendDataToDevice(mac!!, ip!!, password!!))
    } else {
      result.notImplemented()
    }
  }

  private fun sendDataToDevice(mac: String, ip: String, password: String) {
    pair(mac);
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

  private fun isPaired(mac: String): Boolean {
    if (adapter.bondedDevices.any { it -> it.address == mac }) {
      return true;
    }
    return false;
  }

  private fun pair(mac: String) {
    if (isPaired(mac)) {
      val intent = Intent();
      intent.action = BluetoothDevice.ACTION_BOND_STATE_CHANGED;
      intent.putExtra(BluetoothDevice.EXTRA_BOND_STATE, 12);
      intent.putExtra("mac", mac);
      activity.sendBroadcast(intent);
      return
    }
    adapter.startDiscovery()
    liveData.observe(lifecycleOwner, { it ->
      if (it.address == mac) {
        val method: Method = it.javaClass.getMethod("createBond")
        method.invoke(it)
        adapter.cancelDiscovery()
      }
    })
    logger.log(Level.INFO, "Pairing is started.")
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    flutterEngine.getBroadcastReceiverControlSurface().detachFromBroadcastReceiver();
    channel.setMethodCallHandler(null)
  }

  private fun connect(mac: String, context: Context) {
    val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val adapter = manager.adapter
    val boundedDevices = adapter.bondedDevices.toList()
    val device = boundedDevices.first { it -> it.address == mac }
    val method = device.javaClass.getMethod("createInsecureRfcommSocket", Int::class.java)
    val deviceSocket = method.invoke(device, 1) as BluetoothSocket
    deviceSocket.connect()
    outputStream = deviceSocket.outputStream
  }

  private fun writeBytes(bytes: ByteArray) {
    outputStream!!.write(bytes)
    outputStream!!.flush()
  }

  private fun closeOutputStream() {
    outputStream!!.close()
  }

*/
/*  if (intent?.action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
    val state = intent?.extras?.get(BluetoothDevice.EXTRA_BOND_STATE) as Int
    val mac = intent?.extras?.get("mac") as String
    if (state == 12) {
      thread {
        logger.log(Level.INFO, "Paired.")
        connect(mac, context!!)
        writeBytes(byteArrayOf(1, 2))
        Thread.sleep(2000)
        closeOutputStream()
      }
    }
  } else if (intent?.action.equals(BluetoothDevice.ACTION_FOUND)) {
    val device = intent?.extras?.get(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
    this.device.value = device
    logger.log(Level.INFO, "Device found: ${device.name}")
  }*//*

}
*/
