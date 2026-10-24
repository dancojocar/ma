package com.unieats.app.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouritesRepository @Inject constructor() {

    private val _ids = MutableStateFlow(emptySet<String>())
    val favouriteIds: StateFlow<Set<String>> = _ids.asStateFlow()

    fun toggle(id: String) {
        _ids.update { ids -> if (id in ids) ids - id else ids + id }
    }
}
