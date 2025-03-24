package fr.isen.androidsmartdevice.service

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.util.Log
import android.widget.Toast

class BLEService {
    fun bleInitErr(context : Context): Boolean {
        Log.e("BLEService", "Checking for BLE initialization errors")
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Bluetooth is not supported on this device
            Log.e("BLEService", "Bluetooth not supported on this device")
            Toast.makeText(context, "Bluetooth not supported on this device", Toast.LENGTH_LONG).show()
            return true
        }
        if (!bluetoothAdapter.isEnabled) {
            // Bluetooth is not enabled
            Log.e("BLEService", "Bluetooth is not enabled")
            Toast.makeText(context, "Bluetooth is not enabled", Toast.LENGTH_LONG).show()
            return true
        }
        Log.e("BLEService", "No BLE initialization errors")
        return false
    }
}