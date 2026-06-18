package com.example.dacs3_ticket_booking_app.data.api

/**
 * Server configuration helper
 * Change BASE_URL_MODE to switch between Emulator and Physical Device
 */
object ServerConfig {

    enum class Mode {
        EMULATOR,      // Android Emulator
        PHYSICAL,      // Real Android device
        LOCALHOST      // localhost testing
    }

    // Current mode
    private val BASE_URL_MODE = Mode.PHYSICAL

    // Laptop IP
    private const val PHYSICAL_DEVICE_IP = "172.26.34.182"

    val BASE_URL: String
        get() = when (BASE_URL_MODE) {

            // Android Emulator
            Mode.EMULATOR ->
                "http://10.0.2.2:3000/"

            // Physical phone
            Mode.PHYSICAL ->
                "http://$PHYSICAL_DEVICE_IP:3000/"

            // Localhost
            Mode.LOCALHOST ->
                "http://localhost:3000/"
        }

    fun getCurrentMode(): String = BASE_URL_MODE.name
}