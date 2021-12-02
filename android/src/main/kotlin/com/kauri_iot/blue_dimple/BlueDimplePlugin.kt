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
  private lateinit var activity: Activity

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
    this.activity = binding.activtity;
    lifecycleOwner = binding.activity as LifecycleOwner;
  }

  override fun onDetachedFromActivityForConfigChanges() {}

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "isBluetoothEnabled") {
      result.success(isBluetoothEnabled())
    } else if (call.method == "sendDataToDevice") {
      val mac: String = call.arguments[0] as String
      val ip: String = call.arguments[1] as String
      val password: String = call.arguments[2] as String
      result.success(sendDataToDevice(mac, ip, password))
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
      this.activity.sendBroadcast(intent);
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
}
