package com.elisealix22.butterforspotify.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.TextStyleFullscreen
import com.elisealix22.butterforspotify.ui.theme.ThemeColor
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

data class TryAgain(
    val message: String,
    val onTryAgain: () -> Unit
)

@Composable
fun <T> UiStateScaffold(
    modifier: Modifier = Modifier,
    uiState: UiState<T>,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    loadingContent: @Composable BoxScope.() -> Unit = { DefaultLoadingContent() },
    emptyContent: @Composable BoxScope.() -> Unit = { DefaultEmptyContent() },
    errorContent: @Composable BoxScope.(
        errorMessage: String,
        tryAgain: TryAgain?
    ) -> Unit = { errorMessage, tryAgain ->
        DefaultErrorContent(errorMessage = errorMessage, tryAgain = tryAgain)
    },
    content: @Composable BoxScope.() -> Unit
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            when (uiState) {
                is UiState.Initial -> content()
                is UiState.Success -> {
                    if (uiState.isEmpty) {
                        emptyContent()
                    } else {
                        content()
                    }
                }

                is UiState.Error -> {
                    val errorMessage =
                        uiState.message?.text() ?: stringResource(R.string.ui_state_error)
                    val tryAgain = uiState.onTryAgain?.let {
                        TryAgain(
                            message = stringResource(R.string.ui_state_try_again),
                            onTryAgain = it
                        )
                    }
                    if (uiState.showInSnackbar) {
                        LaunchedEffect(uiState) {
                            if (tryAgain != null) {
                                val result = snackbarHostState.showSnackbar(
                                    message = errorMessage,
                                    actionLabel = tryAgain.message,
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    tryAgain.onTryAgain()
                                }
                            } else {
                                snackbarHostState.showSnackbar(errorMessage)
                            }
                        }
                        content()
                    } else {
                        errorContent(errorMessage, tryAgain)
                    }
                }

                is UiState.Loading -> {
                    if (uiState.showFullscreen) {
                        loadingContent()
                    } else {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.DefaultLoadingContent(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier
            .size(48.dp)
            .align(Alignment.Center)
    )
}

@Composable
private fun BoxScope.DefaultEmptyContent(
    modifier: Modifier = Modifier
) {
    RainbowText(
        modifier = modifier
            .padding(Dimen.PaddingDouble)
            .align(Alignment.Center),
        text = stringResource(R.string.ui_state_empty)
    )
}

@Composable
private fun BoxScope.DefaultErrorContent(
    modifier: Modifier = Modifier,
    errorMessage: String,
    tryAgain: TryAgain?
) {
    Column(
        modifier = modifier
            .align(Alignment.Center)
            .padding(Dimen.PaddingDouble)
    ) {
        RainbowText(
            modifier = Modifier,
            text = errorMessage
        )
        if (tryAgain != null) {
            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = Dimen.PaddingDouble),
                content = { Text(tryAgain.message) },
                onClick = tryAgain.onTryAgain
            )
        }
    }
}

@Composable
private fun RainbowText(
    modifier: Modifier,
    text: String
) {
    val rainbowText = remember(text) {
        buildAnnotatedString {
            withStyle(
                SpanStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            ThemeColor.Tangerine,
                            ThemeColor.Orange,
                            ThemeColor.Citrus,
                            ThemeColor.Blue,
                            ThemeColor.Pink
                        )
                    )
                )
            ) {
                append(text)
            }
        }
    }
    Text(
        modifier = modifier,
        textAlign = TextAlign.Center,
        text = rainbowText,
        style = TextStyleFullscreen
    )
}

@ThemePreview
@Composable
fun UiStateLoadingPreview() {
    val uiState = UiState.Loading(data = null)
    ButterForSpotifyTheme {
        UiStateScaffold(
            uiState = uiState,
            content = { }
        )
    }
}

@ThemePreview
@Composable
fun UiStateEmptyPreview() {
    val uiState = UiState.Success(data = Unit, isEmpty = true)
    ButterForSpotifyTheme {
        UiStateScaffold(
            uiState = uiState,
            content = { }
        )
    }
}

@ThemePreview
@Composable
fun UiStateErrorTryAgainPreview() {
    val uiState = UiState.Error(data = Unit, showInSnackbar = false, onTryAgain = {})
    ButterForSpotifyTheme {
        UiStateScaffold(
            uiState = uiState,
            content = { }
        )
    }
}

@ThemePreview
@Composable
fun UiStateErrorPreview() {
    val uiState = UiState.Error(data = Unit, showInSnackbar = false, onTryAgain = null)
    ButterForSpotifyTheme {
        UiStateScaffold(
            uiState = uiState,
            content = { }
        )
    }
}

@ThemePreview
@Composable
fun UiStateSnackbarTryAgainPreview() {
    val hostState = SnackbarHostState()
    val uiState = UiState.Error(data = Unit, showInSnackbar = true, onTryAgain = {})
    ButterForSpotifyTheme {
        UiStateScaffold(
            uiState = uiState,
            content = { },
            snackbarHostState = hostState
        )
    }
}
