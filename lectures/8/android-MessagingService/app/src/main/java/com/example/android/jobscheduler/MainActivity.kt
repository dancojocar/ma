package com.example.android.jobscheduler

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Messenger
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.example.android.jobscheduler.ui.MainScreen
import com.example.android.jobscheduler.viewmodel.MainViewModel
import com.example.android.jobscheduler.*

/**
 * Schedules and configures jobs to be executed by a [JobScheduler].
 *
 * [MyJobService] can send messages to this via a [Messenger]
 * that is sent in the Intent that starts the Service.
 */
class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  // Handler for incoming messages from the service.
  private lateinit var handler: IncomingMessageHandler
  private lateinit var serviceComponent: ComponentName

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)

    handler = IncomingMessageHandler(viewModel)
    serviceComponent = ComponentName(this, MyJobService::class.java)

    setContent {
      MainScreen(viewModel)
    }
  }

  override fun onStart() {
    super.onStart()
    // Start service and provide it a way to communicate with this class.
    val startServiceIntent = Intent(this, MyJobService::class.java)
    val messengerIncoming = Messenger(handler)
    startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming)
    startService(startServiceIntent)
  }

  override fun onStop() {
    // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
    // and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
    // to stopService() won't prevent scheduled jobs to be processed. However, failing
    // to call stopService() would keep it alive indefinitely.
    stopService(Intent(this, MyJobService::class.java))
    super.onStop()
  }
}

