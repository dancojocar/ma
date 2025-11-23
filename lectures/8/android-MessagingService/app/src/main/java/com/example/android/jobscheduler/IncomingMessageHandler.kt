package com.example.android.jobscheduler

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.example.android.jobscheduler.viewmodel.MainViewModel
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import com.example.android.jobscheduler.*

/**
 * A [Handler] allows you to send messages associated with a thread. A [Messenger]
 * uses this handler to communicate from [MyJobService]. It's also used to make
 * the start and stop views blink for a short period of time.
 */
internal class IncomingMessageHandler(viewModel: MainViewModel) : Handler(Looper.getMainLooper()) {

    // Prevent possible leaks with a weak reference.
    private val viewModel: WeakReference<MainViewModel> = WeakReference(viewModel)

    override fun handleMessage(msg: Message) {
        val viewModel = viewModel.get() ?: return
        when (msg.what) {
            /*
             * Receives callback from the service when a job has landed
             * on the app. Turns on indicator and sends a message to turn it off after
             * a second.
             */
            MSG_COLOR_START -> {
                viewModel.onJobStarted(msg.obj as Int)
                sendMessageDelayed(
                    Message.obtain(this, MSG_UNCOLOR_START),
                    TimeUnit.SECONDS.toMillis(1)
                )
            }
            /*
             * Receives callback from the service when a job that previously landed on the
             * app must stop executing. Turns on indicator and sends a message to turn it
             * off after two seconds.
             */
            MSG_COLOR_STOP -> {
                viewModel.onJobStopped(msg.obj as Int)
                sendMessageDelayed(obtainMessage(MSG_UNCOLOR_STOP), TimeUnit.SECONDS.toMillis(1))
            }
            MSG_UNCOLOR_START -> {
                viewModel.onJobStarted(null)
            }
            MSG_UNCOLOR_STOP -> {
                viewModel.onJobStopped(null)
            }
        }
    }
}


