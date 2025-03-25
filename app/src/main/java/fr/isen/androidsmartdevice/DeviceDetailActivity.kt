package fr.isen.androidsmartdevice

import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import fr.isen.androidsmartdevice.service.BLEInstance
import fr.isen.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme
import fr.isen.androidsmartdevice.views.DeviceDetailView

class DeviceDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

        val isConnected = mutableStateOf(false)
        val led1State = mutableStateOf(false)
        val led2State = mutableStateOf(false)
        val led3State = mutableStateOf(false)
        val sw1Notification = mutableStateOf(false)
        val sw3Notification = mutableStateOf(false)
        val sw1Value = mutableStateOf("")
        val sw3Value = mutableStateOf("")

        if (device != null) {
            connectToDevice(device, isConnected, sw1Value, sw3Value)
        }

        setContent {
            AndroidSmartDeviceTheme {
                if (BLEInstance.instance.checkPermission(this) && device != null) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                    ) { innerPadding ->
                        DeviceDetailView().DeviceDetailScreen(
                            modifier = Modifier.padding(innerPadding),
                            device = device,
                            isConnected = isConnected,
                            led1State = led1State,
                            led2State = led2State,
                            led3State = led3State,
                            sw1Notification = sw1Notification,
                            sw3Notification = sw3Notification,
                            sw1Value = sw1Value,
                            sw3Value = sw3Value,
                            onLedToggle = { ledIndex: Byte ->
                                BLEInstance.instance.toggleLed(ledIndex)
                                when (ledIndex) {
                                    0x01.toByte() -> {
                                        led1State.value = !led1State.value
                                        led2State.value = false
                                        led3State.value = false
                                    }

                                    0x02.toByte() -> {
                                        led2State.value = !led2State.value
                                        led1State.value = false
                                        led3State.value = false
                                    }

                                    0x03.toByte() -> {
                                        led3State.value = !led3State.value
                                        led1State.value = false
                                        led2State.value = false
                                    }
                                }
                            },
                            onNotificationToggle = { serviceIndex, characteristicIndex, isChecked ->
                                BLEInstance.instance.setCharacteristicNotification(
                                    serviceIndex,
                                    characteristicIndex,
                                    isChecked
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    private fun connectToDevice(
        device: BluetoothDevice,
        isConnected: MutableState<Boolean>,
        sw1Value: MutableState<String>,
        sw3Value: MutableState<String>
    ) {
        if (BLEInstance.instance.checkPermission(this)) {
            BLEInstance.instance.connectToDevice(this, device.address) {
                isConnected.value = true
                BLEInstance.instance.onCharacteristicChangedCallback = { characteristic ->
                    val value = characteristic.value.joinToString()
                    when (characteristic.uuid) {
                        BLEInstance.instance.bluetoothGatt.services[3].characteristics[0].uuid -> {
                            sw1Value.value = value
                        }

                        BLEInstance.instance.bluetoothGatt.services[2].characteristics[1].uuid -> {
                            sw3Value.value = value
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        BLEInstance.instance.disconnectDevice()
    }
}