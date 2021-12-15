package example.awarnessapi

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.awareness.Awareness
import com.google.android.gms.awareness.fence.AwarenessFence
import com.google.android.gms.awareness.fence.FenceState
import com.google.android.gms.awareness.fence.FenceUpdateRequest
import com.google.android.gms.awareness.fence.HeadphoneFence
import com.google.android.gms.awareness.state.HeadphoneState
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallbacks
import com.google.android.gms.common.api.Status

class HeadphoneFenceApiActivity : AppCompatActivity(), View.OnClickListener, GoogleApiClient.ConnectionCallbacks {

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mHeadPhoneStatusTv: TextView? = null

    /**
     * A [BroadcastReceiver] to be called when any of the awareness fence is activated.
     */
    private val mHeadPhoneFenceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val fenceState = FenceState.extract(intent)

            if (TextUtils.equals(
                    fenceState.fenceKey,
                    HEADPHONE_PLUG_FENCE_KEY
                )
            ) {//response if from headphone plug in fence
                when (fenceState.currentState) {
                    FenceState.TRUE   //Head phones are plugged in. (Check fence register code)
                    -> mHeadPhoneStatusTv!!.text = "Headphones connected."
                    FenceState.FALSE -> mHeadPhoneStatusTv!!.text = "Headphones disconnected."
                    FenceState.UNKNOWN -> mHeadPhoneStatusTv!!.text = "Confused.:-("
                }
            } else if (TextUtils.equals(
                    fenceState.fenceKey,
                    HEADPHONE_UNPLUG_FENCE_KEY
                )
            ) {//response if from headphone unplug fence
                when (fenceState.currentState) {
                    FenceState.FALSE -> mHeadPhoneStatusTv!!.text = "Headphones connected."
                    FenceState.TRUE //Head phones are unplugged. (Check fence register code)
                    -> mHeadPhoneStatusTv!!.text = "Headphones disconnected."
                    FenceState.UNKNOWN -> mHeadPhoneStatusTv!!.text = "Confused.:-("
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fence_api)

        mHeadPhoneStatusTv = findViewById<View>(R.id.fence_status) as TextView

        buildApiClient()
    }

    override fun onStart() {
        super.onStart()

        //register the receiver to get notify
        registerReceiver(mHeadPhoneFenceReceiver, IntentFilter(FENCE_RECEIVER_ACTION))
    }

    /**
     * Build the google api client to use awareness apis.
     */
    private fun buildApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this@HeadphoneFenceApiActivity)
            .addApi(Awareness.API)
            .addConnectionCallbacks(this)
            .build()
        mGoogleApiClient!!.connect()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.register_fence -> registerHeadphoneFence()
            R.id.unregister_fence -> unregisterHeadPhoneFence()
        }
    }

    override fun onStop() {
        super.onStop()

        //unregister the receiver.
        unregisterReceiver(mHeadPhoneFenceReceiver)

        //unregister fence
        unregisterHeadPhoneFence()
    }

    /**
     * Register the headphone status fence. This will register two fences.
     * 1. Fence to activate when headphones plugged in
     * 2. When headphones unplugged.
     */
    private fun registerHeadphoneFence() {
        //generate fence
        val headphonePlugFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN)
        val headphoneUnplugFence = HeadphoneFence.during(HeadphoneState.UNPLUGGED)

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
                .addFence(HEADPHONE_PLUG_FENCE_KEY, headphonePlugFence, fencePendingIntent).build()
        )
            .setResultCallback(object : ResultCallbacks<Status>() {
                override fun onSuccess(status: Status) {
                    Toast.makeText(
                        this@HeadphoneFenceApiActivity,
                        "Fence registered successfully. Plug in you head phones to see magic.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(status: Status) {
                    Toast.makeText(
                        this@HeadphoneFenceApiActivity,
                        "Cannot register headphone fence.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        //fence to activate when headphone is unplugged in
        Awareness.FenceApi.updateFences(
            mGoogleApiClient, FenceUpdateRequest.Builder()
                .addFence(HEADPHONE_UNPLUG_FENCE_KEY, headphoneUnplugFence, fencePendingIntent).build()
        )
    }

    /**
     * Unregister all fences.
     */
    private fun unregisterHeadPhoneFence() {
        Awareness.FenceApi.updateFences(
            mGoogleApiClient,
            FenceUpdateRequest.Builder()
                .removeFence(HEADPHONE_PLUG_FENCE_KEY)
                .removeFence(HEADPHONE_UNPLUG_FENCE_KEY)
                .build()
        ).setResultCallback(object : ResultCallbacks<Status>() {
            override fun onSuccess(status: Status) {
                Toast.makeText(
                    this@HeadphoneFenceApiActivity,
                    "Fence unregistered successfully.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(status: Status) {
                Toast.makeText(
                    this@HeadphoneFenceApiActivity,
                    "Cannot unregister headphone fence.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onConnected(bundle: Bundle?) {
        //Google API client connected.
        //ready to use awareness api
        findViewById<View>(R.id.register_fence).setOnClickListener(this)
        findViewById<View>(R.id.unregister_fence).setOnClickListener(this)
    }

    override fun onConnectionSuspended(i: Int) {
        AlertDialog.Builder(this)
            .setMessage("Cannot connect to google api services.")
            .setPositiveButton(android.R.string.ok) { dialogInterface, i -> finish() }.show()
    }

    companion object {
        private val HEADPHONE_PLUG_FENCE_KEY = "headphonesPlugFence"
        private val HEADPHONE_UNPLUG_FENCE_KEY = "headphonesUnplugFence"
        private val FENCE_RECEIVER_ACTION = "action.headphone.fence"
    }
}
