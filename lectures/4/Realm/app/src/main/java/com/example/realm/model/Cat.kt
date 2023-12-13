package com.example.realm.model

import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet

class Cat() : RealmObject {
  var name: String = ""
  var owners: RealmSet<Person> = realmSetOf()

  constructor(name: String) : this() {
    this.name = name
  }
}
