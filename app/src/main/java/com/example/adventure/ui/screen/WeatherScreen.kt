package com.example.adventure.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.adventure.ui.theme.AdventureTheme
import com.example.adventure.viewmodel.LocationDisplayData
import com.example.adventure.viewmodel.MainViewModel
import com.example.adventure.viewmodel.WeatherDisplayData
import com.example.adventure.viewmodel.WeatherUiState

const val TAG_WEATHER_DESC = "WeatherDescriptionText"
const val TAG_WEATHER_TEMP = "WeatherTemperatureText"
const val TAG_ERROR_TEXT = "ErrorText"
const val TAG_PROGRESS = "ProgressIndicator"
const val TAG_LOCATION_DESC = "LocationDescriptionText"
const val TAG_REFRESH_BUTTON = "RefreshButton"

@Composable
fun WeatherScreen(viewModel: MainViewModel) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
   // val random by remember { mutableStateOf("Hello") }

    WeatherScreenContent(uiState = uiState, onRefresh = { viewModel.fetchData() })

}

@Composable
fun dropDownLocations() {
    val f = 0;
}

@Composable
fun WeatherScreenContent(uiState: WeatherUiState, onRefresh: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text("Accuweather Data", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoadingLocationData && uiState.weatherDisplayData == null && uiState.error == null) {
            CircularProgressIndicator(modifier = Modifier.testTag(TAG_PROGRESS))
            Text(text = "Loading...", modifier = Modifier.padding(8.dp))
        }
        else if (uiState.isLoadingLocationData && uiState.isLoadingWeatherData && uiState.weatherDisplayData == null
            && uiState.error == null) {
            CircularProgressIndicator(modifier = Modifier.testTag(TAG_PROGRESS))
            Text(text = "Loading...", modifier = Modifier.padding(8.dp))
        } else if (uiState.locationDisplayData != null) {
            LocationDetails(data = uiState.locationDisplayData)
            Spacer(modifier = Modifier.height(8.dp))
        } else if (uiState.error != null) {
            Text(text = uiState.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag(TAG_ERROR_TEXT))
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRefresh, modifier = Modifier.testTag(TAG_REFRESH_BUTTON)) {
                Text(text = "Retry")
            }
        }
        if (uiState.weatherDisplayData != null) {
            WeatherDetails(data = uiState.weatherDisplayData)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRefresh, modifier = Modifier.testTag(TAG_REFRESH_BUTTON)) {
                Text(text = "Refresh")
            }
        } else {
            Button(onClick = onRefresh, modifier = Modifier.testTag(TAG_REFRESH_BUTTON)) {
                Text(text = "Fetch Weather and Location")
            }
        }
    }
}

@Composable
fun WeatherDetails(data: WeatherDisplayData) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = data.weatherDescription,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag(TAG_WEATHER_DESC)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Temperature: ${data.temperature}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.testTag(TAG_WEATHER_TEMP)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Observed at: ${data.observedAt}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun LocationDetails(data: LocationDisplayData) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = data.locationName,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Country: ${data.country}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.testTag(TAG_LOCATION_DESC)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Admin Area: ${data.adminArea}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWeatherScreenContent_Loading() {
    AdventureTheme {
        WeatherScreenContent(uiState = WeatherUiState(isLoadingWeatherData = true), onRefresh = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWeatherScreenContent_Success() {
    AdventureTheme {
        WeatherScreenContent(
            uiState = WeatherUiState(
                isLoadingWeatherData = true,
                weatherDisplayData = WeatherDisplayData("Sunny", "25°C", "14:30")
            ),
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWeatherScreenContent_Error() {
    AdventureTheme {
        WeatherScreenContent(
            uiState = WeatherUiState(isLoadingWeatherData = false, error = "Network Error"),
            onRefresh = {}
        )
    }
}