package com.unieats.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.unieats.app.ui.login.LoginScreen
import com.unieats.app.ui.nearby.NearbyScreen
import com.unieats.app.ui.spotdetail.SpotDetailScreen
import com.unieats.app.ui.spotlist.SpotListScreen
import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object SpotList

@Serializable
data class SpotDetail(val spotId: String)

@Serializable
object Nearby

const val DEEP_LINK_APP = "unieats://spots"
const val DEEP_LINK_WEB = "https://unieats.app/spots"

@Composable
fun UniEatsNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val isSignedIn by sessionViewModel.isSignedIn.collectAsStateWithLifecycle()
    val startDestination: Any = remember { if (isSignedIn) SpotList else Login }

    // Signing in leaves the login screen; signing out or a 401 (token cleared) returns to it.
    LaunchedEffect(isSignedIn) {
        val onLogin = navController.currentDestination?.hasRoute<Login>() == true
        if (isSignedIn && onLogin) {
            navController.navigate(SpotList) { popUpTo<Login> { inclusive = true } }
        } else if (!isSignedIn && !onLogin) {
            navController.navigate(Login) { popUpTo(navController.graph.id) { inclusive = true } }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        composable<Login> { LoginScreen() }

        composable<SpotList> {
            SpotListScreen(
                onSpotClick = { spotId -> navController.navigate(SpotDetail(spotId)) },
                onNearbyClick = { navController.navigate(Nearby) },
                onLogout = sessionViewModel::logout
            )
        }

        composable<Nearby> {
            NearbyScreen(
                onBack = { navController.popBackStack() },
                onSpotClick = { spotId -> navController.navigate(SpotDetail(spotId)) }
            )
        }

        // Both patterns resolve to <basePath>/{spotId}, e.g. unieats://spots/spot-3.
        composable<SpotDetail>(
            deepLinks = listOf(
                navDeepLink<SpotDetail>(basePath = DEEP_LINK_APP),
                navDeepLink<SpotDetail>(basePath = DEEP_LINK_WEB)
            )
        ) {
            SpotDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
