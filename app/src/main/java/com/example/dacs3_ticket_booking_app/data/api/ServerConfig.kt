package com.example.dacs3_ticket_booking_app.data.api

/**
 * Server configuration helper
 * Change BASE_URL_MODE to switch between Emulator and Physical Device
 */
object ServerConfig {
    enum class Mode {
        EMULATOR,      // 10.0.2.2:3000 (Android Emulator)
        PHYSICAL,      // 192.168.x.x:3000 (Physical Device)
        LOCALHOST      // localhost:3000 (for testing)
    }
    
    // ⚙️ Using PHYSICAL mode with laptop IP: 192.168.1.11
    private val BASE_URL_MODE = Mode.PHYSICAL
    
    // 📍 Laptop IP address
    private const val PHYSICAL_DEVICE_IP = "192.168.1.11"
    
    val BASE_URL: String
        get() = when (BASE_URL_MODE) {
            Mode.EMULATOR -> "http://10.0.2.2:3000"
            Mode.PHYSICAL -> "http://$PHYSICAL_DEVICE_IP:3000"
            Mode.LOCALHOST -> "http://localhost:3000"
        }
    
    fun getCurrentMode(): String = BASE_URL_MODE.name
}



