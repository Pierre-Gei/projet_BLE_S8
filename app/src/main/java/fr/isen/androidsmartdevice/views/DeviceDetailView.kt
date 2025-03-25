package fr.isen.androidsmartdevice.views

import BLEInstance
import android.bluetooth.BluetoothDevice
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.isen.androidsmartdevice.R

class DeviceDetailView {
    @Composable
    fun DeviceDetailScreen(
        device: BluetoothDevice,
        modifier: Modifier
    ) {
        val isConnected = remember { mutableStateOf(false) }
        val led1State = remember { mutableStateOf(false) }
        val led2State = remember { mutableStateOf(false) }
        val led3State = remember { mutableStateOf(false) }
        val SW1Notification = remember { mutableStateOf(false) }
        val SW3Notification = remember { mutableStateOf(false) }
        val SW1Value = remember { mutableStateOf("") }
        val SW3Value = remember { mutableStateOf("") }
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            Log.d("BLE", "LaunchedEffect called")
            if (BLEInstance.instance.checkPermission(context)) {
                BLEInstance.instance.connectToDevice(context, device.address) {
                    isConnected.value = true
                    Log.d("BLE", "Connected to device")
                    BLEInstance.instance.onCharacteristicChangedCallback = { characteristic ->
                        val value = characteristic.value
                        Log.d("BLE", "Characteristic changed: ${value.joinToString()}")
                        Log.d("BLE", "Characteristic UUID: ${characteristic.uuid}")
                        when (characteristic.uuid) {
                            BLEInstance.instance.bluetoothGatt.services[3].characteristics[0].uuid -> {
                                SW1Value.value = value.joinToString()
                            }

                            BLEInstance.instance.bluetoothGatt.services[2].characteristics[1].uuid -> {
                                SW3Value.value = value.joinToString()
                            }
                        }
                    }
                }
            }
        }

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
            Text(text = "Device: ${device.name} (${device.address})")
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
                                .clickable {
                                    BLEInstance.instance.toggleLed(0x01)
                                    led1State.value = !led1State.value
                                    led2State.value = false
                                    led3State.value = false
                                },
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
                                .clickable {
                                    BLEInstance.instance.toggleLed(0x02)
                                    led2State.value = !led2State.value
                                    led1State.value = false
                                    led3State.value = false
                                },
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
                                .clickable {
                                    BLEInstance.instance.toggleLed(0x03)
                                    led3State.value = !led3State.value
                                    led1State.value = false
                                    led2State.value = false
                                },
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(androidx.compose.ui.graphics.Color.White)
                        )
                        Text(text = "LED 3")
                    }
                }
                Row {
                    Text("Subscribe to notifications on switch 1")
                    Checkbox(
                        checked = SW1Notification.value,
                        onCheckedChange = { isChecked ->
                            SW1Notification.value = isChecked
                            if (isChecked) {
                                BLEInstance.instance.setCharacteristicNotification(3, 0, true)
                            } else {
                                BLEInstance.instance.setCharacteristicNotification(3, 0, false)
                            }
                        },
                    )
                }
                if (SW1Notification.value) {
                    Text("Switch 1 value: ${SW1Value.value}")
                }
                Row {
                    Text("Subscribe to notifications on switch 3")
                    Checkbox(
                        checked = SW3Notification.value,
                        onCheckedChange = { isChecked ->
                            SW3Notification.value = isChecked
                            if (isChecked) {
                                BLEInstance.instance.setCharacteristicNotification(2, 1, true)
                            } else {
                                BLEInstance.instance.setCharacteristicNotification(2, 1, false)
                            }
                        }
                    )
                }
                if (SW3Notification.value) {
                    Text("Switch 3 value: ${SW3Value.value}")
                }
            } else {
                Text(text = "Connecting to BLE device...")
                CircularProgressIndicator()
            }
        }
    }
}