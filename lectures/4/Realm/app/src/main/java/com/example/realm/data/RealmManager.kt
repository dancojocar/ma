package com.example.realm.data

import com.example.realm.model.Cat
import com.example.realm.model.Dog
import com.example.realm.model.Person
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

object RealmManager {
    private val config = RealmConfiguration.Builder(
        schema = setOf(Cat::class, Dog::class, Person::class)
    )
        .deleteRealmIfMigrationNeeded()
        .build()

    val realm: Realm by lazy { Realm.open(config) }
}
