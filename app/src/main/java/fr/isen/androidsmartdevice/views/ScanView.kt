package fr.isen.androidsmartdevice.views

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import fr.isen.androidsmartdevice.DeviceDetailActivity
import fr.isen.androidsmartdevice.R
import fr.isen.androidsmartdevice.service.BLEService

class ScanView(private val BLEInstance: BLEService) {
    @Composable
    fun ScanPage(modifier: Modifier) {
        var isScanning by remember { mutableStateOf(BLEInstance.isScanning) }
        var devices by remember { mutableStateOf(setOf<BluetoothDevice>()) }
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
            Image(
                painter = painterResource(id = R.drawable.la_mere_patriev3), // Ensure this is a PNG, JPG, or WEBP file
                contentDescription = "Logo",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "Scan for BLE Devices",
                style = MaterialTheme.typography.titleLarge,
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
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (BLEInstance.bleInitErr(context)) {
                        Toast.makeText(context, "BLE initialization error", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        if (isScanning) {
                            BLEInstance.stopScan()
                            isScanning = false
                        } else {
                            if (checkPermission(Manifest.permission.BLUETOOTH_SCAN, context)) {
                                devices = emptySet()
                                BLEInstance.startScan(
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
                },
            ) {
                Text(
                    text = if (isScanning) "Scanning..." else "Not Scanning",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(devices.toList()) { device ->
                    if (showUnnamedDevices || ((device.name == null).not())) {
                        Text(
                            text = "${device.name ?: "Unnamed Device"} - ${device.address}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .clickable {
                                    val intent =
                                        Intent(context, DeviceDetailActivity::class.java).apply {
                                            putExtra(BluetoothDevice.EXTRA_DEVICE, device)
                                        }
                                    context.startActivity(intent)
                                }
                        )
                    }
                }
            }
            if (isScanning) {
                CircularProgressIndicator()
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