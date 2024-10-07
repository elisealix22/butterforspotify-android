package com.elisealix22.butterforspotify.signin

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
        uiState = uiState
    ) {
        Button(
            modifier = Modifier
                .padding(Dimen.Padding2x)
                .align(Alignment.Center),
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = Color.SpotifyGreen
            ),
            content = { Text(text = stringResource(R.string.sign_in)) },
            onClick = onSignInClick
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
