package com.elisealix22.butterforspotify.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun BottomNavigationText(
    modifier: Modifier = Modifier,
    tab: BottomNavigationTab
) {
    Text(
        modifier = modifier,
        text = stringResource(tab.name),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}
