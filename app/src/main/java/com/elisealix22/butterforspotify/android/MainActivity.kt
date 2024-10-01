package com.elisealix22.butterforspotify.android

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elisealix22.butterforspotify.android.navigation.BottomNavigationTabs
import com.elisealix22.butterforspotify.android.navigation.ButterRoute
import com.elisealix22.butterforspotify.android.signin.SignInActivity
import com.elisealix22.butterforspotify.android.ui.theme.ButterForSpotifyTheme
import com.elisealix22.butterforspotify.data.auth.AuthStore

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AuthStore.blockingActiveUserToken.isNullOrBlank()) {
            startActivity(
                Intent(this, SignInActivity::class.java),
                ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle()
            )
            finish()
            return
        }

        setContent {
            ButterForSpotifyTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            BottomNavigationTabs.forEach { tab ->
                                NavigationBarItem(
                                    icon = { },
                                    label = { Text(stringResource(tab.name)) },
                                    selected = currentDestination?.hierarchy?.any {
                                        it.hasRoute(tab.route::class)
                                    } == true,
                                    onClick = {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = BottomNavigationTabs.first().route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<ButterRoute.Music> {
                            Greeting(
                                name = "Music",
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        composable<ButterRoute.Profile> {
                            Greeting(
                                name = "Profile",
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ButterForSpotifyTheme {
        Greeting("Android")
    }
}
