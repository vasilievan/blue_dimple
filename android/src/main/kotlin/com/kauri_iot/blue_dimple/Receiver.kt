package com.kauri_iot.blue_dimple

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.thread
import java.io.OutputStream

class Receiver : BroadcastReceiver() {
    private val logger: Logger = Logger.getLogger(Receiver::javaClass.name)
    private var outputStream : OutputStream? = null
    val device = MutableLiveData<BluetoothDevice>()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
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
        }
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
}