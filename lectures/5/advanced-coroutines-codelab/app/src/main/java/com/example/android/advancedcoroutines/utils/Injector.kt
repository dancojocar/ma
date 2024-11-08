package com.example.android.advancedcoroutines.utils

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.example.android.advancedcoroutines.NetworkService
import com.example.android.advancedcoroutines.ui.PlantListViewModelFactory
import com.example.android.advancedcoroutines.PlantRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

interface ViewModelFactoryProvider {
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun providePlantListViewModelFactory(context: Context): PlantListViewModelFactory
}

val Injector: ViewModelFactoryProvider
    get() = currentInjector

private object DefaultViewModelProvider: ViewModelFactoryProvider {
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun getPlantRepository(context: Context): PlantRepository {
        return PlantRepository.getInstance(
            plantDao(context),
            plantService()
        )
    }

    private fun plantService() = NetworkService()

    private fun plantDao(context: Context) =
        AppDatabase.getInstance(context.applicationContext).plantDao()

    @OptIn(FlowPreview::class)
    @ExperimentalCoroutinesApi
    override fun providePlantListViewModelFactory(context: Context): PlantListViewModelFactory {
        val repository = getPlantRepository(context)
        return PlantListViewModelFactory(repository)
    }
}

private object Lock

@Volatile private var currentInjector: ViewModelFactoryProvider =
    DefaultViewModelProvider


@VisibleForTesting
private fun setInjectorForTesting(injector: ViewModelFactoryProvider?) {
    synchronized(Lock) {
        currentInjector = injector ?: DefaultViewModelProvider
    }
}

@VisibleForTesting
private fun resetInjector() =
    setInjectorForTesting(null)