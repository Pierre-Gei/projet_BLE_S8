package fr.isen.androidsmartdevice.views

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.isen.androidsmartdevice.R
import fr.isen.androidsmartdevice.service.BLEInstance

class DeviceDetailView {
    @Composable
    fun DeviceDetailScreen(
        device: BluetoothDevice,
        isConnected: MutableState<Boolean>,
        led1State: MutableState<Boolean>,
        led2State: MutableState<Boolean>,
        led3State: MutableState<Boolean>,
        sw1Notification: MutableState<Boolean>,
        sw3Notification: MutableState<Boolean>,
        sw1Value: MutableState<String>,
        sw3Value: MutableState<String>,
        onLedToggle: (Byte) -> Unit,
        onNotificationToggle: (Int, Int, Boolean) -> Unit,
        modifier: Modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.la_mere_patriev3), // Ensure this is a PNG, JPG, or WEBP file
                contentDescription = "Logo",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 16.dp)
            )
            if (BLEInstance.instance.checkPermission(LocalContext.current)) {
                Text(text = "Device: ${device.name} (${device.address})")
            }
            if (isConnected.value) {
                Text(text = "Connected to device")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column {
                        Image(
                            imageVector = if (led1State.value) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = "LED 1",
                            modifier = Modifier
                                .size(50.dp)
                                .clickable { onLedToggle(0x01) },
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(androidx.compose.ui.graphics.Color.White)
                        )
                        Text(text = "LED 1")
                    }
                    Column {
                        Image(
                            imageVector = if (led2State.value) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = "LED 2",
                            modifier = Modifier
                                .size(50.dp)
                                .clickable { onLedToggle(0x02) },
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(androidx.compose.ui.graphics.Color.White)
                        )
                        Text(text = "LED 2")
                    }
                    Column {
                        Image(
                            imageVector = if (led3State.value) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = "LED 3",
                            modifier = Modifier
                                .size(50.dp)
                                .clickable { onLedToggle(0x03) },
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(androidx.compose.ui.graphics.Color.White)
                        )
                        Text(text = "LED 3")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notifications on switch 1")
                    Checkbox(
                        checked = sw1Notification.value,
                        onCheckedChange = { isChecked ->
                            sw1Notification.value = isChecked
                            onNotificationToggle(3, 0, isChecked)
                        },
                    )
                }
                if (sw1Notification.value) {
                    Text("Switch 1 value: ${sw1Value.value}")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notifications on switch 3")
                    Checkbox(
                        checked = sw3Notification.value,
                        onCheckedChange = { isChecked ->
                            sw3Notification.value = isChecked
                            onNotificationToggle(2, 1, isChecked)
                        }
                    )
                }
                if (sw3Notification.value) {
                    Text("Switch 3 value: ${sw3Value.value}")
                }
            } else {
                Text(text = "Connecting to BLE device...")
                CircularProgressIndicator()
            }
        }
    }
}