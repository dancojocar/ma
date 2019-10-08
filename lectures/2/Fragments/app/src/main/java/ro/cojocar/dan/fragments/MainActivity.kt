package ro.cojocar.dan.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.main_activity.*
import ro.cojocar.dan.fragments.ui.articlelist.ArticleListFragment
import ro.cojocar.dan.fragments.ui.articlereader.ArticleReaderFragment
import ro.cojocar.dan.fragments.ui.articlereader.ArticleReaderFragment.Companion.MESSAGE

class MainActivity : FragmentActivity(), ArticleListFragment.OnSelectionListener {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)
    if (savedInstanceState == null) {
      val mainFragment = findViewById<View>(R.id.container)
      if (mainFragment != null) {
        supportFragmentManager.beginTransaction()
          .replace(mainFragment.id, ArticleListFragment.newInstance())
          .commitNow()
      }
      val detailedContainer = findViewById<View>(R.id.detailedContainer)
      if (detailedContainer != null) {
        supportFragmentManager.beginTransaction()
          .replace(detailedContainer.id, ArticleReaderFragment.newInstance())
          .commitNow()
      }
    }
  }

  override fun onSelect(message: String) {
    if (detailedContainer != null) {
      logd("send message: $message")
      val newFragment = ArticleReaderFragment.newInstance() as Fragment
      val bundle = Bundle()
      bundle.putString(MESSAGE, message)
      newFragment.arguments = bundle
      supportFragmentManager.beginTransaction()
        .replace(R.id.detailedContainer, newFragment)
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
