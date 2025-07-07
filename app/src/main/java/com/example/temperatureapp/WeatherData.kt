package com.example.temperatureapp

data class WeatherResponse(
    val location: Location,
    val current: Current
)

data class Location(
    val name: String,
    val country: String,
    val localtime: String
)

data class Current(
    val temp_c: Double,
    val condition: Condition,
    val humidity: Int,
    val feelslike_c: Double,
    val wind_kph: Double
)

data class Condition(
    val text: String,
    val icon: String
)

data class WeatherData(
    val temperature: String = "Loading...",
    val condition: String = "Loading...",
    val location: String = "Loading...",
    val humidity: String = "Loading...",
    val feelsLike: String = "Loading...",
    val windSpeed: String = "Loading...",
    val isLoading: Boolean = true,
    val error: String? = null
)