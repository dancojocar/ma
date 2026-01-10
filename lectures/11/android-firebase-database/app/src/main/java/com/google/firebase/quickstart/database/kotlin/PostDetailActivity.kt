package com.google.firebase.quickstart.database.kotlin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.models.Comment
import com.google.firebase.quickstart.database.kotlin.models.Post
import com.google.firebase.quickstart.database.kotlin.models.User
import com.google.firebase.quickstart.database.ui.theme.FirebaseDatabaseTheme

class PostDetailActivity : ComponentActivity() {

    private lateinit var postKey: String
    private lateinit var postReference: DatabaseReference
    private lateinit var commentsReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        postKey = intent.getStringExtra(EXTRA_POST_KEY) ?: run {
            finish()
            return
        }

        postReference = FirebaseDatabase.getInstance().reference.child("posts").child(postKey)
        commentsReference = FirebaseDatabase.getInstance().reference.child("post-comments").child(postKey)

        setContent {
            FirebaseDatabaseTheme {
                PostDetailScreen(
                    postReference = postReference,
                    commentsReference = commentsReference,
                    onPostComment = { text -> postComment(text) },
                    onBack = { finish() }
                )
            }
        }
    }

    private fun postComment(commentText: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java) ?: return
                    val authorName = user.username
                    val comment = Comment(uid, authorName, commentText)
                    commentsReference.push().setValue(comment)
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    companion object {
        const val EXTRA_POST_KEY = "post_key"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postReference: DatabaseReference,
    commentsReference: DatabaseReference,
    onPostComment: (String) -> Unit,
    onBack: () -> Unit
) {
    var post by remember { mutableStateOf<Post?>(null) }
    val comments = remember { mutableStateListOf<Comment>() }
    var commentText by remember { mutableStateOf("") }
    
    // Load Post
    DisposableEffect(postReference) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                post = snapshot.getValue(Post::class.java)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        postReference.addValueEventListener(listener)
        onDispose { postReference.removeEventListener(listener) }
    }

    // Load Comments
    DisposableEffect(commentsReference) {
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val comment = snapshot.getValue(Comment::class.java)
                if (comment != null) comments.add(comment)
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {} // Comments usually don't change
            override fun onChildRemoved(snapshot: DataSnapshot) {} 
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        commentsReference.addChildEventListener(listener)
        onDispose { commentsReference.removeEventListener(listener) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(post?.title ?: "Post Detail") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                // Header (Post Content)
                item {
                    if (post != null) {
                        PostHeader(post!!)
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                    }
                }
                
                // Comments
                items(comments) { comment ->
                    CommentItem(comment)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Comment Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Write a comment...") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onPostComment(commentText)
                            commentText = ""
                        }
                    },
                    enabled = commentText.isNotBlank()
                ) {
                    Text("Post")
                }
            }
        }
    }
}

@Composable
fun PostHeader(post: Post) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = post.author ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = post.title ?: "",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = post.body ?: "",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = comment.author ?: "",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = comment.text ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
