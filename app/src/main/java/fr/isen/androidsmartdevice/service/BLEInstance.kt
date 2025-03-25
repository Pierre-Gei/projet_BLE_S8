package fr.isen.androidsmartdevice.service

object BLEInstance {
    val instance: BLEService by lazy {
        BLEService()
    }
}