package com.example.realm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import io.realm.kotlin.ext.realmSetOf
import kotlinx.coroutines.*

class MainActivity : ComponentActivity(), CoroutineScope by MainScope() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      RealmTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

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
    Column {
      Text(text = "DebugId: ${person.id.timestamp}")
      Text(text = "Name: ${person.name} Age: ${person.age}")
      Text(text = " -> Dog: ${person.dog?.name}")
      val cats: Set<Cat> = person.cats
      LazyColumn(modifier = Modifier.height(100.dp)) {
        items(cats.toList()) { cat ->
          CatItem(cat)
        }
      }
    }
  }

  @Composable
  fun CatItem(cat: Cat) {
    Text(" --> Cat Name: ${cat.name}")
  }
}
