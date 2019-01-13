package ro.ubbcluj.cs.books

import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.Date

import ro.ubbcluj.cs.books.books.R
import ro.ubbcluj.cs.books.domain.Book

import kotlinx.android.synthetic.main.book_detail.view.*


/**
 * A fragment representing a single Book detail screen.
 * This fragment is either contained in a [BookListActivity]
 * in two-pane mode (on tablets) or a [EventDetailActivity]
 * on handsets.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class EventDetailFragment : Fragment() {

  /**
   * The dummy content this fragment is presenting.
   */
  private var mItem: Book? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (arguments != null && arguments!!.containsKey(ARG_ITEM_ID)) {
      // Load the dummy content specified by the fragment
      // arguments. In a real-world scenario, use a Loader
      // to load content from a content provider.
      mItem = Book(Integer.parseInt(arguments!!.getString(ARG_ITEM_ID)!!), "foo", Date())

      val activity = this.activity!!
      val appBarLayout = activity.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)
      if (appBarLayout != null) {
        appBarLayout.title = mItem!!.title
      }
    }
  }

  override fun onCreateView(inflater: LayoutInflater,
                            container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val rootView = inflater.inflate(R.layout.book_detail, container, false)

    // Show the dummy content as text in a TextView.
    if (mItem != null) {
      rootView.event_detail.text = mItem!!.title
    }

    return rootView
  }

  companion object {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    const val ARG_ITEM_ID = "item_id"
  }
}
