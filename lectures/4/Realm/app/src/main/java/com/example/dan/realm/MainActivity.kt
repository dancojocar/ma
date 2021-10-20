package com.example.dan.realm

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.example.dan.realm.databinding.ActivityMainBinding
import com.example.dan.realm.model.Cat
import com.example.dan.realm.model.Dog
import com.example.dan.realm.model.Person
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.executeTransactionAwait
import io.realm.kotlin.where
import kotlinx.coroutines.*

class MainActivity : Activity(), CoroutineScope by MainScope() {
  private lateinit var binding: ActivityMainBinding

  private lateinit var rootLayout: LinearLayout
  private lateinit var realm: Realm

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    rootLayout = binding.container
    rootLayout.removeAllViews()

    val config = RealmConfiguration.Builder()
      .allowWritesOnUiThread(true)
      .build()

    // Open the realm for the UI thread.
    realm = Realm.getInstance(config)

    // Delete all persons
    // Using executeTransaction with a lambda reduces code size and makes it impossible
    // to forget to commit the transaction.
    realm.executeTransaction { realm ->
      realm.deleteAll()
    }

    // These operations are small enough that
    // we can generally safely run them on the UI thread.
    basicCRUD(realm)
    basicQuery(realm)
    basicLinkQuery(realm)

// More complex operations can be executed on another thread
    val uiScope = CoroutineScope(Dispatchers.Main + Job())
    uiScope.launch {
      withContext(Dispatchers.IO) {
        var info = ""

        // Open the default realm. All threads must use its own reference to the realm.
        // Those can not be transferred across threads.

        // Realm implements the Closable interface, therefore
        // we can make use of Kotlin's built-in extension method 'use' (pun intended).
        Realm.getDefaultInstance().use { realm ->
          info += complexReadWrite(realm)
          info += complexQuery(realm)
        }
        //Do background tasks...
        withContext(Dispatchers.Main) {
          //Update UI
          showStatus(info)
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    realm.close() // Remember to close Realm when done.
  }

  private fun showStatus(text: String) {
    logd(text)
    val textView = TextView(this)
    textView.text = text
    rootLayout.addView(textView)
  }

  @Suppress("NAME_SHADOWING")
  private fun basicCRUD(realm: Realm) {
    showStatus("Perform basic Create/Read/Update/Delete (CRUD) operations...")

    // All writes must be wrapped in a transaction to facilitate safe multi threading
    realm.executeTransaction { realm ->
      // Add a person
      val person = realm.createObject<Person>(0)
      person.name = "Young Person"
      person.age = 14
    }

    // Find the first person (no query conditions) and read a field
    val person = realm.where<Person>().findFirst()!!
    showStatus("id: ${person.id} name: ${person.name} age: ${person.age}")

    // Update person in a transaction
    realm.executeTransaction { _ ->
      person.name = "Senior Person"
      person.age = 99
    }
    showStatus("id: ${person.id} name: ${person.name} got older: ${person.age}")

    val age = 22
    // Create another person
    realm.executeTransaction { realm ->
      // Add a person
      val person = realm.createObject<Person>(1)
      person.name = "Another Person"
      person.age = age
    }

    realm.executeTransaction { realm ->
      val persons = realm.where<Person>().equalTo("age", age).findAll()!!
      // Delete the elements in the result object
      persons.deleteAllFromRealm()
    }
  }

  private fun basicQuery(realm: Realm) {
    showStatus("\nPerforming basic Query operation...")
    showStatus("Number of persons: ${realm.where<Person>().count()}")

    val ageCriteria = 99
    val results = realm.where<Person>().equalTo("age", ageCriteria).findAll()

    showStatus("Size of result set: " + results.size)
  }

  private fun basicLinkQuery(realm: Realm) {
    showStatus("\nPerforming basic Link Query operation...")
    showStatus("Number of persons: ${realm.where<Person>().count()}")

    val person = realm.where<Person>().findFirst()!!

    realm.executeTransaction { realmDB ->
      // Add a cat
      val cat = realmDB.createObject<Cat>()
      cat.name = "Tiger"
    }
    val cat = realm.where<Cat>().findFirst()!!

    // Update person in a transaction
    realm.executeTransaction { _ ->
      person.cats.add(cat)
    }

    val results = realm.where<Person>().equalTo("cats.name", "Tiger").findAll()

    showStatus("Size of result set: ${results.size}")
  }

  private fun complexReadWrite(realm: Realm): String {
    var status = "\nPerforming complex Read/Write operation..."

    // Add ten persons in one transaction
    realm.executeTransaction {
      val fido = realm.createObject<Dog>()
      fido.name = "fido"
      for (i in 1..9) {
        val person = realm.createObject<Person>(i.toLong())
        person.name = "Person no. $i"
        person.age = i
        person.dog = fido

        // The field tempReference is annotated with @Ignore.
        // This means setTempReference sets the Person tempReference
        // field directly. The tempReference is NOT saved as part of
        // the RealmObject:
        person.tempReference = 42

        for (j in 0 until i) {
          val cat = realm.createObject<Cat>()
          cat.name = "Cat_$j"
          person.cats.add(cat)
        }
      }
    }

    // Implicit read transactions allow you to access your objects
    status += "\nNumber of persons: ${realm.where<Person>().count()}"

    // Iterate over all objects
    for (person in realm.where<Person>().findAll()) {
      val dogName: String = person?.dog?.name ?: "None"

      status += "\n${person.id} - ${person.name}: ${person.age} : $dogName : ${person.cats.size}"

      // The field tempReference is annotated with @Ignore
      // Though we initially set its value to 42, it has
      // not been saved as part of the Person RealmObject:
      check(person.tempReference == 0)
    }

    // Sorting
    val sortedPersons = realm.where<Person>().sort(Person::age.name, Sort.DESCENDING).findAll()
    status += "\nSorting ${sortedPersons.last()?.name} == ${
      realm.where<Person>().findAll().first()?.name
    }"

    return status
  }

  private fun complexQuery(realm: Realm): String {
    var status = "\n\nPerforming complex Query operation..."

    status += "\nNumber of persons: ${realm.where<Person>().count()}"

    // Find all persons where age between 7 and 9 and name begins with "Person".
    val results = realm.where<Person>()
      .between("age", 7, 9)       // Notice implicit "and" operation
      .beginsWith("name", "Person")
      .findAll()

    status += "\nSize of result set: ${results.size}"

    return status
  }
}
