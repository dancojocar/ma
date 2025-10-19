package com.example.realm.model



import io.realm.kotlin.types.TypedRealmObject

interface Pet: TypedRealmObject {
    val name: String
}
