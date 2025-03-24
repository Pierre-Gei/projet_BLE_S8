package fr.isen.androidsmartdevice.views

import BLEService
import android.bluetooth.BluetoothDevice
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.isen.androidsmartdevice.R
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class ScanView(private val BLEInstance: BLEService) {
   @Composable
    fun ScanPage(modifier: Modifier) {
        var isScanning by remember { mutableStateOf(false) }
        var devices by remember { mutableStateOf(listOf<String>()) }
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
            Image(
                painter = rememberVectorPainter(image = if (isScanning) Icons.Rounded.Close else Icons.Rounded.PlayArrow),
                contentDescription = if (isScanning) "Stop Scan" else "Start Scan",
                modifier = Modifier
                    .size(100.dp)
                    .clickable {
                        if (BLEInstance.bleInitErr(context)) {
                            Toast.makeText(context, "BLE initialization error", Toast.LENGTH_SHORT).show()
                        } else {
                            isScanning = !isScanning
                            if (isScanning) {
                                if (checkPermission(Manifest.permission.BLUETOOTH_SCAN, context)) {
                                    BLEInstance.startScan { device: BluetoothDevice ->
                                        devices = devices + ((device.name ?: "Unknown Device") + " - " + device.address)
                                    }
                                }
                            } else {
                                if (checkPermission(Manifest.permission.BLUETOOTH_SCAN, context)) {
                                    BLEInstance.stopScan()
                                }
                            }
                        }
                    }
                    .padding(bottom = 16.dp)
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
                items(devices) { device ->
                    Text(
                        text = device,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }

    private fun checkPermission(permission: String, context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}