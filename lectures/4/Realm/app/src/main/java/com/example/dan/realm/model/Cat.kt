package com.example.dan.realm.model

import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects

open class Cat : RealmObject() {
  var name: String? = null

  @LinkingObjects("cats")
  val owners: RealmResults<Person>? = null
}
