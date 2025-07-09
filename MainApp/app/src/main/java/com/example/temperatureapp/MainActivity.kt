package com.example.temperatureapp

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.example.temperatureapp.ui.theme.TemperatureAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Context


// MainActivity that sets up the initial UI for the temperature app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display for immersive experience
        enableEdgeToEdge()

        // Set the content of the app using the TemperatureAppTheme
        setContent {
            TemperatureAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Display the temperature screen with padding from Scaffold
                    TemperatureScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Composable function to display a background video
@Composable
fun BackgroundVideo(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // AndroidView to display a VideoView that plays a background video
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                // Set the layout parameters to fill the screen
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )

                // URI of the background video resource
                val videoUri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.background}")

                // Set up the video view
                setVideoURI(videoUri)
                setOnPreparedListener { mediaPlayer ->
                    // Loop the video and scale it to fit the screen
                    mediaPlayer.isLooping = true
                    mediaPlayer.setVideoScalingMode(android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                    start()
                }

                // Handle errors during video loading
                setOnErrorListener { _, _, _ ->
                    false // Return false to indicate the error was handled
                }
            }
        },
        modifier = modifier
            .fillMaxSize() // Make sure video fills the entire space
            .alpha(0.6f), // Make video semi-transparent for visual effect
        update = { videoView ->
            // Ensure the video view always matches the screen size
            videoView.layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
    )
}

// Composable function to display a weather widget
@Composable
fun WeatherWidget(
    modifier: Modifier = Modifier,
    weatherData: WeatherData
) {
    // A card to contain the weather widget
    Card(
        modifier = modifier
            .fillMaxWidth() // Make the card take the full width
            .padding(horizontal = 8.dp, vertical = 4.dp), // Add padding around the card
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f) // Set a light opaque white background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp), // Set card elevation
        shape = RoundedCornerShape(20.dp), // Round the corners of the card
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color(0xFF87CEEB).copy(alpha = 0.3f) // Set a subtle border color
        )
    ) {
        // Column to arrange elements vertically inside the card
        Column(
            modifier = Modifier
                .fillMaxWidth() // Make column take full width
                .padding(20.dp), // Add padding inside the column
            horizontalAlignment = Alignment.CenterHorizontally // Center items horizontally
        ) {
            // Header with a gradient background for weather section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth() // Ensure the box fills the width
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF87CEEB).copy(alpha = 0.2f),
                                    Color(0xFF4FC3F7).copy(alpha = 0.3f),
                                    Color(0xFF87CEEB).copy(alpha = 0.2f)
                                )
                            )
                        )
                        .padding(12.dp) // Padding around the gradient background
                ) {
                    // Row to align header elements
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween, // Space between elements
                        verticalAlignment = Alignment.CenterVertically // Vertically center the elements
                    ) {
                        // Display weather icon and label
                        Text(text = "🌤️", fontSize = 28.sp)
                        Text(
                            text = "Local Weather",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = Color(0xFF1565C0) // Set a contrasting color
                        )
                        Text(text = "🌡️", fontSize = 28.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Add space between header and weather details

            // Check if weather data is still loading
            if (weatherData.isLoading) {
                // Show loading indicator if data is loading
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF87CEEB).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth() // Make box fill the width
                            .padding(20.dp), // Add padding around the box
                        contentAlignment = Alignment.Center // Center the loading indicator
                    ) {
                        // Circular progress indicator
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Color(0xFF1565C0),
                            strokeWidth = 3.dp
                        )
                    }
                }
            } else if (weatherData.error != null) {
                // Display error message if there was an issue fetching weather data
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE) // Light red background for error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth() // Make box fill the width
                            .padding(16.dp), // Add padding inside the box
                        contentAlignment = Alignment.Center // Center the error text
                    ) {
                        Text(
                            text = "⚠️ Weather unavailable",
                            color = Color(0xFFD32F2F), // Red color for error
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            } else {
                // Display main weather info if no errors
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF3F9FF) // Light blue background
                    ),
                    shape = RoundedCornerShape(16.dp), // Rounded corners
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Set elevation
                ) {
                    // Column for main weather info
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp), // Padding inside the column
                        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
                    ) {
                        // Row to display weather, humidity, and wind information
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly // Space out the cards evenly
                        ) {
                            // Weather (temperature + condition) section
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF1976D2).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "🌤️", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = weatherData.temperature,
                                        fontSize = 17.sp,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF1565C0) // Blue color for temperature
                                    )
                                    Text(
                                        text = weatherData.condition,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF424242), // Gray color for condition
                                        maxLines = 1 // Limit to one line for the condition text
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Humidity section
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF03A9F4).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "💧", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = weatherData.humidity,
                                        fontSize = 17.sp,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF0277BD)
                                    )
                                    Text(
                                        text = "Humidity",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF424242)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Wind speed
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "💨", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = weatherData.windSpeed, fontSize = 17.sp,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF7B1FA2)
                                    )
                                    Text(
                                        text = "Wind",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF424242)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Location with enhanced styling
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "📍", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = weatherData.location,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Suspend function to load weather data asynchronously
private suspend fun loadWeatherData(
    context: Context, // Context for getting the current location
    onResult: (WeatherData) -> Unit // Callback to pass the result (weather data or error)
) {
    try {
        // Initialize a LocationHelper to fetch the current location of the device
        val locationHelper = LocationHelper(context)

        // Get the current location using the LocationHelper
        val location = locationHelper.getCurrentLocation()

        // If location is available, proceed with fetching the weather data
        if (location != null) {
            // Define your API key for the weather API
            val apiKey = "2786d5cabf1147d0be2203907250607"

            // Make an API call to get current weather data using the location and API key
            val response = WeatherRetrofitClient.apiService.getCurrentWeather(
                apiKey = apiKey,  // The API key for authentication
                location = location // Current location fetched earlier
            )

            // On success, transform the response into a WeatherData object
            onResult(
                WeatherData(
                    temperature = "${response.current.temp_c.toInt()}°C", // Current temperature in Celsius
                    condition = response.current.condition.text, // Weather condition (e.g., "Sunny", "Rainy")
                    location = "${response.location.name}, ${response.location.country}", // Location name and country
                    humidity = "${response.current.humidity}%", // Current humidity in percentage
                    feelsLike = "${response.current.feelslike_c.toInt()}°C", // Feels like temperature in Celsius
                    windSpeed = "${response.current.wind_kph.toInt()} km/h", // Wind speed in km/h
                    isLoading = false, // Set loading state to false as data is successfully fetched
                    error = null // No error occurred
                )
            )
        } else {
            // If location is not available, return an error message through the onResult callback
            onResult(
                WeatherData(
                    isLoading = false, // Loading state is false as the operation is complete
                    error = "Unable to get location" // Error message indicating location retrieval failure
                )
            )
        }
    } catch (e: Exception) {
        // In case of an exception (e.g., network failure, API error), handle the error gracefully
        onResult(
            WeatherData(
                isLoading = false, // Loading state is false as the operation failed
                error = "Failed to load weather: ${e.message}" // Return the exception message as the error
            )
        )
    }
}

