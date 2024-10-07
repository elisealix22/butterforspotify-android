package com.elisealix22.butterforspotify.ui

import androidx.annotation.StringRes
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.data.error.ServiceError

sealed interface UiErrorMessage {
    data class Message(val text: String) : UiErrorMessage
    data class MessageResId(@StringRes val textResId: Int) : UiErrorMessage
}

fun Throwable.toUiErrorMessage(): UiErrorMessage =
    when (this) {
        is ServiceError.IOError -> UiErrorMessage.MessageResId(R.string.ui_state_network_error)
        is ServiceError.ApiError -> this.userFriendlyMessage.let {
            if (it.isNullOrBlank()) {
                UiErrorMessage.MessageResId(R.string.ui_state_error)
            } else {
                UiErrorMessage.Message(it)
            }
        }
        else -> UiErrorMessage.MessageResId(R.string.ui_state_error)
    }
