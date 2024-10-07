package com.elisealix22.butterforspotify.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Color
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.TextStyleFullscreen
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

@Composable
fun <T> UiStateScaffold(
    modifier: Modifier = Modifier,
    uiState: UiState<T>,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    loadingContent: @Composable BoxScope.() -> Unit = { DefaultLoadingContent() },
    emptyContent: @Composable BoxScope.() -> Unit = { DefaultEmptyContent() },
    errorContent: @Composable BoxScope.(errorMessage: String) -> Unit = { errorMessage ->
        DefaultErrorContent(errorMessage = errorMessage)
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
                    val errorMessage = when (uiState.message) {
                        is UiErrorMessage.Message -> uiState.message.text
                        is UiErrorMessage.MessageResId -> stringResource(uiState.message.textResId)
                        null -> stringResource(R.string.ui_state_error)
                    }
                    if (uiState.showInSnackbar) {
                        LaunchedEffect(uiState) {
                            snackbarHostState.showSnackbar(errorMessage)
                        }
                        content()
                    } else {
                        errorContent(errorMessage)
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
fun BoxScope.DefaultLoadingContent(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier
            .width(64.dp)
            .align(Alignment.Center),
        strokeWidth = 8.dp
    )
}

@Composable
fun BoxScope.DefaultEmptyContent(
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier
            .padding(Dimen.Padding2x)
            .align(Alignment.Center),
        textAlign = TextAlign.Center,
        text = rainbowText(stringResource(R.string.ui_state_empty)),
        style = TextStyleFullscreen
    )
}

@Composable
fun BoxScope.DefaultErrorContent(
    modifier: Modifier = Modifier,
    errorMessage: String
) {
    Text(
        modifier = modifier
            .padding(Dimen.Padding2x)
            .align(Alignment.Center),
        textAlign = TextAlign.Center,
        text = rainbowText(errorMessage),
        style = TextStyleFullscreen
    )
}

private fun rainbowText(text: String): AnnotatedString = buildAnnotatedString {
    withStyle(
        SpanStyle(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Tangerine,
                    Color.Orange,
                    Color.Citrus,
                    Color.Blue,
                    Color.Pink
                )
            )
        )
    ) {
        append(text)
    }
}

@ThemePreview
@Composable
fun UiStateLoadingPreview() {
    val uiState = UiState.Loading(data = Unit)
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
fun UiStateErrorPreview() {
    val uiState = UiState.Error(data = Unit, showInSnackbar = false)
    ButterForSpotifyTheme {
        UiStateScaffold(
            uiState = uiState,
            content = { }
        )
    }
}
