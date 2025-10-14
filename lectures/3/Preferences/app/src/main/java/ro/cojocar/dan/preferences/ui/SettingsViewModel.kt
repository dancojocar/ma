package ro.cojocar.dan.preferences.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ro.cojocar.dan.preferences.data.SettingsRepository

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    fun getString(key: String, defaultValue: String): StateFlow<String> {
        return repository.getString(key, defaultValue).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
    }

    fun setString(key: String, value: String) {
        viewModelScope.launch {
            repository.setString(key, value)
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean): StateFlow<Boolean> {
        return repository.getBoolean(key, defaultValue).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    }

    fun setBoolean(key: String, value: Boolean) {
        viewModelScope.launch {
            repository.setBoolean(key, value)
        }
    }
}
