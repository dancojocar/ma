package com.example.cameraxcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.cameraxcompose.camerax.CameraX
import com.example.cameraxcompose.composable.CameraCompose
import com.example.cameraxcompose.ui.theme.CameraXComposeTheme
import com.example.cameraxcompose.utils.Commons.allPermissionsGranted

class MainActivity : ComponentActivity() {
    private var cameraX: CameraX = CameraX(this, this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraXComposeTheme {
                CameraCompose(this, cameraX = cameraX) {
                    if (allPermissionsGranted(this)) {
                        cameraX.capturePhoto()
                    }
                }
            }
        }
    }

}
