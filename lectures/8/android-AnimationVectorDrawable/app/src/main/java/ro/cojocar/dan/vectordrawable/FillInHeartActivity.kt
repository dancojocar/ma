package ro.cojocar.dan.vectordrawable

import android.app.Activity
import android.os.Bundle
import ro.cojocar.dan.vectordrawable.databinding.ActivityFillInHeartBinding

class FillInHeartActivity : Activity() {
  private lateinit var binding: ActivityFillInHeartBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityFillInHeartBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
  }
}