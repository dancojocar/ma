package ro.ubbcluj.cs.books.adapter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ro.ubbcluj.cs.books.EventDetailActivity
import ro.ubbcluj.cs.books.EventDetailFragment
import ro.ubbcluj.cs.books.R
import ro.ubbcluj.cs.books.domain.Book


class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

  private var mValues = mutableListOf<Book>()

  fun setData(books: List<Book>) {
    mValues.clear()
    mValues.addAll(books)
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.book_list_content, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val book = mValues[position]
    holder.mItem = book
    holder.mIdView.text = book.id.toString()
    holder.mContentView.text = book.title

    holder.mView.setOnClickListener { v ->
      val context = v.context
      val intent = Intent(context, EventDetailActivity::class.java)
      val args = Bundle()
      args.putInt(EventDetailFragment.ARG_ITEM_ID, book.id)
      args.putString(EventDetailFragment.ARG_ITEM_TITLE, book.title)
      intent.putExtras(args)
      context.startActivity(intent)
    }
  }

  override fun getItemCount(): Int {
    return mValues.size
  }

  inner class ViewHolder internal constructor(internal val mView: View) : RecyclerView.ViewHolder(mView) {
    internal val mIdView: TextView = mView.findViewById(R.id.id)
    internal val mContentView: TextView = mView.findViewById(R.id.content)
    internal var mItem: Book? = null

    override fun toString(): String {
      return "${super.toString()} '${mContentView.text}'"
    }
  }
}
