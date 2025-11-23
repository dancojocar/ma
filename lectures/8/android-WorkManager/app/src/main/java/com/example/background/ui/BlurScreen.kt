package com.example.background.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.WorkInfo
import coil.compose.rememberAsyncImagePainter
import com.example.background.BlurViewModel
import com.example.background.KEY_IMAGE_URI
import com.example.background.R
import androidx.core.net.toUri

@Composable
fun BlurScreen(viewModel: BlurViewModel) {
    val blurLevels = listOf(1, 2, 3)
    var selectedBlurLevel by remember { mutableIntStateOf(1) }

    val outputWorkInfos by viewModel.outputWorkInfos.collectAsState(initial = emptyList())

    val context = LocalContext.current

    val currentWorkInfo = outputWorkInfos.lastOrNull()

    // Remember the last successfully blurred image URI so we can keep showing it while new work runs
    var lastSuccessfulOutputUri by remember { mutableStateOf<String?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.applyBlur(selectedBlurLevel)
        } else {
            Toast.makeText(
                context,
                "Notification permissions not granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val isWorkRunning = when (currentWorkInfo?.state) {
        WorkInfo.State.ENQUEUED,
        WorkInfo.State.RUNNING,
        WorkInfo.State.BLOCKED -> true
        else -> false
    }

    val isWorkFinished = currentWorkInfo?.state == WorkInfo.State.SUCCEEDED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val outputUri = currentWorkInfo
            ?.outputData
            ?.getString(KEY_IMAGE_URI)

        // If the current work finished successfully, update the source image for next time
        if (isWorkFinished) {
            viewModel.updateImageUriFromOutput(outputUri)
            if (!outputUri.isNullOrEmpty()) {
                lastSuccessfulOutputUri = outputUri
            }
        }

        // Decide what to show in the Image:
        // - If we have a last successful blurred image, show that (even while new work runs)
        // - Otherwise, fall back to the current output or the original cupcake
        val imageUriToShow = when {
            !lastSuccessfulOutputUri.isNullOrEmpty() -> lastSuccessfulOutputUri
            !outputUri.isNullOrEmpty() -> outputUri
            else -> null
        }

        Image(
            painter = if (!imageUriToShow.isNullOrEmpty()) {
                rememberAsyncImagePainter(model = imageUriToShow.toUri())
            } else {
                painterResource(id = R.drawable.android_cupcake)
            },
            contentDescription = stringResource(id = R.string.description_image),
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.blur_title),
            style = MaterialTheme.typography.headlineSmall
        )

        Column(Modifier.selectableGroup()) {
            blurLevels.forEach { level ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (selectedBlurLevel == level),
                            onClick = { if (!isWorkRunning) selectedBlurLevel = level },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedBlurLevel == level),
                        onClick = null,
                        enabled = !isWorkRunning
                    )
                    Text(
                        text = "Blur Level $level",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isWorkRunning) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(id = R.string.work_in_progress),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = { viewModel.cancelWork() }
                ) {
                    Text(text = stringResource(id = R.string.cancel_work))
                }
            } else {
                Button(
                    onClick = {
                        val needsNotificationPermission =
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (needsNotificationPermission) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.applyBlur(selectedBlurLevel)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.go))
                }
            }

            val outputUri = currentWorkInfo
                ?.outputData
                ?.getString(KEY_IMAGE_URI)

            // Blurred image is now only shown inside this screen; external 'see file' action removed.
            // When work is finished, the Image at the top already reflects the blurred result.
            if (isWorkFinished && !outputUri.isNullOrEmpty()) {
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }
}
