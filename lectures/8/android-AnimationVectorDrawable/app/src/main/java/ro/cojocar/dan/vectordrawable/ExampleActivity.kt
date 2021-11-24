package ro.cojocar.dan.vectordrawable

import android.app.Activity
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import ro.cojocar.dan.vectordrawable.databinding.ActivityExampleBinding

class ExampleActivity : Activity() {
  private lateinit var binding: ActivityExampleBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityExampleBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

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