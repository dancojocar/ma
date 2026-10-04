package com.unieats.app.ui.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** Android 17 (API 37): reaching the laptop at 10.0.2.2 or a LAN address needs a runtime grant. */
val LOCAL_NETWORK_PERMISSIONS: List<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
        listOf(Manifest.permission.ACCESS_LOCAL_NETWORK)
    } else {
        emptyList()
    }

val NOTIFICATION_PERMISSIONS: List<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyList()
    }

/**
 * Android handles one permission request at a time and drops a second one launched meanwhile,
 * so each screen asks for everything it still needs in a single call.
 */
@Composable
fun RequestPermissionsOnce(
    permissions: List<String>,
    onResult: (granted: Map<String, Boolean>) -> Unit = {}
) {
    val context = LocalContext.current
    var asked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        asked = true
        onResult(it)
    }
    LaunchedEffect(permissions) {
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty() && !asked) launcher.launch(missing.toTypedArray())
    }
}
