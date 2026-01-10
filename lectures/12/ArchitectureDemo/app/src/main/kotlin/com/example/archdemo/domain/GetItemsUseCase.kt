package com.example.archdemo.domain

import com.example.archdemo.data.ItemsRepository

class GetItemsUseCase(private val repository: ItemsRepository) {
    fun execute(): String {
        return repository.getItems().joinToString()
    }
}