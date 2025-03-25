package fr.isen.androidsmartdevice.service

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission

class BLEService {
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var bluetoothGatt: BluetoothGatt
    private var scanCallback: ScanCallback? = null
    var onCharacteristicChangedCallback: ((BluetoothGattCharacteristic) -> Unit)? = null
    private val handler = Handler()
    var isScanning = false
        private set

    fun bleInitErr(context: Context): Boolean {
        if (bluetoothAdapter == null) {
            Log.e("BLEService", "Bluetooth not supported on this device")
            Toast.makeText(context, "Bluetooth not supported on this device", Toast.LENGTH_LONG).show()
            return true
        }
        if (!bluetoothAdapter!!.isEnabled) {
            Log.e("BLEService", "Bluetooth is not enabled")
            Toast.makeText(context, "Bluetooth is not enabled", Toast.LENGTH_LONG).show()
            return true
        }
        return false
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan(onDeviceFound: (BluetoothDevice) -> Unit, onScanStopped: () -> Unit) {
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        val SCAN_PERIOD: Long = 20000 // 20 seconds scan period

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                onDeviceFound(result.device)
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.e("BLEService", "Scan failed with error code: $errorCode")
            }
        }

        bluetoothLeScanner?.startScan(scanCallback)
        isScanning = true

        handler.postDelayed({
            stopScan()
            onScanStopped()
        }, SCAN_PERIOD)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        scanCallback = null
        isScanning = false
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(context: Context, deviceAddress: String?, onConnected: () -> Unit) {
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        bluetoothGatt = device?.connectGatt(context, false, gattCallback)!!
        onConnected()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("BLEService", "Connected to GATT server.")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("BLEService", "Disconnected from GATT server.")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("BLEService", "Services discovered.")
                gatt.services.forEach { service ->
                    Log.i("BLEService", "Service: ${service.uuid}")
                    service.characteristics.forEach { characteristic ->
                        Log.i("BLEService", "Characteristic: ${characteristic.uuid}")
                    }
                }
            } else {
                Log.w("BLEService", "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("BLEService", "Characteristic written successfully.")
            } else {
                Log.w("BLEService", "Characteristic write failed: $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val value = characteristic.value
            Log.i("BLEService", "Characteristic changed: ${value.joinToString()}")
            onCharacteristicChangedCallback?.invoke(characteristic)
        }
    }

    private val ledStates = mutableMapOf<Byte, Boolean>()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun toggleLed(led: Byte) {
        val characteristic = bluetoothGatt.services[2].characteristics[0]
        val currentState = ledStates[led] ?: false
        val newState = !currentState
        ledStates[led] = newState
        writeCharacteristic(characteristic, if (newState) led else 0x00)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setCharacteristicNotification(serviceIndex: Int, characteristicIndex: Int, enable: Boolean) {
        val characteristic = bluetoothGatt.services[serviceIndex].characteristics[characteristicIndex]
        bluetoothGatt.setCharacteristicNotification(characteristic, enable)
        val descriptor = characteristic.descriptors[0]
        val value = if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        descriptor.value = value
        bluetoothGatt.writeDescriptor(descriptor)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, value: Byte) {
        characteristic.value = byteArrayOf(value)
        bluetoothGatt.writeCharacteristic(characteristic)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnectDevice() {
        bluetoothGatt.disconnect()
        bluetoothGatt.close()
        Log.d("BLEService", "Disconnected from GATT server.")
    }

    fun checkPermission(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }
}