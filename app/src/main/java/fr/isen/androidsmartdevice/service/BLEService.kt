import android.Manifest
    import android.bluetooth.BluetoothAdapter
    import android.bluetooth.BluetoothDevice
    import android.bluetooth.le.ScanCallback
    import android.bluetooth.le.ScanResult
    import android.content.Context
    import android.os.Handler
    import android.util.Log
    import android.widget.Toast
    import androidx.annotation.RequiresPermission

    class BLEService {
        private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        private var scanCallback: ScanCallback? = null
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
            val SCAN_PERIOD: Long = 10000 // 10 seconds scan period

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

            // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                stopScan()
                onScanStopped()
            }, SCAN_PERIOD)
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        fun stopScan() {
            val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
            bluetoothLeScanner?.stopScan(scanCallback)
            scanCallback = null
            isScanning = false
        }
    }