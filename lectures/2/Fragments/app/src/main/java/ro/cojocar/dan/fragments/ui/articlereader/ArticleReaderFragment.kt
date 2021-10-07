package ro.cojocar.dan.fragments.ui.articlereader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import ro.cojocar.dan.fragments.R
import ro.cojocar.dan.fragments.logd

class ArticleReaderFragment : Fragment() {

  private var rootView: View? = null

  companion object {
    const val MESSAGE = "message"
    fun newInstance() = ArticleReaderFragment()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    rootView = inflater.inflate(R.layout.article_reader_fragment, container, false)
    return rootView!!
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val message = arguments?.getString(MESSAGE)
    if (message != null) {
      logd("received message: $message")
      val linearLayout = rootView as LinearLayout
      for (i in 0..4) {
        val tv = TextView(activity)
        tv.text = message
        linearLayout.addView(tv)
      }
    }
  }
}
