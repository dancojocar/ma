package com.example.realm.model

import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet

 class Dog() : RealmObject {
  var name: String? = null

  var owners: RealmSet<Person> = realmSetOf()
}
