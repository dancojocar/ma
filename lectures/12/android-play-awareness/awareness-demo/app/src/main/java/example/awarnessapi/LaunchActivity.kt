package example.awarnessapi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View

/**
 * Tutorial : 'https://inthecheesefactory.com/blog/google-awareness-api-in-action/en'
 */
class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        //snap shot api demo
        findViewById<View>(R.id.snap_shot_api_demo).setOnClickListener {
            startActivity(
                Intent(
                    this@LaunchActivity,
                    SnapshotApiActivity::class.java
                )
            )
        }

        //fence api demo
        findViewById<View>(R.id.headphone_fence_api_demo).setOnClickListener {
            startActivity(
                Intent(
                    this@LaunchActivity,
                    HeadphoneFenceApiActivity::class.java
                )
            )
        }

        //activity recognition fence api demo
        findViewById<View>(R.id.activity_fence_api_demo).setOnClickListener {
            startActivity(
                Intent(
                    this@LaunchActivity,
                    ActivityFenceApiDemo::class.java
                )
            )
        }

        //combine fence api demo
        findViewById<View>(R.id.combine_fence_api_demo).setOnClickListener {
            startActivity(
                Intent(
                    this@LaunchActivity,
                    CombineFenceApiActivity::class.java
                )
            )
        }
    }
}
