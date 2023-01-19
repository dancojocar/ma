package com.google.location.nearby.apps.rockpaperscissors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import java.nio.charset.StandardCharsets

/** Activity controlling the Rock Paper Scissors game  */
class MainActivity : AppCompatActivity() {
  private enum class GameChoice {
    ROCK, PAPER, SCISSORS;

    fun beats(other: GameChoice?): Boolean {
      return (this == ROCK && other == SCISSORS
          || this == SCISSORS && other == PAPER
          || this == PAPER && other == ROCK)
    }
  }

  // Our handle to Nearby Connections
  private var connectionsClient: ConnectionsClient? = null
  // Our randomly generated name
  private val codeName = CodenameGenerator.generate()
  private var opponentEndpointId: String? = null
  private var opponentName: String? = null
  private var opponentScore = 0
  private var opponentChoice: GameChoice? = null
  private var myScore = 0
  private var myChoice: GameChoice? = null
  private var findOpponentButton: Button? = null
  private var disconnectButton: Button? = null
  private var rockButton: Button? = null
  private var paperButton: Button? = null
  private var scissorsButton: Button? = null
  private var opponentText: TextView? = null
  private var statusText: TextView? = null
  private var scoreText: TextView? = null
  // Callbacks for receiving payloads
  private val payloadCallback: PayloadCallback = object : PayloadCallback() {
    override fun onPayloadReceived(endpointId: String, payload: Payload) {
      opponentChoice = GameChoice.valueOf(String(payload.asBytes()!!, StandardCharsets.UTF_8))
    }

    override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
      if (update.status == PayloadTransferUpdate.Status.SUCCESS && myChoice != null && opponentChoice != null) {
        finishRound()
      }
    }
  }
  // Callbacks for finding other devices
  private val endpointDiscoveryCallback: EndpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
    override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
      Log.i(TAG, "onEndpointFound: endpoint found, connecting")
      connectionsClient!!.requestConnection(codeName, endpointId, connectionLifecycleCallback)
    }

    override fun onEndpointLost(endpointId: String) {}
  }
  // Callbacks for connections to other devices
  private val connectionLifecycleCallback: ConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
    override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
      Log.i(TAG, "onConnectionInitiated: accepting connection")
      connectionsClient!!.acceptConnection(endpointId, payloadCallback)
      opponentName = connectionInfo.endpointName
    }

    override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
      if (result.status.isSuccess) {
        Log.i(TAG, "onConnectionResult: connection successful")
        connectionsClient!!.stopDiscovery()
        connectionsClient!!.stopAdvertising()
        opponentEndpointId = endpointId
        setOpponentName(opponentName)
        setStatusText(getString(R.string.status_connected))
        setButtonState(true)
      } else {
        Log.i(TAG, "onConnectionResult: connection failed")
      }
    }

    override fun onDisconnected(endpointId: String) {
      Log.i(TAG, "onDisconnected: disconnected from the opponent")
      resetGame()
    }
  }

  override fun onCreate(bundle: Bundle?) {
    super.onCreate(bundle)
    setContentView(R.layout.activity_main)
    findOpponentButton = findViewById(R.id.find_opponent)
    disconnectButton = findViewById(R.id.disconnect)
    rockButton = findViewById(R.id.rock)
    paperButton = findViewById(R.id.paper)
    scissorsButton = findViewById(R.id.scissors)
    opponentText = findViewById(R.id.opponent_name)
    statusText = findViewById(R.id.status)
    scoreText = findViewById(R.id.score)
    val nameView = findViewById<TextView>(R.id.name)
    nameView.text = getString(R.string.codename, codeName)
    connectionsClient = Nearby.getConnectionsClient(this)
    resetGame()
  }

  override fun onStart() {
    super.onStart()
    if (!hasPermissions(this, *REQUIRED_PERMISSIONS)) {
      requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS)
    }
  }

  override fun onStop() {
    connectionsClient!!.stopAllEndpoints()
    resetGame()
    super.onStop()
  }

  /** Handles user acceptance (or denial) of our permission request.  */
  @CallSuper
  override fun onRequestPermissionsResult(
      requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
      return
    }
    for (grantResult in grantResults) {
      if (grantResult == PackageManager.PERMISSION_DENIED) {
        Toast.makeText(this, R.string.error_missing_permissions, Toast.LENGTH_LONG).show()
        finish()
        return
      }
    }
    recreate()
  }

  /** Finds an opponent to play the game with using Nearby Connections.  */
  fun findOpponent(view: View?) {
    startAdvertising()
    startDiscovery()
    setStatusText(getString(R.string.status_searching))
    findOpponentButton!!.isEnabled = false
  }

  /** Disconnects from the opponent and reset the UI.  */
  fun disconnect(view: View?) {
    connectionsClient!!.disconnectFromEndpoint(opponentEndpointId!!)
    resetGame()
  }

  /** Sends a [GameChoice] to the other player.  */
  fun makeMove(view: View) {
    if (view.id == R.id.rock) {
      sendGameChoice(GameChoice.ROCK)
    } else if (view.id == R.id.paper) {
      sendGameChoice(GameChoice.PAPER)
    } else if (view.id == R.id.scissors) {
      sendGameChoice(GameChoice.SCISSORS)
    }
  }

  /** Starts looking for other players using Nearby Connections.  */
  private fun startDiscovery() { // Note: Discovery may fail. To keep this demo simple, we don't handle failures.
    connectionsClient!!.startDiscovery(
        packageName, endpointDiscoveryCallback,
        DiscoveryOptions.Builder().setStrategy(STRATEGY).build())
  }

  /** Broadcasts our presence using Nearby Connections so other players can find us.  */
  private fun startAdvertising() { // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
    connectionsClient!!.startAdvertising(
        codeName, packageName, connectionLifecycleCallback,
        AdvertisingOptions.Builder().setStrategy(STRATEGY).build())
  }

  /** Wipes all game state and updates the UI accordingly.  */
  private fun resetGame() {
    opponentEndpointId = null
    opponentName = null
    opponentChoice = null
    opponentScore = 0
    myChoice = null
    myScore = 0
    setOpponentName(getString(R.string.no_opponent))
    setStatusText(getString(R.string.status_disconnected))
    updateScore(myScore, opponentScore)
    setButtonState(false)
  }

  /** Sends the user's selection of rock, paper, or scissors to the opponent.  */
  private fun sendGameChoice(choice: GameChoice) {
    myChoice = choice
    connectionsClient!!.sendPayload(
        opponentEndpointId!!, Payload.fromBytes(choice.name.toByteArray(StandardCharsets.UTF_8)))
    setStatusText(getString(R.string.game_choice, choice.name))
    // No changing your mind!
    setGameChoicesEnabled(false)
  }

  /** Determines the winner and update game state/UI after both players have chosen.  */
  private fun finishRound() {
    when {
      myChoice!!.beats(opponentChoice) -> { // Win!
        setStatusText(getString(R.string.win_message, myChoice!!.name, opponentChoice!!.name))
        myScore++
      }
      myChoice == opponentChoice -> { // Tie, same choice by both players
        setStatusText(getString(R.string.tie_message, myChoice!!.name))
      }
      else -> { // Loss
        setStatusText(getString(R.string.loss_message, myChoice!!.name, opponentChoice!!.name))
        opponentScore++
      }
    }
    myChoice = null
    opponentChoice = null
    updateScore(myScore, opponentScore)
    // Ready for another round
    setGameChoicesEnabled(true)
  }

  /** Enables/disables buttons depending on the connection status.  */
  private fun setButtonState(connected: Boolean) {
    findOpponentButton!!.isEnabled = true
    findOpponentButton!!.visibility = if (connected) View.GONE else View.VISIBLE
    disconnectButton!!.visibility = if (connected) View.VISIBLE else View.GONE
    setGameChoicesEnabled(connected)
  }

  /** Enables/disables the rock, paper, and scissors buttons.  */
  private fun setGameChoicesEnabled(enabled: Boolean) {
    rockButton!!.isEnabled = enabled
    paperButton!!.isEnabled = enabled
    scissorsButton!!.isEnabled = enabled
  }

  /** Shows a status message to the user.  */
  private fun setStatusText(text: String) {
    statusText!!.text = text
  }

  /** Updates the opponent name on the UI.  */
  private fun setOpponentName(opponentName: String?) {
    opponentText!!.text = getString(R.string.opponent_name, opponentName)
  }

  /** Updates the running score ticker.  */
  private fun updateScore(myScore: Int, opponentScore: Int) {
    scoreText!!.text = getString(R.string.game_score, myScore, opponentScore)
  }

  companion object {
    private const val TAG = "RockPaperScissors"
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION)
    private const val REQUEST_CODE_REQUIRED_PERMISSIONS = 1
    private val STRATEGY = Strategy.P2P_STAR
    /** Returns true if the app was granted all the permissions. Otherwise, returns false.  */
    private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
      for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(context, permission)
            != PackageManager.PERMISSION_GRANTED) {
          return false
        }
      }
      return true
    }
  }
}