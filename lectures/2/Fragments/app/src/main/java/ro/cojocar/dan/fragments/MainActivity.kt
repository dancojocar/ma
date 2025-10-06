package ro.cojocar.dan.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import ro.cojocar.dan.fragments.databinding.MainActivityBinding
import ro.cojocar.dan.fragments.ui.articlelist.ArticleListFragment
import ro.cojocar.dan.fragments.ui.articlereader.ArticleReaderFragment
import ro.cojocar.dan.fragments.ui.articlereader.ArticleReaderFragment.Companion.MESSAGE

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : FragmentActivity(), ArticleListFragment.OnSelectionListener {
  private lateinit var binding: MainActivityBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = MainActivityBinding.inflate(layoutInflater)
    setContentView(binding.root)

    if (savedInstanceState == null) {
      val mainFragment = binding.container
      supportFragmentManager.beginTransaction()
        .replace(mainFragment.id, ArticleListFragment.newInstance())
        .commitNow()
      val detailedContainer = binding.detailedContainer
      if (detailedContainer != null) {
        supportFragmentManager.beginTransaction()
          .replace(detailedContainer.id, ArticleReaderFragment.newInstance())
          .commitNow()
      }
    }
  }

  override fun onSelect(message: String) {
    val detailedContainer = binding.detailedContainer
    if (detailedContainer != null) {
      logd("send message: $message")
      val newFragment = ArticleReaderFragment.newInstance() as Fragment
      val bundle = Bundle()
      bundle.putString(MESSAGE, message)
      newFragment.arguments = bundle
      supportFragmentManager.beginTransaction()
        .replace(detailedContainer.id, newFragment)
        .commitNow()
    } else {
      Toast.makeText(
        this@MainActivity,
        "Change the orientation first!",
        Toast.LENGTH_SHORT
      ).show()
    }
  }
}
