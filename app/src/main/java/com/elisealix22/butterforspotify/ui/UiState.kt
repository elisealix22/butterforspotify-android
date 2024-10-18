package com.elisealix22.butterforspotify.ui

import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.data.BuildConfig
import com.elisealix22.butterforspotify.data.error.ServiceError

sealed class UiState<T>(
    open val data: T? = null
) {
    data class Initial<T>(
        override val data: T? = null
    ) : UiState<T>(data)

    data class Error<T>(
        override val data: T?,
        val message: UiMessage? = null,
        val showInSnackbar: Boolean = (data != null && data !is List<*>) ||
            (data as? List<*>)?.isNotEmpty() == true,
        val onTryAgain: (() -> Unit)?
    ) : UiState<T>(data)

    data class Loading<T>(
        override val data: T?,
        val showFullscreen: Boolean = data == null || (data as? List<*>)?.isEmpty() == true
    ) : UiState<T>(data)

    data class Success<T>(
        override val data: T,
        val isEmpty: Boolean = (data as? List<*>)?.isEmpty() == true
    ) : UiState<T>(data)
}

fun <T> UiState<T>.isLoading() = this is UiState.Loading<*>

fun <T> UiState<T>.isLoadingOrInitial() = this is UiState.Loading<*> || this is UiState.Initial<*>

fun <T> UiState<T>.isError() = this is UiState.Error<*>

fun <T> UiState<T>.isSuccess() = this is UiState.Success<*>

fun Throwable.toUiErrorMessage(): UiMessage =
    when (this) {
        is ServiceError.IOError -> UiMessage.MessageResId(R.string.ui_state_network_error)
        is ServiceError.ApiError -> this.userFriendlyMessage.let {
            if (it.isNullOrBlank()) {
                UiMessage.MessageResId(R.string.ui_state_error)
            } else {
                UiMessage.Message(it)
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
                UiMessage.Message(debugErrorMessage)
            } else {
                UiMessage.MessageResId(R.string.ui_state_error)
            }
        }
    }
