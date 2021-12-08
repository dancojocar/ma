/*
 * Copyright 2018, Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.simplevideoview

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.simplevideoview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private lateinit var mVideoView: VideoView
  private lateinit var mBufferingTextView: TextView

  // Current playback position (in milliseconds).
  private var mCurrentPosition = 0
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    mVideoView = binding.videoview
    mBufferingTextView = binding.bufferingTextview
    if (savedInstanceState != null) {
      mCurrentPosition = savedInstanceState.getInt(PLAYBACK_TIME)
    }

    // Set up the media controller widget and attach it to the video view.
    val controller = MediaController(this)
    controller.setMediaPlayer(mVideoView)
    mVideoView.setMediaController(controller)
  }

  override fun onStart() {
    super.onStart()

    // Load the media each time onStart() is called.
    initializePlayer()
  }

  override fun onPause() {
    super.onPause()

    // In Android versions less than N (7.0, API 24), onPause() is the
    // end of the visual lifecycle of the app.  Pausing the video here
    // prevents the sound from continuing to play even after the app
    // disappears.
    //
    // This is not a problem for more recent versions of Android because
    // onStop() is now the end of the visual lifecycle, and that is where
    // most of the app teardown should take place.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
      mVideoView.pause()
    }
  }

  override fun onStop() {
    super.onStop()

    // Media playback takes a lot of resources, so everything should be
    // stopped and released at this time.
    releasePlayer()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    // Save the current playback position (in milliseconds) to the
    // instance state bundle.
    outState.putInt(PLAYBACK_TIME, mVideoView.currentPosition)
  }

  private fun initializePlayer() {
    // Show the "Buffering..." message while the video loads.
    mBufferingTextView.visibility = VideoView.VISIBLE

    // Buffer and decode the video sample.
    val videoUri = getMedia(VIDEO_SAMPLE)
    mVideoView.setVideoURI(videoUri)

    // Listener for onPrepared() event (runs after the media is prepared).
    mVideoView.setOnPreparedListener { // Hide buffering message.
      mBufferingTextView.visibility = VideoView.INVISIBLE

      // Restore saved position, if available.
      if (mCurrentPosition > 0) {
        mVideoView.seekTo(mCurrentPosition)
      } else {
        // Skipping to 1 shows the first frame of the video.
        mVideoView.seekTo(1)
      }

      // Start playing!
      mVideoView.start()
    }

    // Listener for onCompletion() event (runs after media has finished
    // playing).
    mVideoView.setOnCompletionListener {
      Toast.makeText(
        this@MainActivity,
        R.string.toast_message,
        Toast.LENGTH_SHORT
      ).show()

      // Return the video position to the start.
      mVideoView.seekTo(0)
    }
  }

  // Release all media-related resources. In a more complicated app this
  // might involve unregistering listeners or releasing audio focus.
  private fun releasePlayer() {
    mVideoView.stopPlayback()
  }

  // Get a Uri for the media sample regardless of whether that sample is
  // embedded in the app resources or available on the internet.
  private fun getMedia(mediaName: String): Uri {
    return if (URLUtil.isValidUrl(mediaName)) {
      // Media name is an external URL.
      Uri.parse(mediaName)
    } else {
      // Media name is a raw resource embedded in the app.
      Uri.parse(
        "android.resource://" + packageName +
            "/raw/" + mediaName
      )
    }
  }

  companion object {
    //    private const val VIDEO_SAMPLE = "tacoma_narrows"
    private const val VIDEO_SAMPLE =
      "http://techslides.com/demos/sample-videos/small.mp4";

    // Tag for the instance state bundle.
    private const val PLAYBACK_TIME = "play_time"
  }
}