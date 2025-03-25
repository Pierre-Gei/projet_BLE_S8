package fr.isen.androidsmartdevice

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import fr.isen.androidsmartdevice.service.BLEInstance
import fr.isen.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme
import fr.isen.androidsmartdevice.views.ScanView

class ScanActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 and above
            if (permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                permissions[Manifest.permission.BLUETOOTH_CONNECT] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            ) {
                // Permissions granted, proceed with BLE scan
            } else {
                Toast.makeText(
                    this,
                    "Permissions denied. BLE scan cannot proceed.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            // Android 8 to 11
            if (permissions[Manifest.permission.BLUETOOTH] == true &&
                permissions[Manifest.permission.BLUETOOTH_ADMIN] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            ) {
                // Permissions granted, proceed with BLE scan
            } else {
                Toast.makeText(
                    this,
                    "Permissions denied. BLE scan cannot proceed.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkPermissions()
        setContent {
            AndroidSmartDeviceTheme {
                var isScanning by remember { mutableStateOf(BLEInstance.instance.isScanning) }
                var devices by remember { mutableStateOf(setOf<BluetoothDevice>()) }
                var showUnnamedDevices by remember { mutableStateOf(false) }
                val context = LocalContext.current

                DisposableEffect(Unit) {
                    onDispose {
                        if (isScanning && BLEInstance.instance.checkPermission(context)) {
                            BLEInstance.instance.stopScan()
                        }
                    }
                }



                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                ) { innerPadding ->
                    ScanView(
                        modifier = Modifier.padding(innerPadding),
                        isScanning = isScanning,
                        devices = devices,
                        showUnnamedDevices = showUnnamedDevices,
                        onShowUnnamedDevicesChange = { showUnnamedDevices = it },
                        onScanButtonClick = {
                            if (BLEInstance.instance.bleInitErr(context)) {
                                Toast.makeText(
                                    context,
                                    "BLE initialization error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                if (isScanning) {
                                    BLEInstance.instance.stopScan()
                                    isScanning = false
                                } else {
                                    if (BLEInstance.instance.checkPermission(context)) {
                                        devices = emptySet()
                                        BLEInstance.instance.startScan(
                                            onDeviceFound = { device: BluetoothDevice ->
                                                devices = devices + device
                                            },
                                            onScanStopped = {
                                                isScanning = false
                                            }
                                        )
                                        isScanning = true
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Permission denied. BLE scan cannot proceed.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                    )

                }
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 and above
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permissions already granted, proceed with BLE scan
                }

                else -> {
                    // Request permissions
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }
            }
        } else {
            // Android 8 to 11
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH
                ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_ADMIN
                        ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permissions already granted, proceed with BLE scan
                }

                else -> {
                    // Request permissions
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }
            }
        }
    }
}