@Composable
fun TemperatureScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // States to manage sensor data, errors, buzzer status, and weather data
    var temperature by remember { mutableStateOf("Loading...") } // Temperature state
    var humidity by remember { mutableStateOf("Loading...") } // Humidity state
    var error by remember { mutableStateOf<String?>(null) } // Error state
    var isBuzzerOn by remember { mutableStateOf(false) } // Fan (buzzer) state
    var weatherData by remember { mutableStateOf(WeatherData()) } // Weather data state

    // Permission launcher for location permissions
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if either fine or coarse location permissions are granted
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        // If permissions granted, fetch weather data
        if (fineLocationGranted || coarseLocationGranted) {
            CoroutineScope(Dispatchers.IO).launch {
                loadWeatherData(context) { newWeatherData ->
                    weatherData = newWeatherData // Update weather data
                }
            }
        } else {
            // If permissions are not granted, update the state with error message
            weatherData = weatherData.copy(
                isLoading = false,
                error = "Location permission required"
            )
        }
    }

    // Animation for the fan button scaling effect
    val fanButtonScale by animateFloatAsState(
        targetValue = if (isBuzzerOn) 1.1f else 1f, // Scale up if buzzer is on, else normal size
        animationSpec = tween(300) // Animation duration
    )

    // Gradient brush for background based on buzzer state (on or off)
    val backgroundBrush = if (isBuzzerOn) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF6B6B).copy(alpha = 0.1f),
                Color(0xFFFF8E8E).copy(alpha = 0.05f),
                Color.Transparent
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF4FC3F7).copy(alpha = 0.1f),
                Color(0xFF81D4FA).copy(alpha = 0.05f),
                Color.Transparent
            )
        )
    }

    // Animated background color based on buzzer state
    val animatedBackgroundBrush by animateColorAsState(
        targetValue = if (isBuzzerOn) Color(0xFFFF6B6B).copy(alpha = 0.05f) else Color(0xFF4FC3F7).copy(alpha = 0.05f),
        animationSpec = tween(500)
    )

    // LaunchedEffect to execute tasks when the screen is loaded
    LaunchedEffect(Unit) {
        // Fetch sensor data asynchronously on background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getSensorData()
                temperature = "${response.temperature}°C" // Set temperature data
                humidity = "${response.humidity}%" // Set humidity data
            } catch (e: Exception) {
                error = "Failed to load sensor data: ${e.message}" // Set error message in case of failure
            }
        }
        // Launch permission request for location access
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Use Box to layer the UI elements: Background Video, Overlay, and Content
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets(0)) // Remove any window insets like navigation bars
    ) {
        // Background Video component (set below all other UI elements)
        BackgroundVideo(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f) // Ensures the background video stays behind everything else
        )

        // Semi-transparent overlay for better readability of content on top of the video
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
                .background(
                    brush = if (isBuzzerOn) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color(0xFFFF6B6B).copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.3f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color(0xFF4FC3F7).copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.3f)
                            )
                        )
                    }
                )
        )

        // Content layer (UI components like weather data, sensor data, and fan control)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f) // Ensures content is above the video and overlay
                .verticalScroll(rememberScrollState()) // Make content scrollable
                .padding(16.dp), // Padding around the content
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display Weather Widget (current weather data like temperature, condition, etc.)
            WeatherWidget(weatherData = weatherData)

            Spacer(modifier = Modifier.height(12.dp))

            // Enhanced Sensor Data Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title for the sensor data section
                    Text(
                        text = "Car Cabin Conditions",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error display if there's an issue with fetching sensor data
                    if (error != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = error ?: "Unknown error",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        // Temperature Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Car emoji for temperature display
                                Text(
                                    text = "🚗",
                                    fontSize = 24.sp,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Temperature",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = temperature,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        ),
                                        color = Color(0xFFFF9800)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Humidity Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Water drop emoji for humidity
                                Text(
                                    text = "💧",
                                    fontSize = 24.sp,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Humidity",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = humidity,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        ),
                                        color = Color(0xFF2196F3)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Fan Control Button (with animated scaling and color change)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(fanButtonScale),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isBuzzerOn)
                                Color(0xFFFF5252).copy(alpha = 0.1f)
                            else
                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Button(
                            onClick = {
                                // Toggle buzzer (fan) state
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        if (!isBuzzerOn) {
                                            RetrofitClient.apiService.turnBuzzerOn() // Turn on buzzer
                                        } else {
                                            RetrofitClient.apiService.turnBuzzerOff() // Turn off buzzer
                                        }
                                        isBuzzerOn = !isBuzzerOn // Update buzzer state
                                    } catch (e: Exception) {
                                        error = "Failed to toggle buzzer: ${e.message}" // Handle error
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBuzzerOn)
                                    Color(0xFFFF5252)
                                else
                                    Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isBuzzerOn)
                                        Icons.Default.Clear
                                    else
                                        Icons.Default.PlayArrow,
                                    contentDescription = if (isBuzzerOn) "Turn Off" else "Turn On",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBuzzerOn) "FAN OFF" else "FAN ON",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // Status indicator for when the fan is running
                    AnimatedVisibility(
                        visible = isBuzzerOn,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF5252).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Fan Running",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Fan is running",
                                    color = Color(0xFFFF5252),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Preview function to display the UI preview of the TemperatureScreen composable
@Preview(showBackground = true) // This annotation tells the preview to render the composable with a background
@Composable
fun PreviewTemperatureScreen() {
    // Wrapping the TemperatureScreen composable inside a custom theme to apply consistent styling
    TemperatureAppTheme { // This applies the theme settings such as colors, typography, etc.
        TemperatureScreen() // Call the TemperatureScreen composable to render it in the preview
    }
}