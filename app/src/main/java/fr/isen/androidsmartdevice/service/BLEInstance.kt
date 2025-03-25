import fr.isen.androidsmartdevice.service.BLEService

object BLEInstance {
    val instance: BLEService by lazy {
        BLEService()
    }
}