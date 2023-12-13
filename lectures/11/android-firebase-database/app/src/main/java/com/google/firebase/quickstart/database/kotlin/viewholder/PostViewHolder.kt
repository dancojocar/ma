package com.google.firebase.quickstart.database.kotlin.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.models.Post
import com.google.firebase.quickstart.database.databinding.ItemPostBinding

class PostViewHolder(itemView: ItemPostBinding) : RecyclerView.ViewHolder(itemView.root) {
  var binding: ItemPostBinding = itemView

  fun bindToPost(post: Post, starClickListener: View.OnClickListener) {
    binding.includePostTextLayout.postTitle.text = post.title
    binding.postAuthorLayout.postAuthor.text = post.author
    binding.postNumStars.text = post.starCount.toString()
    binding.includePostTextLayout.postBody.text = post.body

    binding.star.setOnClickListener(starClickListener)
  }

  fun setLikedState(liked: Boolean) {
    if (liked) {
      binding.star.setImageResource(R.drawable.ic_toggle_star_24)
    } else {
      binding.star.setImageResource(R.drawable.ic_toggle_star_outline_24)
    }
  }
}
