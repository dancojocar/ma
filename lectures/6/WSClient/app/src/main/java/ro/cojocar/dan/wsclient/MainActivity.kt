package ro.cojocar.dan.wsclient

import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // ws test
        val client = OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("ws://10.0.2.2:3000")
            .build()
        val wsListener = EchoWebSocketListener(output)
        client.newWebSocket(request, wsListener) // this provide to make 'Open ws connection'

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown()

        button.setOnClickListener {
            hideKeyboard()
            wsListener.send(inputMessage.text.toString())
        }
    }

    fun hideKeyboard() {
        val inputMethodManager = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().windowToken, 0)
    }
}


private class EchoWebSocketListener(val tv: TextView) : WebSocketListener() {
    lateinit var webSocket: WebSocket
    override fun onOpen(webSocket: WebSocket, response: Response) {
        this.webSocket = webSocket
        webSocket.send("Hello, there!")
        webSocket.send("What's up?")
        webSocket.send(ByteString.decodeHex("deadbeef"))
    }

    override fun onMessage(webSocket: WebSocket?, text: String?) {
        logd("Receiving : ${text!!}")
        GlobalScope.launch(Dispatchers.Main) {
            tv.text = "${tv.text}\n $text"
        }
    }

    override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
        logd("Receiving bytes : ${bytes!!.hex()}")
    }

    override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
        webSocket!!.close(NORMAL_CLOSURE_STATUS, null)
        logd("Closing : $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logd("Error : ${t.message}", t)
    }

    fun send(message: String) {
        webSocket.send(message)
    }

    fun close() {
        webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye!")
    }

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
    }
}
