package com.unieats.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.unieats.app.ui.spotdetail.SpotDetailScreen
import com.unieats.app.ui.spotlist.SpotListScreen
import com.unieats.app.ui.spotlist.SpotListViewModel
import kotlinx.serialization.Serializable

@Serializable
object SpotList

@Serializable
data class SpotDetail(val spotId: String)

const val DEEP_LINK_APP = "unieats://spots"
const val DEEP_LINK_WEB = "https://unieats.app/spots"

@Composable
fun UniEatsNavGraph(
    modifier: Modifier = Modifier,
    listViewModel: SpotListViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
) {
    val listState by listViewModel.uiState.collectAsStateWithLifecycle()
    NavHost(
        navController = navController,
        startDestination = SpotList,
        modifier = modifier
    ) {
        composable<SpotList> {
            SpotListScreen(onSpotClick = { spotId -> navController.navigate(SpotDetail(spotId)) }, viewModel = listViewModel)
        }

        // Both patterns resolve to <basePath>/{spotId}, e.g. unieats://spots/spot-3.
        composable<SpotDetail>(
            deepLinks = listOf(
                navDeepLink<SpotDetail>(basePath = DEEP_LINK_APP),
                navDeepLink<SpotDetail>(basePath = DEEP_LINK_WEB)
            )
        ) { backStackEntry ->
            val spotId = backStackEntry.toRoute<SpotDetail>().spotId
            SpotDetailScreen(
                isFavourite = spotId in listState.favouriteIds,
                onToggleFavourite = { listViewModel.toggleFavourite(spotId) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
