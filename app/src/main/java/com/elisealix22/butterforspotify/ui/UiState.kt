package com.elisealix22.butterforspotify.ui

sealed class UiState<T>(
    open val data: T? = null
) {
    data class Initial<T>(
        override val data: T? = null
    ) : UiState<T>(data)

    data class Error<T>(
        override val data: T?,
        val message: UiErrorMessage? = null,
        val showInSnackbar: Boolean = (data != null && data !is List<*>) ||
            (data as? List<*>)?.isNotEmpty() == true
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
