package com.google.firebase.quickstart.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.quickstart.auth.ui.theme.FirebaseAuthTheme

class ChooserActivity : ComponentActivity() {

  private val collectedProviders = mutableStateListOf<String>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      FirebaseAuthTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          ChooserScreen(collectedProviders)
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    checkLoginBadges()
  }

  private fun checkLoginBadges() {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    val prefs = getSharedPreferences("AuthGamePrefs", Context.MODE_PRIVATE)
    val editor = prefs.edit()

    // If logged in, unlock the provider
    user?.providerData?.forEach { profile ->
      val providerId = profile.providerId
      // Map generic providers to our specific tracking IDs/Classes if needed
      // For now, we'll store the class names we know map to these, or just generic IDs.
      // Simplified logic: If user is logged in, mark the "current" active activity's type?
      // Better: just check if *any* user is logged in, and if so, try to map the provider ID.
      // Even better: The student just finished an activity. We can assume if they are back here and auth.currentUser != null, they succeeded.
      // But checking providerId is more robust.

      // Mapping Firebase Provider IDs to our "Game Badges"
      // google.com -> GoogleSignInActivity
      // facebook.com -> FacebookLoginActivity
      // password -> EmailPasswordActivity
      // phone -> PhoneAuthActivity
      // firebase -> AnonymousAuthActivity (isAnonymous check)

      if (providerId == "google.com") unlockBadge(
        GoogleSignInActivity::class.java.simpleName,
        editor
      )
      if (providerId == "password") unlockBadge(
        EmailPasswordActivity::class.java.simpleName,
        editor
      )
      if (providerId == "phone") unlockBadge(PhoneAuthActivity::class.java.simpleName, editor)
    }

    if (user?.isAnonymous == true) {
      unlockBadge(AnonymousAuthActivity::class.java.simpleName, editor)
    }

    // Load all unlocked badges
    collectedProviders.clear()
    prefs.all.keys.forEach { key ->
      if (prefs.getBoolean(key, false)) {
        collectedProviders.add(key)
      }
    }
  }

  private fun unlockBadge(key: String, editor: android.content.SharedPreferences.Editor) {
    editor.putBoolean(key, true)
    editor.apply()
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun ChooserScreen(unlockedBadges: List<String>) {
    val context = LocalContext.current
    val classes = listOf(
      GoogleSignInActivity::class.java to R.string.desc_google_sign_in,
      EmailPasswordActivity::class.java to R.string.desc_emailpassword,
      PasswordlessActivity::class.java to R.string.desc_passwordless,
      PhoneAuthActivity::class.java to R.string.desc_phone_auth,
      AnonymousAuthActivity::class.java to R.string.desc_anonymous_auth,
      FirebaseUIActivity::class.java to R.string.desc_firebase_ui,
      CustomAuthActivity::class.java to R.string.desc_custom_auth
    )

    val totalBadges = classes.size
    val unlockedCount =
      unlockedBadges.count { badge -> classes.any { it.first.simpleName == badge } } // Simple overlap check
    val progress = if (totalBadges > 0) unlockedCount.toFloat() / totalBadges else 0f

    Scaffold(
      topBar = {
        CenterAlignedTopAppBar(
          title = { Text("Auth Collector") }
        )
      }
    ) { innerPadding ->
      Column(modifier = Modifier.padding(innerPadding)) {

        // Game Status Header
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Agent Status: ${getRank(unlockedCount)}",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Badges: $unlockedCount / $totalBadges")
            LinearProgressIndicator(
              progress = { progress },
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(8.dp),
            )
          }
        }

        // Mission Grid
        LazyVerticalGrid(
          columns = GridCells.Fixed(2),
          contentPadding = PaddingValues(16.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(classes) { (clazz, descId) ->
            val isUnlocked = unlockedBadges.contains(clazz.simpleName)
            ActivityItem(
              name = clazz.simpleName.replace("Activity", ""),
              description = stringResource(descId),
              isUnlocked = isUnlocked,
              onClick = {
                startActivity(Intent(context, clazz))
              }
            )
          }
        }
      }
    }
  }

  private fun getRank(count: Int): String {
    return when {
      count == 0 -> "Rookie"
      count < 3 -> "Apprentice"
      count < 5 -> "Specialist"
      count < 7 -> "Expert"
      else -> "Master of Auth"
    }
  }

  @Composable
  fun ActivityItem(name: String, description: String, isUnlocked: Boolean, onClick: () -> Unit) {
    val containerColor =
      if (isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor =
      if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .clickable(onClick = onClick),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
      colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
      Box(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
              contentDescription = null,
              tint = if (isUnlocked) Color(0xFF4CAF50) else Color.Gray, // Green check or gray lock
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
              text = name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = contentColor
            )
          }
          Spacer(modifier = Modifier.size(8.dp))
          Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            maxLines = 4,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}
