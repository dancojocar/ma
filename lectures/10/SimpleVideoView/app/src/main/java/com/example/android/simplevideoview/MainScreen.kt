package com.example.android.simplevideoview

import android.net.Uri
import android.os.Build
import android.webkit.URLUtil
import android.widget.VideoView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var videoView: VideoView? by remember { mutableStateOf(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableStateOf(0) }
    var totalDuration by remember { mutableStateOf(0) }
    var sliderPosition by remember { mutableStateOf(0f) }
    var isDraggingSlider by remember { mutableStateOf(false) }

    // Polling for progress updates
    LaunchedEffect(isPlaying, isDraggingSlider) {
        while (isPlaying && !isDraggingSlider) {
            videoView?.let {
                currentPosition = it.currentPosition
                sliderPosition = it.currentPosition.toFloat()
            }
            delay(500)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    videoView?.pause()
                    isPlaying = false
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                videoView?.stopPlayback()
                isPlaying = false
            } else if (event == Lifecycle.Event.ON_START) {
                 if (videoView != null && !videoView!!.isPlaying) {
                     val uri = getMedia(context, "tacoma_narrows")
                     videoView?.setVideoURI(uri)
                     isBuffering = true
                 }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp), // Fixed height for video container
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                videoView = this
                                setOnPreparedListener { mp ->
                                    isBuffering = false
                                    totalDuration = duration
                                    mp.isLooping = true
                                    start()
                                    isPlaying = true
                                }
                                setOnCompletionListener {
                                    isPlaying = false
                                    seekTo(0)
                                }
                                val uri = getMedia(ctx, "tacoma_narrows")
                                setVideoURI(uri)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    if (isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(currentPosition),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        IconButton(
                            onClick = {
                                videoView?.let {
                                    if (it.isPlaying) {
                                        it.pause()
                                        isPlaying = false
                                    } else {
                                        it.start()
                                        isPlaying = true
                                    }
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = if (isPlaying) painterResource(android.R.drawable.ic_media_pause) else painterResource(android.R.drawable.ic_media_play),
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = formatDuration(totalDuration),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Slider(
                        value = sliderPosition,
                        onValueChange = { 
                            isDraggingSlider = true
                            sliderPosition = it 
                        },
                        onValueChangeFinished = {
                            videoView?.seekTo(sliderPosition.toInt())
                            isDraggingSlider = false
                            currentPosition = sliderPosition.toInt()
                        },
                        valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun getMedia(context: android.content.Context, mediaName: String): Uri {
    return if (URLUtil.isValidUrl(mediaName)) {
      mediaName.toUri()
    } else {
      ("android.resource://" + context.packageName + "/raw/" + mediaName).toUri()
    }
}

private fun formatDuration(millis: Int): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis.toLong())
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis.toLong()) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}
