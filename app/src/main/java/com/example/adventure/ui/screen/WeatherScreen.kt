package com.example.adventure.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.adventure.R
import com.example.adventure.data.model.State
import com.example.adventure.ui.state.LocationSelectionState
import com.example.adventure.ui.state.LocationType
import com.example.adventure.ui.state.WeatherDataState
import com.example.adventure.ui.state.WeatherUiState
import com.example.adventure.ui.theme.AdventureTheme
import com.example.adventure.viewmodel.MainViewModel
import com.example.adventure.viewmodel.UnitType
import com.example.adventure.viewmodel.WeatherDisplayData

const val TAG_LOCATION_DROPDOWN = "LocationDropdown"

const val TAG_CITY_DROPDOWN = "CityDropdown"
const val TAG_LOCATION_DROPDOWN_OUTLINE = "LocationDropdownOutline"

const val TAG_CITY_DROPDOWN_OUTLINE = "CityDropdownOutline"
const val TAG_WEATHER_DESC = "WeatherDescriptionText"
const val TAG_WEATHER_TEMP = "WeatherTemperatureText"
const val TAG_ERROR_TEXT = "ErrorText"
const val TAG_PROGRESS = "ProgressIndicator"
const val TAG_LOCATION_DESC = "LocationDescriptionText"
const val TAG_REFRESH_BUTTON = "RefreshButton"

@Composable
fun WeatherScreen(viewModel: MainViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WeatherScreenContent(uiState = uiState,
        onDropdownSearch = { locationType, search -> viewModel.searchDropdownList(locationType, search) },
        onDropdownClear = { viewModel.clearDropdownSelection(it) },
        onDropdownSelected = { locationType, location -> viewModel.setDropdownSelection(locationType, location) },
        onRefreshClicked = { viewModel.searchLocation() },
        onUnitSelected = { viewModel.triggerTempTypeChange(it) }
    )
}

@Composable
fun WeatherScreenContent(uiState: WeatherUiState,
                         onDropdownSearch: (LocationType, TextFieldValue) -> Unit,
                         onDropdownClear : (LocationType) -> Unit,
                         onDropdownSelected : (LocationType, String) -> Unit,
                         onRefreshClicked: () -> Unit,
                         onUnitSelected : (UnitType) -> Unit) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text("Accuweather Data", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        SearchableDropDown(
            label = "US State",
            testTag = TAG_LOCATION_DESC,
            options = uiState.locationState.filteredStates.ifEmpty { uiState.locationState.availableStates!! },
            onClear = { onDropdownClear(LocationType.STATE) },
            searchQuery = uiState.locationState.stateSearchQuery,
            onSearchQueryChanged = { onDropdownSearch(LocationType.STATE, it) } ,
            isSelected = uiState.locationState.selectedState != null,
            onOptionSelected = { onDropdownSelected(LocationType.STATE, it) }
        ) { it.name }
        Spacer(Modifier.height(8.dp))
        if (uiState.locationState.selectedState != null && !uiState.locationState.availableCities.isNullOrEmpty()) {
            SearchableDropDown(
                label = "City",
                testTag = TAG_CITY_DROPDOWN,
                options = uiState.locationState.filteredCities.ifEmpty { uiState.locationState.availableCities },
                onClear = { onDropdownClear(LocationType.CITY) } ,
                searchQuery = uiState.locationState.citySearchQuery,
                onSearchQueryChanged = { onDropdownSearch(LocationType.CITY, it) } ,
                isSelected = uiState.locationState.selectedCity != null,
                onOptionSelected = { onDropdownSelected(LocationType.CITY, it) }
            ) { it }
            Spacer(Modifier.height(16.dp))
        }
        if ((uiState.locationState.isLoadingStates || uiState.locationState.isLoadingCities
            || uiState.weatherState.isLoadingWeather) && uiState.error == null) {
            CircularProgressIndicator(modifier = Modifier.testTag(TAG_PROGRESS))
            Text(text = "Loading...", modifier = Modifier.padding(8.dp))
        }
        else if (uiState.weatherState.displayData == null
            && uiState.error == null) {
            Text(text = "Weather/Location Data", modifier = Modifier.padding(8.dp))
        } else if (uiState.error != null) {
            Text(text = uiState.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag(TAG_ERROR_TEXT))
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (uiState.weatherState.displayData != null) {
            WeatherDetails(data = uiState.weatherState.displayData, unit = uiState.weatherState.temperatureUnit)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(onClick = onRefreshClicked, modifier = Modifier.testTag(TAG_REFRESH_BUTTON)
            , elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)) {
            Text(text = if (uiState.weatherState.displayData != null) "Refresh Weather Data" else "Fetch Weather Data")
        }
        Spacer(modifier = Modifier.height(24.dp))
        RadioButtonSelection(selectedUnit = uiState.weatherState.temperatureUnit, onOptionSelected = onUnitSelected)
    }
}

