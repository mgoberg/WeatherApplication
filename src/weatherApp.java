import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

// Retrieve data from API - fetching latest weather
// external API.
public class weatherApp {
    // Fetch weather data for given location
    public static JSONObject getWeatherData(String locationName){
        // get location coordinates using geolocation API
        JSONArray locationData = getLocationData(locationName);

        // extracting latitude and longitude data
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // Build API request URL for location coordinates.
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude="+latitude+"&longitude="+longitude+"&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Europe%2FLondon";
        try{
            // Call api and get response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // Check for response status
            // 200 - success
            if(conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API GEO LOCATION");
                return null;

            }

            // Store resulting json data
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNext()){
                // Read and store into the string builder
                resultJson.append(scanner.nextLine());
            }

            // Closing the scanner
            scanner.close();

            // Closing the URL connection
            conn.disconnect();

            // parse through the data
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // retrive hourly data
            JSONObject hourly = (JSONObject)  resultJsonObj.get("hourly");

            // Current hour's data
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            // Get temperature
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            // Get weather data
            JSONArray weathercode = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            // get humidity data
            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (long) relativeHumidity.get(index);

            // Get the windspeed
            JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
            double windspeed = (double) windspeedData.get(index);

            // Building the weather json data object
            // that is accessing the frontend
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            return weatherData;

        }catch(Exception e){
            e.printStackTrace();
        }


        return null;
    }

    public static JSONArray getLocationData(String locationName){
        // Replaces any whitespace in location name to + to adhere to API request format.
        locationName = locationName.replaceAll(" ", "+");

        // Building API url with location parameter
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + locationName + "&count=10&language=en&format=json";

        try{
            // call api and get a response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // Checking response status
            // 200 means succesful connection
            if(conn.getResponseCode() != 200){
                System.out.println("Error: Could not connect to API");
                return null;
            }else{
                // Store the API results
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                // Read and store the resulting json data into the string builder
                while(scanner.hasNext()){
                    resultJson.append(scanner.nextLine());

                }

                // Close off scanner
                scanner.close();

                // Close off url connection
                conn.disconnect();

                // Parse the JSON string into a JSON obj
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                // get the list of location data at the API generated from the location
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;

            }

        }catch(Exception e){
            e.printStackTrace();
        }
        // Could not find location.
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString){
        try{
            // attempt to create a connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // set request method //GET
            conn.setRequestMethod("GET");

            // Connect to API
            conn.connect();
            return conn;
        }catch(IOException e){
            e.printStackTrace();
        }
        // Not able to make connection
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList){
        String currentTime = getCurrentTime();

        // Looping to find the line that matches our time the best
        for(int i = 0; i < timeList.size(); i++){
            String time = (String) timeList.get(i);
            if(time.equalsIgnoreCase(currentTime)) {
                // return the index
                return i;
            }
        }
        return 0;
    }

    public static String getCurrentTime(){
        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Formatting date to be todays date.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-MM-dd'T'HH':00'");

        // Format and print current date and time
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;

    }
    // Converting the weathercode more readable
    private static String convertWeatherCode(long weathercode){
        String weatherCondition = "";
        if(weathercode == 0L){
            weatherCondition = "Clear";
        }else if(weathercode <= 3L && weathercode > 0L) {
            weatherCondition = "Cloudy";
        }else if((weathercode >= 51L && weathercode <= 67L)
                || (weathercode >= 80L && weathercode < 99L)){
            weatherCondition = "Rain";
        }else if(weathercode >= 71L && weathercode <=71L){
            weatherCondition = "Snow";
        }

        return weatherCondition;
    }
}
