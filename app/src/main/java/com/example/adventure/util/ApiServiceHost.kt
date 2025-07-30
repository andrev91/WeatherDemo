package com.example.adventure.util

enum class ApiServiceHost(
    val baseUrl : String,
    val apiKeyConfigName : String,
    var isActive: Boolean = false) {
    ACCUWEATHER("https://dataservice.accuweather.com/",
        "ACCUWEATHER_API_KEY"),
    OPEN_WEATHER_MAP("https://api.openweathermap.org/data/2.5/weather?",
        "OPEN_WEATHER_API_KEY");


    companion object {
        fun setActive(host: ApiServiceHost) {
            entries.forEach { it.isActive = false }
            host.isActive = true
        }
        fun getActive() : ApiServiceHost {
            return entries.first { it.isActive }
        }
    }
}