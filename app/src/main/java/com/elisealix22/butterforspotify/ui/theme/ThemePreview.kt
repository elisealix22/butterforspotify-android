package com.elisealix22.butterforspotify.ui.theme

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Light mode", showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "Night mode", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
annotation class ThemePreview

@Preview(
    name = "Light mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO,
    widthDp = 720,
    heightDp = 360
)
@Preview(
    name = "Night mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES,
    widthDp = 720,
    heightDp = 360
)
annotation class LandscapeThemePreview
