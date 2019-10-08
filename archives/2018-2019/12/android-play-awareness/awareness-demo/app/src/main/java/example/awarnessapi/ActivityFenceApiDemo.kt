package example.awarnessapi

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.awareness.Awareness
import com.google.android.gms.awareness.fence.DetectedActivityFence
import com.google.android.gms.awareness.fence.FenceState
import com.google.android.gms.awareness.fence.FenceUpdateRequest
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallbacks
import com.google.android.gms.common.api.Status

class ActivityFenceApiDemo : AppCompatActivity(), View.OnClickListener, GoogleApiClient.ConnectionCallbacks {

  private var mGoogleApiClient: GoogleApiClient? = null
  private var mActivityFenceStatusTv: TextView? = null

  /**
   * A [BroadcastReceiver] to be called when any of the awareness fence is activated.
   */
  private val mActivityFenceReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val fenceState = FenceState.extract(intent)

      if (TextUtils.equals(fenceState.fenceKey, ACTIVITY_STILL_FENCE_KEY)) {
        when (fenceState.currentState) {
          FenceState.TRUE   //User is still
          -> mActivityFenceStatusTv!!.text = getString(R.string.stillMessage)
          FenceState.FALSE -> mActivityFenceStatusTv!!.text = getString(R.string.movingMessage)
          FenceState.UNKNOWN -> mActivityFenceStatusTv!!.text = getString(R.string.unknownMessage)
        }
      } else if (TextUtils.equals(fenceState.fenceKey, ACTIVITY_MOVING_FENCE_KEY)) {
        when (fenceState.currentState) {
          FenceState.FALSE -> mActivityFenceStatusTv!!.text = getString(R.string.stillMessage)
          FenceState.TRUE //User is moving
          -> mActivityFenceStatusTv!!.text = getString(R.string.movingMessage)
          FenceState.UNKNOWN -> mActivityFenceStatusTv!!.text = getString(R.string.unknownMessage)
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_fence_api_demo)
    mActivityFenceStatusTv = findViewById<View>(R.id.activity_fence_status) as TextView

    buildApiClient()
  }

  override fun onStart() {
    super.onStart()

    //register the receiver to get notify
    registerReceiver(mActivityFenceReceiver, IntentFilter(FENCE_RECEIVER_ACTION))
  }

  /**
   * Build the google api client to use awareness apis.
   */
  private fun buildApiClient() {
    mGoogleApiClient = GoogleApiClient.Builder(this)
        .addApi(Awareness.API)
        .addConnectionCallbacks(this)
        .build()
    mGoogleApiClient!!.connect()
  }

  override fun onClick(view: View) {
    when (view.id) {
      R.id.register_activity_fence -> registerActivityFence()
      R.id.unregister_activity_fence -> unregisterActivityFence()
    }
  }

  override fun onStop() {
    super.onStop()

    //unregister the receiver.
    unregisterReceiver(mActivityFenceReceiver)

    //unregister fence
    unregisterActivityFence()
  }

  /**
   * Register the user activity fence. This will register two fences.
   * 1. Fence to activate when user is still
   * 2. When user is walking.
   */
  private fun registerActivityFence() {
    //generate fence
    val activityStillFence = DetectedActivityFence.during(DetectedActivityFence.STILL)
    val activityMovingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING)

    //generate pending intent
    val fencePendingIntent = PendingIntent.getBroadcast(
        this,
        10001,
        Intent(FENCE_RECEIVER_ACTION),
        0
    )

    //fence to activate when headphone is plugged in
    Awareness.FenceApi.updateFences(
        mGoogleApiClient, FenceUpdateRequest.Builder()
        .addFence(ACTIVITY_STILL_FENCE_KEY, activityStillFence, fencePendingIntent).build()
    )
        .setResultCallback(object : ResultCallbacks<Status>() {
          override fun onSuccess(status: Status) {
            Toast.makeText(
                this@ActivityFenceApiDemo,
                "Fence registered successfully. Move your device to see magic.",
                Toast.LENGTH_SHORT
            ).show()
          }

          override fun onFailure(status: Status) {
            Toast.makeText(
                this@ActivityFenceApiDemo,
                "Cannot register activity fence.",
                Toast.LENGTH_SHORT
            ).show()
          }
        })

    //fence to activate when headphone is unplugged in
    Awareness.FenceApi.updateFences(
        mGoogleApiClient, FenceUpdateRequest.Builder()
        .addFence(ACTIVITY_MOVING_FENCE_KEY, activityMovingFence, fencePendingIntent).build()
    )
  }

  private fun unregisterActivityFence() {
    Awareness.FenceApi.updateFences(
        mGoogleApiClient,
        FenceUpdateRequest.Builder()
            .removeFence(ACTIVITY_STILL_FENCE_KEY)
            .removeFence(ACTIVITY_MOVING_FENCE_KEY)
            .build()
    ).setResultCallback(object : ResultCallbacks<Status>() {
      override fun onSuccess(status: Status) {
        Toast.makeText(
            this@ActivityFenceApiDemo,
            "Fence unregistered successfully.",
            Toast.LENGTH_SHORT
        ).show()
      }

      override fun onFailure(status: Status) {
        Toast.makeText(
            this@ActivityFenceApiDemo,
            "Cannot unregister fence.",
            Toast.LENGTH_SHORT
        ).show()
      }
    })
  }

  override fun onConnected(bundle: Bundle?) {
    //Google API client connected.
    //ready to use awareness api
    findViewById<View>(R.id.register_activity_fence).setOnClickListener(this)
    findViewById<View>(R.id.unregister_activity_fence).setOnClickListener(this)
  }

  override fun onConnectionSuspended(i: Int) {
    AlertDialog.Builder(this)
        .setMessage("Cannot connect to google api services.")
        .setPositiveButton(android.R.string.ok) { _, _ -> finish() }.show()
  }

  companion object {
    private const val ACTIVITY_STILL_FENCE_KEY = "stillActivityFence"
    private const val ACTIVITY_MOVING_FENCE_KEY = "movingActivityFence"
    private const val FENCE_RECEIVER_ACTION = "action.activity.fence"
  }
}
