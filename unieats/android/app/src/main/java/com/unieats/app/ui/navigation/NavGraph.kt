package com.unieats.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.unieats.app.ui.spotdetail.SpotDetailScreen
import com.unieats.app.ui.spotdetail.SpotDetailViewModel
import com.unieats.app.ui.spotlist.SpotListScreen
import kotlinx.serialization.Serializable

@Serializable
data object SpotList : NavKey

@Serializable
data class SpotDetail(val spotId: String) : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun UniEatsNavDisplay(
    startKeys: List<NavKey>,
    modifier: Modifier = Modifier,
    pendingDeepLink: List<NavKey>? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val backStack = rememberNavBackStack(*startKeys.toTypedArray())
    LaunchedEffect(pendingDeepLink) {
        if (pendingDeepLink != null) {
            backStack.clear()
            backStack.addAll(pendingDeepLink)
            onDeepLinkConsumed()
        }
    }
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        sceneStrategies = listOf(rememberListDetailSceneStrategy()),
        entryProvider = entryProvider {
            entry<SpotList>(
                metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { SelectASpotPlaceholder() })
            ) {
                SpotListScreen(onSpotClick = { spotId -> backStack.add(SpotDetail(spotId)) })
            }

            entry<SpotDetail>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                SpotDetailScreen(
                    onBack = { backStack.removeLastOrNull() },
                    viewModel = hiltViewModel<SpotDetailViewModel, SpotDetailViewModel.Factory>(
                        creationCallback = { factory -> factory.create(key.spotId) }
                    )
                )
            }
        }
    )
}

@Composable
private fun SelectASpotPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Select a spot", style = MaterialTheme.typography.titleMedium)
    }
}
