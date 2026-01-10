package com.example.archdemo.data

class ItemsRepository {
    fun getItems(): List<String> {
        return listOf("Item 1", "Item 2", "Item 3")
    }
}