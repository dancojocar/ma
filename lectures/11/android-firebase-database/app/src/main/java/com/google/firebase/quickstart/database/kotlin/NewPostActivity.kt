package com.google.firebase.quickstart.database.kotlin

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.databinding.ActivityMainBinding
import com.google.firebase.quickstart.database.databinding.ActivityNewPostBinding
import com.google.firebase.quickstart.database.kotlin.models.Post
import com.google.firebase.quickstart.database.kotlin.models.User
import java.util.*

class NewPostActivity : BaseActivity() {

  private lateinit var database: DatabaseReference
  private lateinit var binding: ActivityNewPostBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityNewPostBinding.inflate(layoutInflater)

    setContentView(binding.root)

    database = FirebaseDatabase.getInstance().reference

    binding.fabSubmitPost.setOnClickListener { submitPost() }
  }

  private fun submitPost() {
    val title = binding.fieldTitle.text.toString()
    val body = binding.fieldBody.text.toString()

    // Title is required
    if (TextUtils.isEmpty(title)) {
      binding.fieldTitle.error = REQUIRED
      return
    }

    // Body is required
    if (TextUtils.isEmpty(body)) {
      binding.fieldBody.error = REQUIRED
      return
    }

    // Disable button so there are no multi-posts
    setEditingEnabled(false)
    Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show()

    val userId = uid
    database.child("users").child(userId).addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          // Get user value
          val user = dataSnapshot.getValue(User::class.java)

          if (user == null) {
            // User is null, error out
            loge("User $userId is unexpectedly null")
            Toast.makeText(
              baseContext,
              "Error: could not fetch user.",
              Toast.LENGTH_SHORT
            ).show()
          } else {
            // Write new post
            writeNewPost(userId, user.username.toString(), title, body)
          }

          // Finish this Activity, back to the stream
          setEditingEnabled(true)
          finish()
        }

        override fun onCancelled(databaseError: DatabaseError) {
          logw("getUser:onCancelled", databaseError.toException())
          setEditingEnabled(true)
        }
      })
  }

  private fun setEditingEnabled(enabled: Boolean) {
    binding.fieldTitle.isEnabled = enabled
    binding.fieldBody.isEnabled = enabled
    if (enabled) {
      binding.fabSubmitPost.show()
    } else {
      binding.fabSubmitPost.hide()
    }
  }

  private fun writeNewPost(userId: String, username: String, title: String, body: String) {
    // Create new post at /user-posts/$userid/$postid and at
    // /posts/$postid simultaneously
    val key = database.child("posts").push().key
    if (key == null) {
      logw("Couldn't get push key for posts")
      return
    }

    val post = Post(userId, username, title, body)
    val postValues = post.toMap()

    val childUpdates = HashMap<String, Any>()
    childUpdates["/posts/$key"] = postValues
    childUpdates["/user-posts/$userId/$key"] = postValues

    database.updateChildren(childUpdates)
  }

  companion object {
    private const val REQUIRED = "Required"
  }
}
