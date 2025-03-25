package fr.isen.androidsmartdevice.views

import android.bluetooth.BluetoothDevice
import android.content.Intent
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
    modifier: Modifier,
    isScanning: Boolean,
    devices: Set<BluetoothDevice>,
    showUnnamedDevices: Boolean,
    onShowUnnamedDevicesChange: (Boolean) -> Unit,
    onScanButtonClick: () -> Unit
) {
    val context = LocalContext.current

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
                onCheckedChange = onShowUnnamedDevicesChange
            )
            Text(
                text = "Show unnamed devices",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            onClick = onScanButtonClick,
        ) {
            Text(
                text = if (isScanning) "Scanning..." else "Not Scanning",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (BLEInstance.instance.checkPermission(context)) {
                items(devices.toList()) { device ->
                    if (showUnnamedDevices || ((device.name == null).not())) {
                        Text(
                            text = "${device.name ?: "Unnamed Device"} - ${device.address}",
                            style = MaterialTheme.typography.bodyLarge,
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