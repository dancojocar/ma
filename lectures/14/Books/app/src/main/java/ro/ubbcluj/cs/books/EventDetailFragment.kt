package ro.ubbcluj.cs.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ro.ubbcluj.cs.books.databinding.BookDetailBinding
import ro.ubbcluj.cs.books.domain.Book
import java.util.*


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
  private lateinit var book: Book
  private lateinit var binding: BookDetailBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (arguments != null && arguments!!.containsKey(ARG_ITEM_ID)) {
      val bookId = arguments!!.getInt(ARG_ITEM_ID)
      val bookTitle = arguments!!.getString(ARG_ITEM_TITLE)
      book = Book(bookId, bookTitle, Date())
    }
  }

  override fun onCreateView(inflater: LayoutInflater,
                            container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    binding = BookDetailBinding.inflate(inflater, container, false)
    val rootView = binding.root

    // Show the dummy content as text in a TextView.
    binding.eventDetail.text = book.title

    return rootView
  }

  companion object {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    const val ARG_ITEM_ID = "item_id"
    const val ARG_ITEM_TITLE = "item_title"
  }
}
