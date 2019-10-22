package com.example.dan.realm.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

// Your model has to extend RealmObject. Furthermore, the class must be annotated with open (Kotlin classes are final
// by default).
open class Person(
  // You can put properties in the constructor as long as all of them are initialized with
  // default values. This ensures that an empty constructor is generated.
  // All properties are by default persisted.
  // Properties can be annotated with PrimaryKey or Index.
  // If you use non-nullable types, properties must be initialized with non-null values.
  @PrimaryKey var id: Long = 0,
  var name: String = "",
  var age: Int = 0,
  // Other objects in a one-to-one
  // relation must also subclass RealmObject
  var dog: Dog? = null,
  // One-to-many relations is simply a RealmList of the objects which also subclass RealmObject
  var cats: RealmList<Cat> = RealmList(),

  // You can instruct Realm to ignore a field and not persist it.
  @Ignore var tempReference: Int = 0

) : RealmObject() {
  // The Kotlin compiler generates standard getters and setters.
  // Realm will overload them and code inside them is ignored.
  // So if you prefer you can also just have empty abstract methods.
}
