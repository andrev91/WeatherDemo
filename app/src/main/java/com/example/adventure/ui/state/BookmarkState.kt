package com.example.adventure.ui.state

import com.example.adventure.util.UiText

sealed interface BookmarkState {
    val message: UiText
    data class onSuccess(override val message: UiText) : BookmarkState
    data class onError(override val message: UiText) : BookmarkState
    data class onDelete(override val message: UiText) : BookmarkState
}