package com.elisealix22.butterforspotify.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.ui.UiState
import com.elisealix22.butterforspotify.ui.UiStateScaffold
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.Color
import com.elisealix22.butterforspotify.ui.theme.Dimen
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

@Composable
fun SignInScreen(
    viewModel: SignInViewModel = viewModel(),
    onSignInClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    SignInUiScaffold(
        uiState = uiState,
        onSignInClick = onSignInClick
    )
}

@Composable
private fun SignInUiScaffold(
    uiState: UiState<Unit>,
    onSignInClick: () -> Unit
) {
    UiStateScaffold(
        uiState = uiState,
        loadingContent = { SignInButton(uiState, onSignInClick) },
        content = { SignInButton(uiState, onSignInClick) }
    )
}

@Composable fun BoxScope.SignInButton(
    uiState: UiState<Unit>,
    onSignInClick: () -> Unit
) {
    val showLoading = uiState is UiState.Loading || uiState is UiState.Success
    Button(
        enabled = !showLoading,
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimen.PaddingDouble)
            .align(Alignment.Center),
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = Color.SpotifyGreen
        ),
        content = { SignInButtonContent(showLoading = showLoading) },
        onClick = onSignInClick
    )
}

@Composable
private fun SignInButtonContent(showLoading: Boolean) {
    Row {
        if (showLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically),
                strokeWidth = 4.dp,
                color = Color.SpotifyGreen
            )
        } else {
            Image(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(R.drawable.ic_spotify_16),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                contentDescription = stringResource(R.string.spotify_icon_description)
            )
        }
        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .align(Alignment.CenterVertically),
            fontSize = 16.sp,
            letterSpacing = 0.2.sp,
            fontWeight = FontWeight.Medium,
            text = stringResource(R.string.sign_in)
        )
    }
}

@ThemePreview
@Composable
fun SignInScreenPreview() {
    val uiState = UiState.Initial(Unit)
    ButterForSpotifyTheme {
        SignInUiScaffold(uiState = uiState, onSignInClick = { })
    }
}

@ThemePreview
@Composable
fun SignInLoadingPreview() {
    val uiState = UiState.Loading(Unit)
    ButterForSpotifyTheme {
        SignInUiScaffold(uiState = uiState, onSignInClick = { })
    }
}
