package fr.isen.androidsmartdevice.views

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.isen.androidsmartdevice.DeviceDetailActivity
import fr.isen.androidsmartdevice.R
import fr.isen.androidsmartdevice.service.BLEInstance

@Composable
fun ScanView(
    isScanning: Boolean,
    devices: Set<BluetoothDevice>,
    showUnnamedDevices: Boolean,
    onShowUnnamedDevicesChange: (Boolean) -> Unit,
    onScanButtonClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold() {contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
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
                    onCheckedChange = onShowUnnamedDevicesChange
                )
                Text(
                    text = "Show unnamed devices",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onScanButtonClick,
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
                if(BLEInstance.instance.checkPermission(context)) {
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
            }
            if (isScanning) {
                CircularProgressIndicator()
            }
        }
    }
}