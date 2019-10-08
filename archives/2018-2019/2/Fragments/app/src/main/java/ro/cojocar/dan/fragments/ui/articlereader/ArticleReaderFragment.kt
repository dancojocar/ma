package ro.cojocar.dan.fragments.ui.articlereader

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import ro.cojocar.dan.fragments.R
import ro.cojocar.dan.fragments.logd

class ArticleReaderFragment : Fragment() {

    private var rootView: View? = null

    companion object {
        val MESSAGE = "message"
        fun newInstance() = ArticleReaderFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.article_reader_fragment, container, false)
        return rootView!!
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
