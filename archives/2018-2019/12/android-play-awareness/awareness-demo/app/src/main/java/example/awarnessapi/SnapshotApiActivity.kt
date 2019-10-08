package example.awarnessapi

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.annotation.RequiresPermission
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.awareness.Awareness
import com.google.android.gms.awareness.snapshot.DetectedActivityResult
import com.google.android.gms.awareness.snapshot.HeadphoneStateResult
import com.google.android.gms.awareness.snapshot.LocationResult
import com.google.android.gms.awareness.snapshot.PlacesResult
import com.google.android.gms.awareness.snapshot.WeatherResult
import com.google.android.gms.awareness.state.HeadphoneState
import com.google.android.gms.awareness.state.Weather
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.places.PlaceLikelihood
import com.squareup.picasso.Picasso

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * This activity will demonstrate how to use snapshot apis.
 *
 * @see 'https://developers.google.com/awareness/android-api/snapshot-get-data'
 */
class SnapshotApiActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks {

    private var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snapshot)

        buildApiClient()
    }

    /**
     * Build the google api client to use awareness apis.
     */
    private fun buildApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this@SnapshotApiActivity)
            .addApi(Awareness.API)
            .addConnectionCallbacks(this)
            .build()
        mGoogleApiClient!!.connect()
    }

    override fun onConnected(bundle: Bundle?) {
        //Google API client connected.
        //ready to use awareness api
        callSnapShotGroupApis()
    }

    /**
     * This method will call all the snap shot group apis.
     */
    private fun callSnapShotGroupApis() {
        //get info about user's current activity
        getCurrentActivity()

        //get the current state of the headphones.
        getHeadphoneStatus()

        //get current location. This will need location permission, so first check that.
        if (ContextCompat.checkSelfPermission(
                this@SnapshotApiActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@SnapshotApiActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                GET_LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLocation()
        }

        //get current place. This will need location permission, so first check that.
        if (ContextCompat.checkSelfPermission(
                this@SnapshotApiActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@SnapshotApiActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                GET_PLACE_PERMISSION_REQUEST_CODE
            )
        } else {
            getPlace()
        }

        //get current weather conditions. This will need location permission, so first check that.
        if (ContextCompat.checkSelfPermission(
                this@SnapshotApiActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@SnapshotApiActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                GET_WEATHER_PERMISSION_REQUEST_CODE
            )
        } else {
            getWeather()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                GET_LOCATION_PERMISSION_REQUEST_CODE//location permission granted
                ->
                    getLocation()
                GET_PLACE_PERMISSION_REQUEST_CODE//location permission granted
                ->
                    getPlace()
                GET_WEATHER_PERMISSION_REQUEST_CODE//location permission granted
                ->
                    getWeather()
            }
        }
    }

    /**
     * Get the current weather condition at current location.
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    private fun getWeather() {

        Awareness.SnapshotApi.getWeather(mGoogleApiClient)
            .setResultCallback(ResultCallback { weatherResult ->
                if (!weatherResult.status.isSuccess) {
                    Toast.makeText(this@SnapshotApiActivity, "Could not get weather.", Toast.LENGTH_LONG).show()
                    return@ResultCallback
                }

                //parse and display current weather status
                val weather = weatherResult.weather
                val weatherReport = ("Temperature: " + weather.getTemperature(Weather.CELSIUS)
                        + "\nHumidity: " + weather.humidity)
                (findViewById<View>(R.id.weather_status) as TextView).text = weatherReport
            })
    }

    /**
     * Get the nearby places using Snapshot apis. We are going to display only first 5 places to the user in the list.
     */
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    private fun getPlace() {

        Awareness.SnapshotApi.getPlaces(mGoogleApiClient)
            .setResultCallback(ResultCallback { placesResult ->
                if (!placesResult.status.isSuccess) {
                    Toast.makeText(this@SnapshotApiActivity, "Could not get places.", Toast.LENGTH_LONG).show()
                    return@ResultCallback
                }

                //get the list of all like hood places
                val placeLikelihoodList = placesResult.placeLikelihoods

                // Show the top 5 possible location results.
                val linearLayout = findViewById<View>(R.id.current_place_container) as LinearLayout
                linearLayout.removeAllViews()
                if (placeLikelihoodList != null) {
                    var i = 0
                    while (i < 5 && i < placeLikelihoodList.size) {
                        val p = placeLikelihoodList[i]

                        //add place row
                        val v = LayoutInflater.from(this@SnapshotApiActivity)
                            .inflate(R.layout.row_nearby_place, linearLayout, false)
                        (v.findViewById<View>(R.id.place_name) as TextView).text = p.place.name
                        (v.findViewById<View>(R.id.place_address) as TextView).text = p.place.address
                        linearLayout.addView(v)
                        i++
                    }
                } else {
                    Toast.makeText(this@SnapshotApiActivity, "Could not get nearby places.", Toast.LENGTH_LONG).show()
                }
            })
    }

    /**
     * Get user's current location. We are also displaying Google Static map.
     */
    @SuppressLint("MissingPermission")
    @RequiresPermission("android.permission.ACCESS_FINE_LOCATION")
    private fun getLocation() {

        Awareness.SnapshotApi.getLocation(mGoogleApiClient)
            .setResultCallback(ResultCallback { locationResult ->
                if (!locationResult.status.isSuccess) {
                    Toast.makeText(this@SnapshotApiActivity, "Could not get location.", Toast.LENGTH_LONG).show()
                    return@ResultCallback
                }

                //get location
                val location = locationResult.location
                (findViewById<View>(R.id.current_latlng) as TextView).text = location.latitude.toString() + ", " +
                        location.longitude

                //display the time
                val timeTv = findViewById<View>(R.id.latlng_time) as TextView
                val sdf = SimpleDateFormat("h:mm a dd-MM-yyyy", Locale.getDefault())
                timeTv.text = "as on: " + sdf.format(Date(location.time))

                //Load the current map image from Google map
                val url = ("https://maps.googleapis.com/maps/api/staticmap?center="
                        + location.latitude + "," + location.longitude
                        + "&zoom=20&size=400x250&key=" + getString(R.string.api_key))
                Picasso.with(this@SnapshotApiActivity).load(url).into(findViewById<View>(R.id.current_map) as ImageView)
            })
    }

    /**
     * Check weather the headphones are plugged in or not? This is under snapshot api category.
     */
    private fun getHeadphoneStatus() {
        Awareness.SnapshotApi.getHeadphoneState(mGoogleApiClient)
            .setResultCallback(ResultCallback { headphoneStateResult ->
                if (!headphoneStateResult.status.isSuccess) {
                    Toast.makeText(this@SnapshotApiActivity, "Could not get headphone state.", Toast.LENGTH_LONG).show()
                    return@ResultCallback
                }
                val headphoneState = headphoneStateResult.headphoneState

                //display the status
                val headphoneStatusTv = findViewById<View>(R.id.headphone_status) as TextView
                headphoneStatusTv.text =
                        if (headphoneState.state == HeadphoneState.PLUGGED_IN) "Plugged in." else "Unplugged."
            })
    }

    /**
     * Get current activity of the user. This is under snapshot api category.
     * Current activity and confidence level will be displayed on the screen.
     */
    private fun getCurrentActivity() {
        Awareness.SnapshotApi.getDetectedActivity(mGoogleApiClient)
            .setResultCallback(ResultCallback { detectedActivityResult ->
                if (!detectedActivityResult.status.isSuccess) {
                    Toast.makeText(this@SnapshotApiActivity, "Could not get the current activity.", Toast.LENGTH_LONG)
                        .show()
                    return@ResultCallback
                }
                val ar = detectedActivityResult.activityRecognitionResult
                val probableActivity = ar.mostProbableActivity

                //set the activity name
                val activityName = findViewById<View>(R.id.probable_activity_name) as TextView
                when (probableActivity.type) {
                    DetectedActivity.IN_VEHICLE -> activityName.text = "In vehicle"
                    DetectedActivity.ON_BICYCLE -> activityName.text = "On bicycle"
                    DetectedActivity.ON_FOOT -> activityName.text = "On foot"
                    DetectedActivity.RUNNING -> activityName.text = "Running"
                    DetectedActivity.STILL -> activityName.text = "Still"
                    DetectedActivity.TILTING -> activityName.text = "Tilting"
                    DetectedActivity.UNKNOWN -> activityName.text = "Unknown"
                    DetectedActivity.WALKING -> activityName.text = "Walking"
                }

                //set the confidante level
                val confidenceLevel = findViewById<View>(R.id.probable_activity_confidence) as ProgressBar
                confidenceLevel.progress = probableActivity.confidence

                //display the time
                val timeTv = findViewById<View>(R.id.probable_activity_time) as TextView
                val sdf = SimpleDateFormat("h:mm a dd-MM-yyyy", Locale.getDefault())
                timeTv.text = "as on: " + sdf.format(Date(ar.time))
            })
    }

    override fun onConnectionSuspended(i: Int) {
        AlertDialog.Builder(this)
            .setMessage("Cannot connect to google api services.")
            .setPositiveButton(android.R.string.ok) { dialogInterface, i -> finish() }.show()
    }

    companion object {
        private val GET_LOCATION_PERMISSION_REQUEST_CODE = 12345
        private val GET_PLACE_PERMISSION_REQUEST_CODE = 123456
        private val GET_WEATHER_PERMISSION_REQUEST_CODE = 1234567
    }
}
