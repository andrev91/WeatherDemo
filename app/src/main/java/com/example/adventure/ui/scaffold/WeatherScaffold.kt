package com.example.adventure.ui.scaffold

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.adventure.data.local.model.Bookmark
import com.example.adventure.ui.state.WeatherUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScaffold(
    uiState: WeatherUiState,
    snackBarHostState: SnackbarHostState,
    onRemoveBookmark: (Bookmark) -> Unit,
    onLoadBookmark: (Bookmark) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather App") },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.Bookmark, contentDescription = "Bookmarks")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (uiState.bookmarks.isNotEmpty()) {
                            BookmarksList(
                                bookmarks = uiState.bookmarks,
                                onBookmarkClick = {
                                    onLoadBookmark(it)
                                    expanded = false
                                },
                                onDeleteClick = onRemoveBookmark
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("No bookmarks yet") },
                                onClick = { expanded = false }
                            )
                        }
                    }
                }
            )
        },
        content = content,
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        }
    )
}

@Composable
private fun BookmarksList(
    bookmarks: List<Bookmark>,
    onBookmarkClick: (Bookmark) -> Unit,
    onDeleteClick: (Bookmark) -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Bookmarked Locations", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            bookmarks.forEach { bookmark ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBookmarkClick(bookmark) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${bookmark.cityName}, ${bookmark.stateAbbreviation}")
                    IconButton(onClick = { onDeleteClick(bookmark) }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Delete bookmark")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScaffold() {
    WeatherScaffold(
        uiState = WeatherUiState(),
        onRemoveBookmark = {},
        onLoadBookmark = {},
        content = {},
        snackBarHostState = remember { SnackbarHostState() }
    )
}
