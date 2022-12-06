package com.example.memoryleakexample

object GlobalSingleton {

    private val listeners = mutableListOf<GlobalSingletonListener>()

    fun register(listener: GlobalSingletonListener) {
        listeners.add(listener)
    }

    fun unregister(listener: GlobalSingletonListener) {
        listeners.remove(listener)
    }
}

interface GlobalSingletonListener {
    fun onEvent()
}