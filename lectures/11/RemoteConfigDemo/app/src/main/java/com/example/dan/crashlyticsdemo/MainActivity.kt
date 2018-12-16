package com.example.dan.crashlyticsdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Crashlytics.log("app started")
        remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build()
        remoteConfig.setConfigSettings(configSettings)

        remoteConfig.setDefaults(R.xml.remote_config_defaults)

        setContentView(R.layout.activity_main)
        crashButton.setOnClickListener {
            if (remoteConfig.getString(HIDE_CRASH_LOGIC) ==
                remoteConfig.getString(HIDE_CRASH_LOGIC_VALUE)
            ) {
                Crashlytics.getInstance().crash()
            } else {
                Snackbar.make(
                    parent_content,
                    remoteConfig.getString(HIDE_CRASH_NO_LOGIC),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
        fetchValues()
    }

    private fun fetchValues() {
        textView.text = remoteConfig.getString(HELLO_MESSAGE)
        val isUsingDeveloperMode = remoteConfig.info.configSettings.isDeveloperModeEnabled

        // If your app is using developer mode, cacheExpiration is set to 0,
        // so each fetch will retrieve values from the service.
        val cacheExpiration: Long = if (isUsingDeveloperMode) {
            0
        } else {
            3600 // 1 hour in seconds.
        }

        remoteConfig.fetch(cacheExpiration)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this, "Fetch Succeeded",
                        Toast.LENGTH_SHORT
                    ).show()

                    // After config data is successfully fetched,
                    // it must be activated before newly fetched
                    // values are returned.
                    remoteConfig.activateFetched()
                } else {
                    Toast.makeText(
                        this, "Fetch Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                textView.text = remoteConfig.getString(HELLO_MESSAGE)
            }
    }

    companion object {
        private const val HIDE_CRASH_LOGIC = "hide_crash_logic"
        private const val HIDE_CRASH_LOGIC_VALUE = "hide_crash_value"
        private const val HIDE_CRASH_NO_LOGIC = "hide_crash_nologic"
        private const val HELLO_MESSAGE = "hello_message"
    }
}
