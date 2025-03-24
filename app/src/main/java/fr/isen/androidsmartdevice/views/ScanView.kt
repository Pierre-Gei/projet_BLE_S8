package fr.isen.androidsmartdevice.views

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.isen.androidsmartdevice.R

class ScanView {
    @Composable
    fun ScanPage(modifier: Modifier) {
        var isScanning by remember { mutableStateOf(false) }
        var devices by remember { mutableStateOf(listOf<String>()) }

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
                        isScanning = !isScanning
                        if (isScanning) {
                            // Start scanning logic
                            devices = listOf("Device 1", "Device 2") // Example devices
                        } else {
                            // Stop scanning logic
                            devices = emptyList()
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
}