@Composable
fun RadioButtonSelection(selectedUnit : UnitType, onOptionSelected : (UnitType) -> Unit) {
    val radioOptions = UnitType.entries
    Row (Modifier.selectableGroup(),
        horizontalArrangement = Arrangement.Center) {
        radioOptions.forEach { option ->
            Row(modifier = Modifier
                .height(56.dp)
                .padding(16.dp)
                .selectable(
                    selected = (option == selectedUnit),
                    onClick = { onOptionSelected(option) },
                    role = androidx.compose.ui.semantics.Role.RadioButton
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                    RadioButton(
                        modifier = Modifier.testTag(option.toString()) ,
                        selected = (option == selectedUnit ),
                        onClick = null
                    )
                    Text(
                        text = option.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableDropDown(
    label: String,
    testTag: String,
    options: List<T>,
    searchQuery: TextFieldValue,
    onSearchQueryChanged: (TextFieldValue) -> Unit,
    onOptionSelected: (String) -> Unit,
    onClear: () -> Unit,
    isSelected: Boolean = false,
    optionToString: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }
    val focusController = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = Modifier.fillMaxWidth(0.8f)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = it
            }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth()
                    .testTag(testTag),
                value = searchQuery,
                onValueChange = {
                    onSearchQueryChanged(it)
                    expanded = true // Keep the dropdown open while searching
                },
                label = { Text(label) },
                trailingIcon = {
                    if (isSelected) {
                        IconButton(onClick = {
                            onClear()
                            expanded = false
                        }) { Icon(Icons.Filled.Clear, contentDescription = "Clear selection") }
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }},
                singleLine = true
            )
            if (options.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(optionToString(option)) },
                            onClick = {
                                keyboardController?.hide()
                                focusController.clearFocus()
                                expanded = false
                                onOptionSelected(optionToString(option))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherDetails(data: WeatherDisplayData, unit : UnitType = UnitType.CELSIUS) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(
                text = data.weatherDescription,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.testTag(TAG_WEATHER_DESC)
            )
            if (data.weatherIcon != null && data.weatherIcon != 0) {
                Image(
                    painter = painterResource(id = data.weatherIcon),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Temperature: " + if (unit == UnitType.CELSIUS) data.temperatureCelsius
                else data.temperatureFahrenheit,
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
}

@Preview(showBackground = true)
@Composable
fun PreviewWeatherScreenContent_Loading() {
    AdventureTheme {
        WeatherScreenContent(uiState = WeatherUiState(LocationSelectionState(
            isLoadingCities = true, isLoadingStates = true),
            WeatherDataState(isLoadingWeather = true)),
            onDropdownSelected = { _, _ -> },
            onDropdownSearch = { _, _ -> },
            onRefreshClicked = {}, onUnitSelected = {}, onDropdownClear = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewWeatherScreenContent_Success() {
    AdventureTheme(darkTheme = true) {
        WeatherScreenContent(
            uiState = WeatherUiState(
                weatherState = WeatherDataState(
                    isLoadingWeather = false,
                    displayData = WeatherDisplayData("Sunny", "25°C", "77°F",
                        R.mipmap.rainy_white_background,"14:30")
                ),
                locationState = LocationSelectionState(
                    selectedState = State("Georgia", "GA"),
                    selectedCity = "Dunwoody",
                    availableCities = listOf("Dunwoody","Powder Springs, Marietta")
                )
            ),
            onDropdownSelected = { _, _ -> },
            onDropdownSearch = { _, _ -> },
            onRefreshClicked = {},onUnitSelected = {}, onDropdownClear = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWeatherScreenContent_Error() {
    AdventureTheme {
        WeatherScreenContent(
            uiState = WeatherUiState(
                weatherState = WeatherDataState(isLoadingWeather = false),
                locationState = LocationSelectionState(isLoadingStates = false),
                error = "Network Error"),
            onDropdownSelected = { _, _ -> },
            onDropdownSearch = { _, _ -> },
            onRefreshClicked = {}, onUnitSelected = {}, onDropdownClear = {}
        )
    }
}