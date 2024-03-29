package com.example.livedataactivity.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.livedataactivity.core.Person
import com.example.livedataactivity.core.getSuperheroList
import kotlinx.coroutines.delay

class SuperheroesViewModel : ViewModel() {
  val superheroes: LiveData<List<Person>> = liveData {
    val superheroList = loadSuperheroes()
    emit(superheroList)
  }

  // Added a delay of 2 seconds to emulate a network request. This method just sets the list of
  // superheroes to the livedata after 2 seconds.
  private suspend fun loadSuperheroes(): List<Person> {
    delay(2000)
    return getSuperheroList()
  }
}
