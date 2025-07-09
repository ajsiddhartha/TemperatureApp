#include <WiFi.h>
#include <DHT.h>

#define DHT_PIN 24
#define DHT_TYPE DHT22
#define BUZZER_PIN 39

char ssid[]     = "BharathsIphone";
char password[] = "12345678";

WiFiServer server(80);
DHT dht(DHT_PIN, DHT_TYPE);

// Global variables for sensor readings
float currentTemperature = 0.0;
float currentHumidity = 0.0;
unsigned long lastSensorRead = 0;
const unsigned long SENSOR_INTERVAL = 2000; // Read sensor every 2 seconds
bool sensorReadingValid = false;

bool buzzerOn = false;

void setup() {
  Serial.begin(9600);
  dht.begin();
  
  pinMode(BUZZER_PIN, OUTPUT);
  digitalWrite(BUZZER_PIN, LOW);  // buzzer off initially

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  Serial.println();
  Serial.print("WiFi connected. IP address: ");
  Serial.println(WiFi.localIP());
  server.begin();
  
  // Initial sensor reading
  delay(2000); // Wait for sensor to stabilize
  readSensor();
}

void readSensor() {
  float temp = dht.readTemperature();
  float humid = dht.readHumidity();
  
  if (!isnan(temp) && !isnan(humid)) {
    currentTemperature = temp;
    currentHumidity = humid;
    sensorReadingValid = true;
    
    Serial.print("Sensor Reading - Temp: ");
    Serial.print(currentTemperature);
    Serial.print(" °C, Humidity: ");
    Serial.print(currentHumidity);
    Serial.println(" %");
  } else {
    sensorReadingValid = false;
    Serial.println("Failed to read from DHT sensor!");
  }
}

void loop() {
  unsigned long currentTime = millis();

  // Read sensor at regular intervals
  if (currentTime - lastSensorRead >= SENSOR_INTERVAL) {
    readSensor();
    lastSensorRead = currentTime;
  }

  WiFiClient client = server.available();
  if (client) {
    String req = client.readStringUntil('\r');
    client.read(); // consume newline
    Serial.println("Request: " + req);

    if (req.indexOf("GET /sensor") != -1) {
      // Send sensor JSON - use the same readings that were printed to serial
      Serial.print("Sending to client - Temp: ");
      Serial.print(currentTemperature);
      Serial.print(" °C, Humidity: ");
      Serial.print(currentHumidity);
      Serial.println(" %");
      
      if (!sensorReadingValid) {
        client.println("HTTP/1.1 500 Internal Server Error");
        client.println("Content-Type: application/json");
        client.println("Connection: close");
        client.println();
        client.println("{\"error\":\"Sensor read failed\"}");
      } else {
        String json = "{\"temperature\":";
        json += String(currentTemperature, 2);
        json += ",\"humidity\":";
        json += String(currentHumidity, 2);
        json += ",\"buzzer\":";
        json += buzzerOn ? "true" : "false";
        json += "}";

        client.println("HTTP/1.1 200 OK");
        client.println("Content-Type: application/json");
        client.println("Access-Control-Allow-Origin: *");
        client.println("Connection: close");
        client.println();
        client.println(json);
      }

    } else if (req.indexOf("GET /buzzer/on") != -1) {
      buzzerOn = true;
      digitalWrite(BUZZER_PIN, HIGH);
      Serial.println("Buzzer turned ON");
      
      client.println("HTTP/1.1 200 OK");
      client.println("Content-Type: application/json");
      client.println("Access-Control-Allow-Origin: *");
      client.println("Connection: close");
      client.println();
      client.println("{\"status\":\"Buzzer turned ON\",\"buzzer\":true}");

    } else if (req.indexOf("GET /buzzer/off") != -1) {
      buzzerOn = false;
      digitalWrite(BUZZER_PIN, LOW);
      Serial.println("Buzzer turned OFF");
      
      client.println("HTTP/1.1 200 OK");
      client.println("Content-Type: application/json");
      client.println("Access-Control-Allow-Origin: *");
      client.println("Connection: close");
      client.println();
      client.println("{\"status\":\"Buzzer turned OFF\",\"buzzer\":false}");

    } else if (req.indexOf("GET /") != -1) {
      // Enhanced web page with auto-refresh
      client.println("HTTP/1.1 200 OK");
      client.println("Content-Type: text/html");
      client.println("Connection: close");
      client.println();
      client.println("<!DOCTYPE html>");
      client.println("<html><head>");
      client.println("<title>DHT22 Sensor Control</title>");
      client.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
      client.println("<style>");
      client.println("body { font-family: Arial, sans-serif; margin: 20px; }");
      client.println("h1 { color: #333; }");
      client.println(".reading { font-size: 1.2em; margin: 10px 0; }");
      client.println(".button { padding: 10px 20px; margin: 10px; text-decoration: none; background: #007bff; color: white; border-radius: 5px; }");
      client.println(".button:hover { background: #0056b3; }");
      client.println(".status { font-weight: bold; }");
      client.println("</style>");
      client.println("</head>");
      client.println("<body>");
      client.println("<h1>DHT22 Sensor & Buzzer Control</h1>");
      
      if (sensorReadingValid) {
        client.println("<div class='reading'>🌡️ Temperature: " + String(currentTemperature, 2) + " °C</div>");
        client.println("<div class='reading'>💧 Humidity: " + String(currentHumidity, 2) + " %</div>");
      } else {
        client.println("<div class='reading'>❌ Sensor reading failed</div>");
      }
      
      client.println("<div class='reading status'>🔊 Buzzer: " + String(buzzerOn ? "ON" : "OFF") + "</div>");
      client.println("<hr>");
      client.println("<a href='/buzzer/on' class='button'>Turn Buzzer ON</a>");
      client.println("<a href='/buzzer/off' class='button'>Turn Buzzer OFF</a>");
      client.println("<br><br>");
      client.println("<a href='/sensor' class='button'>Get JSON Data</a>");
      client.println("<br><br>");
      client.println("<button onclick='location.reload()' class='button'>Refresh</button>");
      client.println("</body></html>");
    } else {
      client.println("HTTP/1.1 404 Not Found");
      client.println("Connection: close");
      client.println();
    }
    
    client.stop();
    Serial.println("Client disconnected");
  }

  delay(50); // Reduced delay for better responsiveness
}
