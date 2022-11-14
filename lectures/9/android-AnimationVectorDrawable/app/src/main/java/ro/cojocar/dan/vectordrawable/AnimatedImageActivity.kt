package ro.cojocar.dan.vectordrawable

import android.app.Activity
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.widget.ImageView

abstract class AnimatedImageActivity : Activity() {
  private var imageView: ImageView? = null

  protected abstract val layoutId: Int

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(layoutId)
    imageView = findViewById<View>(R.id.image) as ImageView
    imageView!!.setOnClickListener { animate() }
  }

  private fun animate() {
    val drawable = imageView!!.drawable
    if (drawable is Animatable) {
      (drawable as Animatable).start()
    }
  }
}