package com.example.realm.model

import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

// Your model has to extend RealmObject. Furthermore, the class must be annotated with open (Kotlin classes are final
// by default).
class Person() : RealmObject {
  // You can put properties in the constructor as long as all of them are initialized with
  // default values. This ensures that an empty constructor is generated.
  // All properties are by default persisted.
  // Properties can be annotated with PrimaryKey or Index.
  // If you use non-nullable types, properties must be initialized with non-null values.
  @PrimaryKey
  var id: ObjectId = ObjectId()
  var name: String = ""
  var age: Int = 0

  // Other objects in a one-to-one
  // relation must also subclass RealmObject
  var dog: Dog? = null

  // One-to-many relations is simply a RealmList of the objects which also subclass RealmObject
  var cats: RealmSet<Cat> = realmSetOf()

  // You can instruct Realm to ignore a field and not persist it.
  @Ignore
  var tempReference: Int = 0
}
