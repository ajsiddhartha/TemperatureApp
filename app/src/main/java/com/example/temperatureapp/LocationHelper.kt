package com.example.temperatureapp

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Helper class to handle location fetching using Fused Location Provider
class LocationHelper(private val context: Context) {

    // FusedLocationProviderClient is used to access the device's location
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Method to fetch the current location in a suspendable coroutine
    @SuppressLint("MissingPermission")  // Suppressing the warning for missing location permissions
    suspend fun getCurrentLocation(): String? {
        return try {
            // Request the last known location using the fusedLocationClient
            val location = fusedLocationClient.lastLocation.await()

            // If location is not null, return it as a formatted string (latitude, longitude)
            if (location != null) {
                "${location.latitude},${location.longitude}"
            } else {
                null  // Return null if location is not available
            }
        } catch (e: Exception) {
            null  // If an error occurs, return null
        }
    }
}

// Extension function for Task<T> to convert it into a suspending function
// This allows us to await the result of the task without blocking the thread
suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        // Add a completion listener to the task
        addOnCompleteListener { task ->
            // If the task completed with an exception, resume the coroutine with the exception
            if (task.exception != null) {
                continuation.resumeWithException(task.exception!!)
            } else {
                // If the task was successful, resume the coroutine with the result
                continuation.resume(task.result)
            }
        }
    }
}
