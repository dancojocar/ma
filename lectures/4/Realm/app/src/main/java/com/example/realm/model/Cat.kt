package com.example.realm.model

import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

class Cat() : RealmObject, Pet {
  @PrimaryKey var id: String = UUID.randomUUID().toString()
  override var name: String = ""
  var owners: RealmSet<Person> = realmSetOf()

  constructor(name: String) : this() {
    this.name = name
  }
}
