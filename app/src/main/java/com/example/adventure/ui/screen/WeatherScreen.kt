package com.example.adventure.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.adventure.ui.theme.AdventureTheme
import com.example.adventure.viewmodel.LocationDisplayData
import com.example.adventure.viewmodel.LocationOption
import com.example.adventure.viewmodel.MainViewModel
import com.example.adventure.viewmodel.WeatherDisplayData
import com.example.adventure.viewmodel.WeatherUiState

const val TAG_LOCATION_DROPDOWN = "LocationDropdown"
const val TAG_WEATHER_DESC = "WeatherDescriptionText"
const val TAG_WEATHER_TEMP = "WeatherTemperatureText"
const val TAG_ERROR_TEXT = "ErrorText"
const val TAG_PROGRESS = "ProgressIndicator"
const val TAG_LOCATION_DESC = "LocationDescriptionText"
const val TAG_REFRESH_BUTTON = "RefreshButton"

@Composable
fun WeatherScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WeatherScreenContent(uiState = uiState,
        onLocationSelected = { selectedLocation -> viewModel.setSelectedLocation(selectedLocation!!) },
        onRefreshClicked = {
            uiState.selectedLocation?.let {
                viewModel.searchLocation()
            } ?: viewModel.fetchWeatherAndLocation() }
    )
}

@Composable
fun WeatherScreenContent(uiState: WeatherUiState,
                         onLocationSelected: (LocationOption?) -> Unit,
                         onRefreshClicked: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text("Accuweather Data", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        DropDownLocations(uiState = uiState, onLocationSelected)
        Spacer(Modifier.height(24.dp))

        if (uiState.isLoadingLocationData && uiState.isLoadingWeatherData && uiState.error == null) {
            CircularProgressIndicator(modifier = Modifier.testTag(TAG_PROGRESS))
            Text(text = "Loading...", modifier = Modifier.padding(8.dp))
        }
        else if (uiState.weatherDisplayData == null
            && uiState.error == null && uiState.locationDisplayData == null) {
            Text(text = "Weather/Location Data", modifier = Modifier.padding(8.dp))
        } else if (uiState.locationDisplayData != null) {
            LocationDetails(data = uiState.locationDisplayData)
            Spacer(modifier = Modifier.height(8.dp))
        } else if (uiState.error != null) {
            Text(text = uiState.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag(TAG_ERROR_TEXT))
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (uiState.weatherDisplayData != null) {
            WeatherDetails(data = uiState.weatherDisplayData)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(onClick = onRefreshClicked, modifier = Modifier.testTag(TAG_REFRESH_BUTTON)) {
            Text(text = "Fetch Weather Data")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownLocations(uiState: WeatherUiState, onLocationSelected: (LocationOption?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOptionObject = uiState.availableLocations?.find { it == uiState.selectedLocation }
    Box(modifier = Modifier.fillMaxWidth(0.8f).testTag(TAG_LOCATION_DROPDOWN)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (uiState.availableLocations!!.isNotEmpty() && !uiState.isLoadingLocationList) {
                    expanded = !expanded
                }
            }
        ) {
            OutlinedTextField(
                value = selectedOptionObject?.value ?: "Select Location",
                onValueChange = {},
                readOnly = true,
                label = { Text("Location") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable, expanded)
                    .fillMaxWidth(),
                enabled = !uiState.availableLocations.isNullOrEmpty() && !uiState.isLoadingLocationList
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (uiState.isLoadingLocationList) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (uiState.availableLocations!!.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No Locations Found") },
                        onClick = { expanded = false },
                        enabled = false
                    )
                } else {
                    uiState.availableLocations.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.value) },
                            onClick = {
                                onLocationSelected(option)
                                expanded = false
                            })
                    }
                }
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
        WeatherScreenContent(uiState = WeatherUiState(isLoadingWeatherData = true,
            isLoadingLocationData = true, isLoadingLocationList = true),
            onLocationSelected = { LocationOption("2177453","Arkansas") },
            onRefreshClicked = {})
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
            onLocationSelected = { LocationOption("349727","New York") },
            onRefreshClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWeatherScreenContent_Error() {
    AdventureTheme {
        WeatherScreenContent(
            uiState = WeatherUiState(isLoadingWeatherData = false, error = "Network Error"),
            onLocationSelected = { LocationOption("348308","Chicago") },
            onRefreshClicked = {}
        )
    }
}