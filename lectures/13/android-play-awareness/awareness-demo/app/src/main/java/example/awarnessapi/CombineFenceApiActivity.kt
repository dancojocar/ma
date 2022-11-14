package example.awarnessapi

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.awareness.Awareness
import com.google.android.gms.awareness.fence.AwarenessFence
import com.google.android.gms.awareness.fence.FenceState
import com.google.android.gms.awareness.fence.FenceUpdateRequest
import com.google.android.gms.awareness.fence.LocationFence
import com.google.android.gms.awareness.fence.TimeFence
import com.google.android.gms.awareness.fence.TimeFence.*
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallbacks
import com.google.android.gms.common.api.Status

import java.util.ArrayList
import java.util.TimeZone

/**
 * This activity will demonstrate use awareness api and combine multiple conditions.
 *
 * In this we are going to use awareness apis to change your phone profile to silent when you satisfy below 3 conditions.
 * 1. particular location. (e.g. your work place)
 * 2. particular interval of time. (e.g. your work hours)
 * 3. particular day of week. (e.g. your work days)
 * see [.registerFence] to more detail.
 */
class CombineFenceApiActivity : AppCompatActivity(), View.OnClickListener, GoogleApiClient.ConnectionCallbacks {

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mStatusTv: TextView? = null

    private val mFenceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val fenceState = FenceState.extract(intent)

            if (TextUtils.equals(fenceState.fenceKey, COMBINE_FENCE_ENTERING_KEY)) {
                val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                when (fenceState.currentState) {
                    FenceState.TRUE -> {
                        mStatusTv!!.text = getString(R.string.atWorkMessage)
                        audio.ringerMode = AudioManager.RINGER_MODE_SILENT
                    }
                    FenceState.FALSE -> {
                        mStatusTv!!.text = getString(R.string.notAtWorkMessage)
                        audio.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    }
                    FenceState.UNKNOWN -> mStatusTv!!.text = getString(R.string.unknownMessage)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_combine_fence_api)

        mStatusTv = findViewById<View>(R.id.fence_status) as TextView

        buildApiClient()
    }

    override fun onStart() {
        super.onStart()

        //register the receiver to get notify
        registerReceiver(mFenceReceiver, IntentFilter(FENCE_RECEIVER_ACTION))
    }

    /**
     * Build the google api client to use awareness apis.
     */
    private fun buildApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this@CombineFenceApiActivity)
            .addApi(Awareness.API)
            .addConnectionCallbacks(this)
            .build()
        mGoogleApiClient!!.connect()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.register_fence ->
                //Check for the location permission. We need them to generate location fence.
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                } else {
                    registerFence()
                }
            R.id.unregister_fence -> unregisterFence()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                LOCATION_PERMISSION_REQUEST_CODE//location permission granted
                ->
                    registerFence()
            }
        }
    }

    override fun onStop() {
        super.onStop()

        //unregister the receiver.
        unregisterReceiver(mFenceReceiver)

        //unregister fence
        unregisterFence()
    }

    /**
     * Register fences to get notified at particular condition.
     * Here we are using multiple fences and also combining them.
     */
    @SuppressLint("MissingPermission")
    private fun registerFence() {
        /**
         * This is location fence will trigger while entering the into the given location.
         */
        val locationFence = LocationFence.`in`(
            46.7739757, //Latitude of place
            23.6183036, //Longitude of place
            500.00, //Radius in meters
            (5 * 1000).toLong()
        )     //Wait for the five seconds

        /**
         * This time fence will trigger between 10:30AM to 8PM on Monday to Friday.
         * So, generate 5 time fences for each day and apply or to them.
         */
        val timeFences = ArrayList<AwarenessFence>(6)
        for (i in 0..4) {
            when (i) {
                0//Register for Monday
                -> timeFences.add(
                    TimeFence.inIntervalOfDay(
                        DAY_OF_WEEK_MONDAY,
                        TimeZone.getDefault(),
                        10L * 30L * 60L * 1000L,
                        20L * 60L * 60L * 1000L
                    )
                ) //10:30AM - 8PM
                1 //Register for Tuesday
                -> timeFences.add(
                    TimeFence.inIntervalOfDay(
                        DAY_OF_WEEK_TUESDAY,
                        TimeZone.getDefault(),
                        10L * 30L * 60L * 1000L,
                        20L * 60L * 60L * 1000L
                    )
                ) //10:30AM - 8PM
                2//Register for Wednesday
                -> timeFences.add(
                    TimeFence
                        .inIntervalOfDay(
                            DAY_OF_WEEK_WEDNESDAY,
                            TimeZone.getDefault(),
                            10L * 30L * 60L * 1000L,
                            20L * 60L * 60L * 1000L
                        )
                ) //10:30AM - 8PM
                3//Register for Thursday
                -> timeFences.add(
                    TimeFence
                        .inIntervalOfDay(
                            DAY_OF_WEEK_THURSDAY,
                            TimeZone.getDefault(),
                            10L * 30L * 60L * 1000L,
                            20L * 60L * 60L * 1000L
                        )
                ) //10:30AM - 8PM
                4//Register for Friday
                -> timeFences.add(
                    TimeFence
                        .inIntervalOfDay(
                            DAY_OF_WEEK_FRIDAY,
                            TimeZone.getDefault(),
                            10L * 30L * 60L * 1000L,
                            20L * 60L * 60L * 1000L
                        )
                ) //10:30AM - 8PM
            }
        }
        val oredTimeFences = AwarenessFence.or(timeFences)

        /**
         * Now apply and fence to location fence and OR-ED time fences.
         */
        val andFence = AwarenessFence.and(locationFence, oredTimeFences)

        //generate pending intent to call when condition appears
        val fencePendingIntent = PendingIntent.getBroadcast(
            this,
            10001,
            Intent(FENCE_RECEIVER_ACTION),
            0
        )

        //fence to activate when headphone is plugged in
        Awareness.FenceApi.updateFences(
            mGoogleApiClient, FenceUpdateRequest.Builder()
                .addFence(COMBINE_FENCE_ENTERING_KEY, andFence, fencePendingIntent).build()
        )
            .setResultCallback(object : ResultCallbacks<Status>() {
                override fun onSuccess(status: Status) {
                    Toast.makeText(
                        this@CombineFenceApiActivity,
                        "Fence registered successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(status: Status) {
                    Toast.makeText(
                        this@CombineFenceApiActivity,
                        "Cannot register fence.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    /**
     * unregister fence.
     */
    private fun unregisterFence() {
        Awareness.FenceApi.updateFences(
            mGoogleApiClient,
            FenceUpdateRequest.Builder()
                .removeFence(COMBINE_FENCE_ENTERING_KEY)
                .build()
        ).setResultCallback(object : ResultCallbacks<Status>() {
            override fun onSuccess(status: Status) {
                Toast.makeText(
                    this@CombineFenceApiActivity,
                    "Fence unregistered successfully.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(status: Status) {
                Toast.makeText(
                    this@CombineFenceApiActivity,
                    "Cannot unregister fence.",
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
            .setPositiveButton(android.R.string.ok) { _, _ -> finish() }.show()
    }

    companion object {
        private const val COMBINE_FENCE_ENTERING_KEY = "entringCombineFence"
        private const val FENCE_RECEIVER_ACTION = "action.combine.fence"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 12345678

    }
}
