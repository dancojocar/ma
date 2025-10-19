package com.example.realm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.realm.model.Cat
import com.example.realm.model.Dog
import com.example.realm.model.Person
import com.example.realm.navigation.AppNavigator
import com.example.realm.ui.theme.RealmTheme
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config =
            RealmConfiguration.Builder(
                schema = setOf(Cat::class, Dog::class, Person::class),
            )
                .deleteRealmIfMigrationNeeded()
                .build()
        val realm: Realm = Realm.open(config)

        setContent {
            RealmTheme {
                AppNavigator()
            }
        }
    }
}
