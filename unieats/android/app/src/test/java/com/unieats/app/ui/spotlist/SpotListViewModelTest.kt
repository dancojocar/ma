package com.unieats.app.ui.spotlist

import app.cash.turbine.test
import com.unieats.app.testing.FakeEatsRepository
import com.unieats.app.testing.FakeFeatureFlags
import com.unieats.app.testing.MainDispatcherRule
import com.unieats.shared.Category
import com.unieats.shared.RemoteFlags
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpotListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeEatsRepository()
    private val flags = FakeFeatureFlags()

    /** uiState uses WhileSubscribed, so something has to collect it, as the screen would. */
    private fun TestScope.createViewModel(): SpotListViewModel =
        SpotListViewModel(repository, flags).also { vm ->
            backgroundScope.launch(mainDispatcherRule.dispatcher) { vm.uiState.collect {} }
        }

    @Test
    fun `first page is requested on start and spots come from the repository`() = runTest {
        val viewModel = createViewModel()

        assertEquals(listOf(FakeEatsRepository.RefreshCall(1, "", null)), repository.refreshCalls)
        val state = viewModel.uiState.value
        assertEquals(listOf("Central Canteen", "Espresso Lab", "Pizza Stop"), state.spots.map { it.name })
        assertEquals(false, state.isLoading)
        assertTrue(state.isLive)
    }

    @Test
    fun `search filters the list and reloads page 1 after the debounce`() = runTest {
        val viewModel = createViewModel()

        viewModel.onSearchQueryChange("pizza")

        assertEquals("pizza", viewModel.uiState.value.searchQuery)
        assertEquals(listOf("Pizza Stop"), viewModel.uiState.value.spots.map { it.name })
        advanceTimeBy(301)
        assertEquals(FakeEatsRepository.RefreshCall(1, "pizza", null), repository.refreshCalls.last())
    }

    @Test
    fun `category filter keeps only that category`() = runTest {
        val viewModel = createViewModel()

        viewModel.onCategorySelected(Category.CAFE)

        assertEquals(listOf("Espresso Lab"), viewModel.uiState.value.spots.map { it.name })
    }

    @Test
    fun `toggling a favourite adds it and toggling again removes it`() = runTest {
        val viewModel = createViewModel()

        viewModel.toggleFavourite("spot-2")
        assertEquals(setOf("spot-2"), viewModel.uiState.value.favouriteIds)

        viewModel.toggleFavourite("spot-2")
        assertEquals(emptySet<String>(), viewModel.uiState.value.favouriteIds)
    }

    @Test
    fun `a failed refresh shows an error and retry clears it`() = runTest {
        repository.failRefresh = true
        val viewModel = createViewModel()
        assertEquals("Can't reach the server. Is it running on port 3000?", viewModel.uiState.value.errorMessage)

        repository.failRefresh = false
        viewModel.retry()

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(2, repository.refreshCalls.size)
    }

    @Test
    fun `a live edit in the repository flows straight into the list`() = runTest {
        val viewModel = SpotListViewModel(repository, flags)

        viewModel.uiState.test {
            assertEquals("Pizza Stop", awaitItem().spots.last().name)
            repository.editSpot("spot-3", "Pizza Stop (new oven)", "", openNow = false)
            assertEquals("Pizza Stop (new oven)", awaitItem().spots.last().name)
        }
    }

    @Test
    fun `the remote flag reaches the list state`() = runTest {
        val viewModel = createViewModel()

        flags.flags.value = RemoteFlags(showNewRatingUi = true)

        assertTrue(viewModel.uiState.value.showNewRatingUi)
    }
}
