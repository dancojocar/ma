package com.example.listview

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val listView = findViewById<View>(R.id.listView) as ListView
    val listItem = resources.getStringArray(R.array.array_technology)
    val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
      this,
      R.layout.my_list_item, R.id.textView, listItem
    )
    listView.adapter = adapter

    listView.setOnItemClickListener { _, _, position, _ ->
      val value = adapter.getItem(position)
      Toast.makeText(applicationContext, value, Toast.LENGTH_SHORT).show()
    }
  }
}