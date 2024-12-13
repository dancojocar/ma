package com.google.firebase.quickstart.database.kotlin.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.quickstart.database.databinding.FragmentAllPostsBinding
import com.google.firebase.quickstart.database.databinding.ItemPostBinding
import com.google.firebase.quickstart.database.kotlin.PostDetailActivity
import com.google.firebase.quickstart.database.kotlin.logd
import com.google.firebase.quickstart.database.kotlin.models.Post
import com.google.firebase.quickstart.database.kotlin.viewholder.PostViewHolder

abstract class PostListFragment : Fragment() {

  private lateinit var database: DatabaseReference

  private lateinit var manager: LinearLayoutManager
  private var adapter: FirebaseRecyclerAdapter<Post, PostViewHolder>? = null
  private var _binding: FragmentAllPostsBinding? = null
  private val binding get() = _binding!!

  val uid: String
    get() = FirebaseAuth.getInstance().currentUser!!.uid

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    super.onCreateView(inflater, container, savedInstanceState)
    _binding = FragmentAllPostsBinding.inflate(inflater, container, false)
    val view = binding.root

    database = FirebaseDatabase.getInstance().reference

    binding.messagesList.setHasFixedSize(false)

    return view
  }

  @Deprecated("Deprecated in Java")
  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    // Set up Layout Manager, reverse layout
    manager = LinearLayoutManager(activity)
    manager.reverseLayout = true
    manager.stackFromEnd = true
    
    // Disable predictive animations and item changes animations
    binding.messagesList.apply {
      layoutManager = manager
      itemAnimator = null
      setHasFixedSize(true)
    }

    // Set up FirebaseRecyclerAdapter with the Query
    val postsQuery = getQuery(database)

    val options = FirebaseRecyclerOptions.Builder<Post>()
      .setQuery(postsQuery, Post::class.java)
      .build()

    adapter = object : FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {
      override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PostViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding = ItemPostBinding.inflate(inflater, viewGroup, false)
        return PostViewHolder(binding)
      }

      override fun onBindViewHolder(viewHolder: PostViewHolder, position: Int, model: Post) {
        try {
          if (position >= itemCount) return  // Prevent index out of bounds
          
          val postRef = getRef(position)
          val postKey = postRef.key

          // Set click listener for the whole post view
          viewHolder.itemView.setOnClickListener {
            // Launch PostDetailActivity
            val intent = Intent(activity, PostDetailActivity::class.java)
            intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey)
            startActivity(intent)
          }

          // Determine if the current user has liked this post and set UI accordingly
          viewHolder.setLikedState(model.stars?.containsKey(uid) ?: false)

          // Bind Post to ViewHolder, setting OnClickListener for the star button
          viewHolder.bindToPost(model) {
            // Need to write to both places the post is stored
            postKey?.let { key ->
              val globalPostRef = database.child("posts").child(key)
              val userPostRef = database.child("user-posts").child(model.uid ?: "").child(key)

              // Run two transactions
              onStarClicked(globalPostRef)
              onStarClicked(userPostRef)
            }
          }
        } catch (e: Exception) {
          logd("Error binding view holder: ${e.message}")
        }
      }

      override fun onDataChanged() {
        super.onDataChanged()
        // Handle empty adapter
        if (itemCount == 0) {
          binding.messagesList.visibility = View.GONE
          // You might want to show an empty state view here
        } else {
          binding.messagesList.visibility = View.VISIBLE
        }
      }
    }

    // Initialize RecyclerView with the adapter
    binding.messagesList.adapter = adapter
  }

  private fun onStarClicked(postRef: DatabaseReference) {
    postRef.runTransaction(object : Transaction.Handler {
      override fun doTransaction(mutableData: MutableData): Transaction.Result {
        val p = mutableData.getValue(Post::class.java)
          ?: return Transaction.success(mutableData)

        if (p.stars.containsKey(uid)) {
          // Unstar the post and remove self from stars
          p.starCount = p.starCount - 1
          p.stars.remove(uid)
        } else {
          // Star the post and add self to stars
          p.starCount = p.starCount + 1
          p.stars[uid] = true
        }

        // Set value and report transaction success
        mutableData.value = p
        return Transaction.success(mutableData)
      }

      override fun onComplete(
        databaseError: DatabaseError?,
        b: Boolean,
        dataSnapshot: DataSnapshot?
      ) {
        // Transaction completed
        logd("postTransaction:onComplete: $databaseError")
      }
    })
  }

  override fun onResume() {
    super.onResume()
    adapter?.startListening()
  }

  override fun onPause() {
    super.onPause()
    adapter?.stopListening()
  }

  abstract fun getQuery(databaseReference: DatabaseReference): Query
}
