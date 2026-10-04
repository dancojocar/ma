package com.unieats.app.ui.nearby

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unieats.app.ui.components.LoadingView
import com.unieats.app.ui.components.MessageView
import kotlin.math.roundToInt

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(
    onBack: () -> Unit,
    onSpotClick: (String) -> Unit,
    viewModel: NearbyViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    var askedOnce by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        askedOnce = true
        viewModel.onPermissionResult()
    }
    // Re-check when coming back from the system settings screen.
    LifecycleResumeEffect(Unit) {
        viewModel.onPermissionResult()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spots near me") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when {
            !state.hasPermission -> {
                val showRationale = activity != null &&
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                PermissionRequest(
                    showRationale = showRationale,
                    permanentlyDenied = askedOnce && !showRationale,
                    onRequest = { launcher.launch(LOCATION_PERMISSIONS) },
                    modifier = contentModifier
                )
            }
            state.location == null -> LoadingView(contentModifier)
            state.spots.isEmpty() -> MessageView(
                "No spots within 2 km of you.\n\nOn the emulator, set the location to the campus " +
                    "(44.427, 26.103) in Extended controls → Location.",
                contentModifier
            )
            else -> LazyColumn(
                modifier = contentModifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.spots, key = { it.spot.id }) { nearby ->
                    NearbyRow(nearby, onClick = { onSpotClick(nearby.spot.id) })
                }
            }
        }
    }
}

@Composable
private fun PermissionRequest(
    showRationale: Boolean,
    permanentlyDenied: Boolean,
    onRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = when {
                permanentlyDenied -> "Location is turned off for UniEats. Enable it in Settings to see spots near you."
                showRationale -> "Without your location UniEats cannot tell which spots are within walking distance. " +
                    "It is used only while this screen is open."
                else -> "UniEats uses your location only while this screen is open, to list spots within 2 km."
            },
            textAlign = TextAlign.Center
        )
        if (permanentlyDenied) {
            Button(onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
                )
            }) { Text("Open settings") }
        } else {
            Button(onClick = onRequest) { Text("Allow location") }
        }
    }
}

@Composable
private fun NearbyRow(nearby: NearbySpot, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(nearby.spot.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${nearby.distanceMeters.roundToInt()} m · ${nearby.spot.category.label} · " +
                    if (nearby.spot.openNow) "Open" else "Closed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
