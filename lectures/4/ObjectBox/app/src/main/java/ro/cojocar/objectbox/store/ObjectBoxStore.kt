package ro.cojocar.objectbox.store

import android.content.Context
import io.objectbox.BoxStore
import ro.cojocar.objectbox.domain.MyObjectBox

object ObjectBoxStore {
  lateinit var store: BoxStore
    private set

  fun init(context: Context) {
    store = MyObjectBox.builder()
      .androidContext(context)
//      .inMemory("test-db")
      .build()
  }
}