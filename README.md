# AndroidWeatherApp
A functional Android weather app using Dark Sky API.

It displays: current weather conditions, hour-by-hour temperature for the next 5 hours, average temperature for the next 48 hours, predicted high and low temperatures for the next week, observed temperature at the present location at any specified time in the past.

## Requirements
Targets Android SDK 27 through 29.

## Function
Calls asynchronous GET requests to Dark Sky API, uses JSON parsing, gets user's location coordinates through Google Play Services, and provides a UI to display the weather information.
