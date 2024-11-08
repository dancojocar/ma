package com.example.realm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.realm.model.Cat
import com.example.realm.model.Dog
import com.example.realm.model.Person
import com.example.realm.ui.theme.RealmTheme
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class MainActivity : ComponentActivity(), CoroutineScope by MainScope() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      RealmTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {

          val config =
            RealmConfiguration.Builder(
              schema = setOf(Cat::class, Dog::class, Person::class),
            )
              .deleteRealmIfMigrationNeeded()
              .build()
          val realm: Realm = Realm.open(config)

          val cat1 = Cat("Whiskers")
          val cat2 = Cat("Mittens")

          val catSet = setOf(cat1, cat2)

          val person = Person().apply {
            name = "John"
            age = 42
            dog = Dog().apply {
              name = "Jack"
            }
          }
          person.cats.addAll(catSet)

          realm.writeBlocking {
//            deleteAll()
            copyToRealm(person)
          }

          val persons: List<Person> = realm.query<Person>().find()
          LazyColumn {
            items(persons) { person ->
              PersonItem(person)
            }
          }

        }
      }
    }
  }

  @Composable
  fun PersonItem(person: Person) {
    Column(modifier = Modifier.padding(8.dp)) {
      Text(text = "DebugId: ${person.id.timestamp}")
      Text(text = "Name: ${person.name}, Age: ${person.age}")
      Text(text = "Dog: ${person.dog?.name}")

      // Displaying all cats for this person in LazyColumn
      val cats: Set<Cat> = person.cats
      Text(text = "Cats:")
      LazyColumn(modifier = Modifier.height(100.dp)) {
        items(cats.toList()) { cat ->
          CatItem(cat)
        }
      }
    }
  }

  @Composable
  fun CatItem(cat: Cat) {
    Text(text = " --> Cat: ${cat.name}", modifier = Modifier.padding(start = 16.dp))
  }
}
