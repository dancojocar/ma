package com.google.firebase.quickstart.database.kotlin

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.models.Post
import com.google.firebase.quickstart.database.kotlin.models.User
import com.google.firebase.quickstart.database.ui.theme.FirebaseDatabaseTheme

class NewPostActivity : ComponentActivity() {

  private lateinit var database: DatabaseReference
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    database = FirebaseDatabase.getInstance().reference

    setContent {
      FirebaseDatabaseTheme {
        NewPostScreen(
          onSubmit = { title, body -> submitPost(title, body) }
        )
      }
    }
  }

  private fun submitPost(title: String, body: String) {
    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    if (uid == null) {
      // Should not happen if logged in
      Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
      return
    }

    Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show()

    database.child("users").child(uid).addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          val user = dataSnapshot.getValue(User::class.java)

          if (user == null) {
            Toast.makeText(baseContext, "Error: could not fetch user.", Toast.LENGTH_SHORT).show()
          } else {
            writeNewPost(uid, user.username.toString(), title, body)
          }
          finish()
        }

        override fun onCancelled(databaseError: DatabaseError) {
          Toast.makeText(baseContext, "Error: " + databaseError.message, Toast.LENGTH_SHORT).show()
        }
      })
  }

  private fun writeNewPost(userId: String, username: String, title: String, body: String) {
    val key = database.child("posts").push().key ?: return
    val post = Post(userId, username, title, body)
    val postValues = post.toMap()

    val childUpdates = HashMap<String, Any>()
    childUpdates["/posts/$key"] = postValues
    childUpdates["/user-posts/$userId/$key"] = postValues

    database.updateChildren(childUpdates)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPostScreen(onSubmit: (String, String) -> Unit) {
  var title by remember { mutableStateOf("") }
  var body by remember { mutableStateOf("") }
  var isSubmitting by remember { mutableStateOf(false) } // Local UI state

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("New Post") },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    },
    floatingActionButton = {
      if (!isSubmitting) {
        FloatingActionButton(onClick = {
          if (TextUtils.isEmpty(title)) return@FloatingActionButton // or show error
          if (TextUtils.isEmpty(body)) return@FloatingActionButton

          isSubmitting = true
          onSubmit(title, body)
        }) {
          Icon(Icons.Filled.Check, contentDescription = "Submit")
        }
      }
    }
  ) { innerPadding ->
    Box(modifier = Modifier
      .padding(innerPadding)
      .fillMaxSize()) {
      Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = body,
          onValueChange = { body = it },
          label = { Text("Body") },
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
        )
      }

      if (isSubmitting) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      }
    }
  }
}
