package com.example.adventure.ui.scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.adventure.data.local.model.Bookmark
import com.example.adventure.ui.screen.BookmarksList
import com.example.adventure.ui.state.WeatherUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScaffold(
    uiState: WeatherUiState,
    onRemoveBookmark: (Bookmark) -> Unit,
    onLoadBookmark: (Bookmark) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Orb") },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.Bookmark, contentDescription = "Bookmarks")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (uiState.locationState.bookmarks.isNotEmpty()) {
                            BookmarksList(
                                bookmarks = uiState.locationState.bookmarks,
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
        content = content
    )
}
