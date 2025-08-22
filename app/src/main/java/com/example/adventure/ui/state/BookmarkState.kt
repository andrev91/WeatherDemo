package com.example.adventure.ui.state

sealed interface BookmarkState {
    val message: String
    data class onSuccess(override val message: String) : BookmarkState
    data class onError(override val message: String) : BookmarkState
    data class onDelete(override val message: String) : BookmarkState
}