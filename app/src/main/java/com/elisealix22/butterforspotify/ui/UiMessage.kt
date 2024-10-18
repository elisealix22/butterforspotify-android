package com.elisealix22.butterforspotify.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiMessage {
    data class Message(val text: String) : UiMessage
    data class MessageResId(@StringRes val textResId: Int) : UiMessage
}

@Composable
fun UiMessage.text(): String =
    when (this) {
        is UiMessage.Message -> this.text
        is UiMessage.MessageResId -> stringResource(this.textResId)
    }
