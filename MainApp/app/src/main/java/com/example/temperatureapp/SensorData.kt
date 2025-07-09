package com.example.temperatureapp

// Data class to represent the sensor data (temperature and humidity) that is returned from the API
data class SensorData(
    // The temperature value in Celsius (Float type to handle decimal points)
    val temperature: Float,

    // The humidity value in percentage (Float type to handle decimal points)
    val humidity: Float
)
