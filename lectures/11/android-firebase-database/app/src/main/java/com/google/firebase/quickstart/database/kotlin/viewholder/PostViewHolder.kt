package com.google.firebase.quickstart.database.kotlin.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.models.Post
import com.google.firebase.quickstart.database.databinding.ItemPostBinding

class PostViewHolder(itemView: ItemPostBinding) : RecyclerView.ViewHolder(itemView.root) {
  var binding: ItemPostBinding = itemView

  fun bindToPost(post: Post?, starClickListener: View.OnClickListener) {
    post?.let { safePost ->
      binding.includePostTextLayout.postTitle.text = safePost.title.orEmpty()
      binding.postAuthorLayout.postAuthor.text = safePost.author.orEmpty()
      binding.postNumStars.text = safePost.starCount?.toString() ?: "0"
      binding.includePostTextLayout.postBody.text = safePost.body.orEmpty()

      binding.star.setOnClickListener(starClickListener)
    } ?: run {
      binding.includePostTextLayout.postTitle.text = ""
      binding.postAuthorLayout.postAuthor.text = ""
      binding.postNumStars.text = "0"
      binding.includePostTextLayout.postBody.text = ""
      binding.star.setOnClickListener(null)
    }
  }

  fun setLikedState(liked: Boolean) {
    val resourceId = if (liked) {
      R.drawable.ic_toggle_star_24
    } else {
      R.drawable.ic_toggle_star_outline_24
    }
    binding.star.setImageResource(resourceId)
  }
}
