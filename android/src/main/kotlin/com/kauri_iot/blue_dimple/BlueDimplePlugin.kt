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
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import java.lang.reflect.Method
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.random.Random
import com.kauri_iot.blue_dimple.Receiver
import java.io.OutputStream
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import androidx.lifecycle.LifecycleOwner

/** BlueDimplePlugin */

class BlueDimplePlugin: FlutterPlugin, MethodCallHandler, ActivityAware  {
  private lateinit var channel : MethodChannel
  private val logger: Logger = Logger.getLogger(BlueDimplePlugin::javaClass.name)
  private lateinit var receiver: Receiver
  private lateinit var manager: BluetoothManager
  private lateinit var adapter: BluetoothAdapter
  private lateinit var liveData: MutableLiveData<BluetoothDevice>
  private lateinit var context : Context
  private lateinit var flutterEngine: FlutterEngine
  private lateinit var lifecycleOwner: LifecycleOwner
  private var mac = ""
  private var outputStream : OutputStream? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "blue_dimple")
    context = flutterPluginBinding.applicationContext
    receiver = Receiver()
    liveData = receiver.device
    manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    adapter = manager.adapter
    flutterEngine = FlutterEngine(context, null)
    flutterEngine.getBroadcastReceiverControlSurface().attachToBroadcastReceiver(receiver,
      object : Lifecycle() {
        override fun addObserver(observer: LifecycleObserver) {
        }

        override fun removeObserver(observer: LifecycleObserver) {
        }

        override fun getCurrentState(): State {
          return State.RESUMED;
        }
      });
    channel.setMethodCallHandler(this)
  }

  override fun onDetachedFromActivity() {}

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    lifecycleOwner = binding.activity as LifecycleOwner;
  }

  override fun onDetachedFromActivityForConfigChanges() {}

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if (call.method == "connect") {
      connect()
    } else if (call.method == "pair") {
      val mac: String = call.arguments as String
      this.mac = mac
      pair(mac)
    } else if (call.method == "isBluetoothEnabled") {
      result.success(isBluetoothEnabled())
    } else if (call.method == "isPaired") {
      val mac: String = call.arguments as String
      result.success(isPaired(mac))
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
    } else if (call.method == "closeOutputStream") {
      closeOutputStream();
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

  private fun connect() {
    val boundedDevices = adapter.bondedDevices.toList()
    val device = boundedDevices.first { it -> it.address == mac }
    val method = device.javaClass.getMethod("createInsecureRfcommSocket", Int::class.java)
    val deviceSocket = method.invoke(device, 1) as BluetoothSocket
    deviceSocket.connect()
    outputStream = deviceSocket.outputStream
  }

  private fun isPaired(mac: String): Boolean {
    if (adapter.bondedDevices.any { it -> it.address == mac }) {
      return true;
    }
    return false;
  }

  private fun pair(mac: String) {
    if (adapter.bondedDevices.any { it -> it.address == mac }) {
      logger.log(Level.INFO, "Already paired.")
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

  private fun writeBytes(bytes: ByteArray) {
    outputStream!!.write(bytes)
    outputStream!!.flush()
  }

  private fun closeOutputStream() {
    outputStream!!.close()
  }
}
