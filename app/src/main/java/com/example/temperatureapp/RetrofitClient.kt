package com.example.temperatureapp

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton object to provide a Retrofit instance for network requests
object RetrofitClient {

    // Define the base URL for the API
    private const val BASE_URL = "http://172.20.10.5/"

    // Configure a Gson instance to handle JSON parsing, set lenient parsing to allow more flexible handling of JSON
    private val gson = GsonBuilder()
        .setLenient()  // Allow lenient parsing of JSON (can help with malformed or unexpected JSON formats)
        .create()

    // Lazy initialization of Retrofit's ApiService interface (only created when needed)
    val apiService: ApiService by lazy {
        // Build the Retrofit instance
        Retrofit.Builder()
            .baseUrl(BASE_URL)  // Set the base URL for API calls
            .addConverterFactory(GsonConverterFactory.create(gson))  // Use the customized Gson converter
            .build()  // Build the Retrofit instance
            .create(ApiService::class.java)  // Create the ApiService instance from the Retrofit API interface
    }
}
