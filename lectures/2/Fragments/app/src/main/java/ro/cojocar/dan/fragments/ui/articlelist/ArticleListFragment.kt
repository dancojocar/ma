package ro.cojocar.dan.fragments.ui.articlelist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.article_list_fragment.*
import ro.cojocar.dan.fragments.R
import ro.cojocar.dan.fragments.logd

class ArticleListFragment : Fragment() {

  companion object {
    fun newInstance() = ArticleListFragment()
  }

  private var listener: OnSelectionListener? = null

  interface OnSelectionListener {
    fun onSelect(message: String)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    try {
      listener = context as OnSelectionListener
      logd("listener attached")
    } catch (e: ClassCastException) {
      throw ClassCastException("$context must implement OnSelectionListener")
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return inflater.inflate(R.layout.article_list_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    for (i in 0..10) {
      val horizontalLayout = LinearLayout(activity)
      horizontalLayout.orientation = LinearLayout.HORIZONTAL
      val textView = TextView(activity)
      textView.text = getString(R.string.textViewLabel, i)
      horizontalLayout.addView(textView)
      val button = Button(activity)
      button.text = getString(R.string.okButton, i)
      button.setOnClickListener { listener!!.onSelect(getString(R.string.onPressMessage, i)) }
      horizontalLayout.addView(button)
      list_fragment?.addView(horizontalLayout)
    }
  }
}
