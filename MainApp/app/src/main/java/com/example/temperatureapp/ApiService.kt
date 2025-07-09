package com.example.temperatureapp

import retrofit2.http.GET

// Interface that defines the API endpoints for Retrofit to interact with the server
interface ApiService {

    // Endpoint to get sensor data such as temperature, humidity, etc.
    @GET("/sensor")  // This annotation specifies the HTTP GET request to the "/sensor" endpoint
    suspend fun getSensorData(): SensorData // Returns SensorData object, marked as suspend since it's a network call

    // Endpoint to turn on the buzzer
    @GET("/buzzer/on")  // Sends a GET request to the "/buzzer/on" endpoint
    suspend fun turnBuzzerOn() // Marks the function as suspend to run asynchronously, no return value

    // Endpoint to turn off the buzzer
    @GET("/buzzer/off")  // Sends a GET request to the "/buzzer/off" endpoint
    suspend fun turnBuzzerOff() // Marks the function as suspend to run asynchronously, no return value
}

