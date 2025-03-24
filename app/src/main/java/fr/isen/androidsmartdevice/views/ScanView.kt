package fr.isen.androidsmartdevice.views

import BLEService
import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class ScanView(private val BLEInstance: BLEService) {
   @Composable
    fun ScanPage(modifier: Modifier) {
        var isScanning by remember { mutableStateOf(BLEInstance.isScanning) }
        var devices by remember { mutableStateOf(setOf<String>()) }
        var showUnnamedDevices by remember { mutableStateOf(false) }
        val context = LocalContext.current

        DisposableEffect(Unit) {
            onDispose {
                if (isScanning && checkPermission(Manifest.permission.BLUETOOTH_SCAN, context)) {
                    BLEInstance.stopScan()
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "BLE Scan",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Checkbox(
                    checked = showUnnamedDevices,
                    onCheckedChange = { showUnnamedDevices = it }
                )
                Text(
                    text = "Show unnamed devices",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Image(
                painter = rememberVectorPainter(image = if (isScanning) Icons.Rounded.Close else Icons.Rounded.PlayArrow),
                contentDescription = if (isScanning) "Stop Scan" else "Start Scan",
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        if (BLEInstance.bleInitErr(context)) {
                            Toast.makeText(context, "BLE initialization error", Toast.LENGTH_SHORT).show()
                        } else {
                            if (isScanning) {
                                BLEInstance.stopScan()
                                isScanning = false
                            } else {
                                if (checkPermission(Manifest.permission.BLUETOOTH_SCAN, context)) {
                                    devices = emptySet()
                                    BLEInstance.startScan(
                                        onDeviceFound = { device: BluetoothDevice ->
                                            val deviceName = device.name ?: "Unknown Device"
                                            devices = devices + (deviceName + " - " + device.address)
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
                    .padding(bottom = 16.dp),
            )
            Text(
                text = if (isScanning) "Scanning..." else "Not Scanning",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(devices.toList()) { device ->
                    if (showUnnamedDevices || device.contains("Unknown Device").not()) {
                        Text(
                            text = device,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun checkPermission(permission: String, context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}