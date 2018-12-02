package ro.cojocar.dan.vectordrawable

import android.app.Activity
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_example.*

class ExampleActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

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