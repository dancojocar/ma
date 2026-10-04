package com.unieats.app.ui.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.unieats.app.R
import com.unieats.app.data.auth.SessionState
import com.unieats.app.ui.auth.UnlockScreen
import com.unieats.app.ui.auth.authenticate
import com.unieats.app.ui.auth.rememberBiometricUnlockAvailable
import com.unieats.app.ui.login.LoginScreen
import com.unieats.app.ui.nearby.NearbyScreen
import com.unieats.app.ui.spotdetail.SpotDetailScreen
import com.unieats.app.ui.spotdetail.SpotDetailViewModel
import com.unieats.app.ui.spotlist.SpotListScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data object Login : NavKey

@Serializable
data object Unlock : NavKey

@Serializable
data object SpotList : NavKey

@Serializable
data class SpotDetail(val spotId: String) : NavKey

@Serializable
data object Nearby : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun UniEatsNavDisplay(
    startKeys: List<NavKey>,
    modifier: Modifier = Modifier,
    pendingDeepLink: List<NavKey>? = null,
    onDeepLinkConsumed: () -> Unit = {},
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val session by sessionViewModel.session.collectAsStateWithLifecycle()
    // MainActivity keeps the splash screen up until DataStore has been read.
    if (session == SessionState.Loading) return

    val authKey: NavKey? = when (session) {
        is SessionState.SignedIn -> null
        is SessionState.Locked -> Unlock
        else -> Login
    }
    val backStack = rememberNavBackStack(*(authKey?.let { listOf(it) } ?: startKeys).toTypedArray())

    // Signing in or unlocking leaves the auth screen for startKeys (a cold-start deep link
    // included); signing out or a 401 (token cleared) returns to Login.
    LaunchedEffect(authKey) {
        val onAuthScreen = backStack.lastOrNull().let { it == Login || it == Unlock }
        when {
            authKey == null && onAuthScreen -> {
                backStack.clear()
                backStack.addAll(startKeys)
            }
            authKey != null && backStack.lastOrNull() != authKey -> {
                backStack.clear()
                backStack.add(authKey)
            }
        }
    }

    LaunchedEffect(pendingDeepLink, authKey) {
        if (pendingDeepLink != null && authKey == null) {
            backStack.clear()
            backStack.addAll(pendingDeepLink)
            onDeepLinkConsumed()
        }
    }

    val activity = LocalActivity.current as FragmentActivity
    val scope = rememberCoroutineScope()
    val biometricAvailable = rememberBiometricUnlockAvailable()
    val biometricEnabled by sessionViewModel.biometricEnabled.collectAsStateWithLifecycle()
    val enablePromptTitle = stringResource(R.string.biometric_enable_prompt_title)
    val enablePromptNegative = stringResource(R.string.action_cancel)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        sceneStrategies = listOf(rememberListDetailSceneStrategy()),
        transitionSpec = {
            (slideInHorizontally(initialOffsetX = { it }) + fadeIn()) togetherWith
                (slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut())
        },
        popTransitionSpec = {
            (slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn()) togetherWith
                (slideOutHorizontally(targetOffsetX = { it }) + fadeOut())
        },
        predictivePopTransitionSpec = { _ ->
            (slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn()) togetherWith
                (slideOutHorizontally(targetOffsetX = { it }) + fadeOut())
        },
        entryProvider = entryProvider {
            entry<Login> { LoginScreen() }

            entry<Unlock> { UnlockScreen(onUsePassword = sessionViewModel::logout) }

            entry<SpotList>(
                metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { SelectASpotPlaceholder() })
            ) {
                SpotListScreen(
                    onSpotClick = { spotId -> backStack.add(SpotDetail(spotId)) },
                    onNearbyClick = { backStack.add(Nearby) },
                    onLogout = sessionViewModel::logout,
                    biometricAvailable = biometricAvailable,
                    biometricEnabled = biometricEnabled,
                    onToggleBiometric = { enable ->
                        if (enable) {
                            scope.launch {
                                sessionViewModel.enableBiometric { cipher ->
                                    activity.authenticate(cipher, enablePromptTitle, enablePromptNegative)
                                }
                            }
                        } else {
                            sessionViewModel.disableBiometric()
                        }
                    }
                )
            }

            entry<Nearby> {
                NearbyScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onSpotClick = { spotId -> backStack.add(SpotDetail(spotId)) }
                )
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
        Text(stringResource(R.string.list_select_a_spot), style = MaterialTheme.typography.titleMedium)
    }
}
