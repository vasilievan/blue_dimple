package com.kauri_iot.blue_dimple

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import java.util.logging.Level
import java.util.logging.Logger

class Receiver : BroadcastReceiver() {
    private val logger: Logger = Logger.getLogger(Receiver::javaClass.name)
    val device = MutableLiveData<BluetoothDevice>()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
            val state = intent?.extras?.get(BluetoothDevice.EXTRA_BOND_STATE) as Int
            if (state == 12) {
                logger.log(Level.INFO, "Paired.")
            }
        } else if (intent?.action.equals(BluetoothDevice.ACTION_FOUND)) {
            val device = intent?.extras?.get(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
            this.device.value = device
            logger.log(Level.INFO, "Device found: ${device.name}")
        }
    }
}