package com.elisealix22.butterforspotify.navigation

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
fun BottomNavigationIcon(
    modifier: Modifier = Modifier,
    tab: BottomNavigationTab
) {
    Icon(
        modifier = modifier,
        painter = painterResource(tab.iconResId),
        contentDescription = stringResource(tab.name)
    )
}
