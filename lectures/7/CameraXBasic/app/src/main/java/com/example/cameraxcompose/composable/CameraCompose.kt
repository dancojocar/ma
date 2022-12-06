package com.example.cameraxcompose.composable

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.cameraxcompose.camerax.CameraX
import com.example.cameraxcompose.utils.Commons.REQUIRED_PERMISSIONS

@Composable
fun CameraCompose(
  context: Context,
  cameraX: CameraX,
  onCaptureClick: () -> Unit,
) {
  var hasCamPermission by remember {
    mutableStateOf(
      REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) ==
            PackageManager.PERMISSION_GRANTED
      })
  }


  val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
    onResult = { granted ->
      hasCamPermission = granted.size == 2
    }
  )
  LaunchedEffect(key1 = true) {
    launcher.launch(
      arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
      )
    )
  }
  Column(modifier = Modifier.fillMaxSize()) {
    if (hasCamPermission) {
      AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { cameraX.startCameraPreviewView() }
      )
    }
  }
  Column(
    modifier = Modifier.fillMaxSize(), Arrangement.Bottom, Alignment.CenterHorizontally
  ) {
    Button(
      onClick = onCaptureClick
    ) {
      Text(text = "Capture")
    }
  }
}

