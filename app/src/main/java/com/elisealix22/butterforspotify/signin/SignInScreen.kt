package com.elisealix22.butterforspotify.signin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.elisealix22.butterforspotify.R
import com.elisealix22.butterforspotify.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.ui.theme.ThemePreview

@Composable
fun SignInScreen(onSignInClick: () -> Unit) {
    Surface {
        Box(modifier = Modifier.fillMaxSize()) {
            Button(
                modifier = Modifier.align(Alignment.Center),
                content = { Text(text = stringResource(R.string.sign_in)) },
                onClick = onSignInClick
            )
        }
    }
}

@ThemePreview
@Composable
fun SignInScreenPreview() {
    ButterForSpotifyTheme {
        SignInScreen(onSignInClick = {})
    }
}
