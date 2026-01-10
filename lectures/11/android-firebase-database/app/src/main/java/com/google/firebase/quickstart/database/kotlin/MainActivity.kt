package com.google.firebase.quickstart.database.kotlin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Query
import com.google.firebase.database.Transaction
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.models.Post
import com.google.firebase.quickstart.database.ui.theme.FirebaseDatabaseTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  private lateinit var auth: FirebaseAuth
  private lateinit var database: DatabaseReference

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    auth = FirebaseAuth.getInstance()
    database = FirebaseDatabase.getInstance().reference

    setContent {
      FirebaseDatabaseTheme {
        MainScreen(
          onNewPost = { startActivity(Intent(this, NewPostActivity::class.java)) },
          onLogout = {
            auth.signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
          },
          database = database,
          uid = auth.currentUser?.uid ?: ""
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
  onNewPost: () -> Unit,
  onLogout: () -> Unit,
  database: DatabaseReference,
  uid: String
) {
  val pagerState = rememberPagerState(pageCount = { 3 })
  val coroutineScope = rememberCoroutineScope()
  val titles = listOf(
    stringResource(R.string.heading_recent),
    stringResource(R.string.heading_my_posts),
    stringResource(R.string.heading_my_top_posts)
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
          IconButton(onClick = onLogout) {
            Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary,
          actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onNewPost) {
        Icon(Icons.Filled.Add, contentDescription = "New Post")
      }
    }
  ) { innerPadding ->
    Column(modifier = Modifier.padding(innerPadding)) {
      TabRow(selectedTabIndex = pagerState.currentPage) {
        titles.forEachIndexed { index, title ->
          Tab(
            selected = pagerState.currentPage == index,
            onClick = {
              coroutineScope.launch { pagerState.animateScrollToPage(index) }
            },
            text = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
          )
        }
      }

      HorizontalPager(state = pagerState) { page ->
        val query = when (page) {
          0 -> database.child("posts").limitToFirst(100)
          1 -> database.child("user-posts").child(uid)
          2 -> database.child("user-posts").child(uid).orderByChild("starCount")
          else -> database.child("posts").limitToFirst(100)
        }

        PostList(query = query, database = database, uid = uid)
      }
    }
  }
}

@Composable
fun PostList(query: Query, database: DatabaseReference, uid: String) {
  val context = LocalContext.current
  val posts = remember { mutableStateListOf<Pair<String, Post>>() }
  var isLoading by remember { mutableStateOf(true) }

  DisposableEffect(query) {
    val listener = object : ChildEventListener {
      override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
        val post = snapshot.getValue(Post::class.java)
        if (post != null) {
          posts.add(snapshot.key!! to post)
          isLoading = false
        }
      }

      override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        val key = snapshot.key
        val newPost = snapshot.getValue(Post::class.java)
        val index = posts.indexOfFirst { it.first == key }
        if (index >= 0 && newPost != null) {
          posts[index] = key!! to newPost
        }
      }

      override fun onChildRemoved(snapshot: DataSnapshot) {
        val key = snapshot.key
        posts.removeIf { it.first == key }
      }

      override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        // Ignore for now or implement if strictly needed (reordering)
      }

      override fun onCancelled(error: DatabaseError) {
        isLoading = false
      }
    }

    query.addChildEventListener(listener)
    // Also add a value listener just to turn off loading if empty
    query.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        if (!snapshot.exists() || snapshot.childrenCount == 0L) isLoading = false
      }

      override fun onCancelled(error: DatabaseError) {
        isLoading = false
      }
    })

    onDispose {
      query.removeEventListener(listener)
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    if (isLoading && posts.isEmpty()) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    } else {
      // Replicating reverseLayout=true, stackFromEnd=true behavior of original fragment
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp), // Space for FAB
        reverseLayout = true // Important matching original!
      ) {
        items(posts) { (key, post) ->
          PostItem(
            post = post,
            uid = uid,
            onPostClick = {
              val intent = Intent(context, PostDetailActivity::class.java)
              intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, key)
              context.startActivity(intent)
            },
            onStarClick = {
              val globalPostRef = database.child("posts").child(key)
              val userPostRef = database.child("user-posts").child(post.uid ?: "").child(key)
              onStarClicked(globalPostRef, uid)
              onStarClicked(userPostRef, uid)
            }
          )
        }
      }
    }
  }
}

fun onStarClicked(postRef: DatabaseReference, uid: String) {
  postRef.runTransaction(object : Transaction.Handler {
    override fun doTransaction(mutableData: MutableData): Transaction.Result {
      val p = mutableData.getValue(Post::class.java) ?: return Transaction.success(mutableData)

      if (p.stars.containsKey(uid)) {
        p.starCount = p.starCount - 1
        p.stars.remove(uid)
      } else {
        p.starCount = p.starCount + 1
        p.stars[uid] = true
      }

      mutableData.value = p
      return Transaction.success(mutableData)
    }

    override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
      // Transaction completed
    }
  })
}

@Composable
fun PostItem(
  post: Post,
  uid: String,
  onPostClick: () -> Unit,
  onStarClick: () -> Unit
) {
  val isLiked = post.stars.containsKey(uid)
  val starIcon = if (isLiked) Icons.Filled.Star else Icons.Outlined.StarBorder
  val starColor =
    if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .clickable(onClick = onPostClick),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      // Author
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.AccountCircle,
          contentDescription = "Author Photo",
          tint = Color.Gray,
          modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = post.author ?: "",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )

        // Stars
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable(onClick = onStarClick)
        ) {
          Icon(
            imageVector = starIcon,
            contentDescription = "Star",
            tint = starColor
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = post.starCount.toString())
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Title & Body
      Text(
        text = post.title ?: "",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = post.body ?: "",
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
