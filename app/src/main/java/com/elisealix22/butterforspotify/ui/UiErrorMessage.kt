package com.elisealix22.butterforspotify.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.data.BuildConfig
import com.elisealix22.butterforspotify.data.error.ServiceError

sealed interface UiErrorMessage {
    data class Message(val text: String) : UiErrorMessage
    data class MessageResId(@StringRes val textResId: Int) : UiErrorMessage
}

internal fun Throwable.toUiErrorMessage(): UiErrorMessage =
    when (this) {
        is ServiceError.IOError -> UiErrorMessage.MessageResId(R.string.ui_state_network_error)
        is ServiceError.ApiError -> this.userFriendlyMessage.let {
            if (it.isNullOrBlank()) {
                UiErrorMessage.MessageResId(R.string.ui_state_error)
            } else {
                UiErrorMessage.Message(it)
            }
        }
        else -> {
            if (BuildConfig.DEBUG) {
                val debugErrorMessage = message.let {
                    if (it.isNullOrBlank()) {
                        this.javaClass.simpleName
                    } else {
                        "${this.javaClass.simpleName}: $it"
                    }
                }
                UiErrorMessage.Message(debugErrorMessage)
            } else {
                UiErrorMessage.MessageResId(R.string.ui_state_error)
            }
        }
    }

@Composable
fun UiErrorMessage?.text(): String =
    when (this) {
        is UiErrorMessage.Message -> this.text
        is UiErrorMessage.MessageResId -> stringResource(this.textResId)
        null -> stringResource(R.string.ui_state_error)
    }
