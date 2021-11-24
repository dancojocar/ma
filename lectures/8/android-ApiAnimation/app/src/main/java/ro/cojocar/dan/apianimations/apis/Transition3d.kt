package ro.cojocar.dan.apianimations.apis

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter

import ro.cojocar.dan.apianimations.R
import ro.cojocar.dan.apianimations.databinding.AnimationsMainScreenBinding

/**
 * This sample application shows how to use layout animation and various
 * transformations on views. The result is a 3D transition between a
 * ListView and an ImageView. When the user clicks the list, it flips to
 * show the picture. When the user clicks the picture, it flips to show the
 * list. The animation is made of two smaller animations: the first half
 * rotates the list by 90 degrees on the Y axis and the second half rotates
 * the picture by 90 degrees on the Y axis. When the first half finishes, the
 * list is made invisible and the picture is set visible.
 */
class Transition3d : Activity(), AdapterView.OnItemClickListener, View.OnClickListener {
  private lateinit var binding: AnimationsMainScreenBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = AnimationsMainScreenBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    // Prepare the ListView
    val adapter = ArrayAdapter(
      this,
      android.R.layout.simple_list_item_1, PHOTOS_NAMES
    )

    binding.mPhotosList.adapter = adapter
    binding.mPhotosList.onItemClickListener = this

    // Prepare the ImageView
    binding.mImageView.isClickable = true
    binding.mImageView.isFocusable = true
    binding.mImageView.setOnClickListener(this)
  }

  /**
   * Setup a new 3D rotation on the container view.
   *
   * @param position the item that was clicked to show a picture, or -1 to show the list
   * @param start    the start angle at which the rotation must begin
   * @param end      the end angle of the rotation
   */
  private fun applyRotation(position: Int, start: Float, end: Float) {
    // Find the center of the container
    val centerX = binding.mContainer.width / 2.0f
    val centerY = binding.mContainer.height / 2.0f

    // Create a new 3D rotation with the supplied parameter
    // The animation listener is used to trigger the next animation
    val rotation = Rotate3dAnimation(start, end, centerX, centerY, 310.0f, true)
    rotation.duration = 500
    rotation.fillAfter = true
    rotation.interpolator = AccelerateInterpolator()
    rotation.setAnimationListener(DisplayNextView(position))

    binding.mContainer.startAnimation(rotation)
  }

  override fun onItemClick(parent: AdapterView<*>, v: View, position: Int, id: Long) {
    // Pre-load the image then start the animation
    binding.mImageView.setImageResource(PHOTOS_RESOURCES[position])
    applyRotation(position, 0f, 90f)
  }

  override fun onClick(v: View) {
    applyRotation(-1, 180f, 90f)
  }

  /**
   * This class listens for the end of the first half of the animation.
   * It then posts a new action that effectively swaps the views when the container
   * is rotated 90 degrees and thus invisible.
   */
  private inner class DisplayNextView constructor(val mPosition: Int) :
    Animation.AnimationListener {

    override fun onAnimationStart(animation: Animation) {}

    override fun onAnimationEnd(animation: Animation) {
      binding.mContainer.post(SwapViews(mPosition))
    }

    override fun onAnimationRepeat(animation: Animation) {}
  }

  /**
   * This class is responsible for swapping the views and start the second
   * half of the animation.
   */
  private inner class SwapViews(private val mPosition: Int) : Runnable {

    override fun run() {
      val centerX = binding.mContainer.width / 2.0f
      val centerY = binding.mContainer.height / 2.0f
      val rotation: Rotate3dAnimation

      if (mPosition > -1) {
        binding.mPhotosList.visibility = View.GONE
        binding.mImageView.visibility = View.VISIBLE
        binding.mImageView.requestFocus()

        rotation = Rotate3dAnimation(90f, 180f, centerX, centerY, 310.0f, false)
      } else {
        binding.mImageView.visibility = View.GONE
        binding.mPhotosList.visibility = View.VISIBLE
        binding.mPhotosList.requestFocus()

        rotation = Rotate3dAnimation(90f, 0f, centerX, centerY, 310.0f, false)
      }

      rotation.duration = 500
      rotation.fillAfter = true
      rotation.interpolator = DecelerateInterpolator()

      binding.mContainer.startAnimation(rotation)
    }
  }

  companion object {
    // Names of the photos we show in the list
    private val PHOTOS_NAMES: Array<String> =
      arrayOf(
        "Lyon",
        "Livermore",
        "Tahoe Pier",
        "Lake Tahoe",
        "Grand Canyon",
        "Bodie"
      )
    // Resource identifiers for the photos we want to display
    private val PHOTOS_RESOURCES = intArrayOf(
      R.drawable.photo1,
      R.drawable.photo2,
      R.drawable.photo3,
      R.drawable.photo4,
      R.drawable.photo5,
      R.drawable.photo6
    )
  }
}
