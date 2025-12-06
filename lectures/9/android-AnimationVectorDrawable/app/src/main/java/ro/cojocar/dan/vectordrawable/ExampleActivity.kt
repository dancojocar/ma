package ro.cojocar.dan.vectordrawable

import android.app.Activity
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import ro.cojocar.dan.vectordrawable.databinding.ActivityExampleBinding

class ExampleActivity : Activity() {
  private lateinit var binding: ActivityExampleBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    binding = ActivityExampleBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
      val insets = windowInsets.getInsets(
        WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
      )
      v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
      WindowInsetsCompat.CONSUMED
    }
    val container = binding.container
    container.setOnClickListener {
      for (i in 0 until container.childCount) {
        animateDrawables(container.getChildAt(i))
      }
    }
    for (i in 0 until container.childCount) {
      val child = container.getChildAt(i)
      if (child is TextView) {
        child.setOnClickListener {
          for (j in 0 until container.childCount) {
            animateDrawables(container.getChildAt(i))
          }
        }
      }
    }
  }

  private fun animateDrawables(view: View) {
    if (view !is TextView) {
      return
    }
    for (drawable in view.compoundDrawables) {
      if (drawable is Animatable) {
        (drawable as Animatable).start()
      }
    }
  }
}