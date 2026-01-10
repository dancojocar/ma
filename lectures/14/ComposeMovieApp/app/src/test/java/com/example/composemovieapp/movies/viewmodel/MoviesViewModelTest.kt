package com.example.composemovieapp.movies.viewmodel

import com.example.composemovieapp.movies.domain.Movie
import com.example.composemovieapp.movies.repo.Result
import com.example.composemovieapp.movies.usecase.AddMovieUseCase
import com.example.composemovieapp.movies.usecase.GetMoviesUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for [MoviesViewModel].
 * Demonstrates:
 * - Mocking use cases with Mockito
 * - Testing StateFlow emissions
 * - Testing coroutines with TestDispatcher
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getMoviesUseCase: GetMoviesUseCase
    private lateinit var addMovieUseCase: AddMovieUseCase
    private lateinit var viewModel: MoviesViewModel

    private val testMovies = listOf(
        Movie(
            category = "Action",
            desc = "A test action movie",
            imageUrl = "https://example.com/action.jpg",
            link = "https://example.com/action",
            name = "Test Action Movie"
        ),
        Movie(
            category = "Comedy",
            desc = "A test comedy movie",
            imageUrl = "https://example.com/comedy.jpg",
            link = "https://example.com/comedy",
            name = "Test Comedy Movie"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMoviesUseCase = mock()
        addMovieUseCase = mock()
        viewModel = MoviesViewModel(getMoviesUseCase, addMovieUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have empty movies list`() {
        val state = viewModel.uiState.value
        
        assertThat(state.movies).isEmpty()
        assertThat(state.loading).isFalse()
        assertThat(state.error).isFalse()
    }

    @Test
    fun `loadInitialDataIfNeeded should fetch movies on first call`() = runTest {
        // Given
        whenever(getMoviesUseCase()).thenReturn(Result.Success(testMovies))

        // When
        viewModel.loadInitialDataIfNeeded()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.movies).hasSize(2)
        assertThat(state.movies[0].name).isEqualTo("Test Action Movie")
        assertThat(state.loading).isFalse()
        assertThat(state.error).isFalse()
    }

    @Test
    fun `loadInitialDataIfNeeded should not refetch on second call`() = runTest {
        // Given
        whenever(getMoviesUseCase()).thenReturn(Result.Success(testMovies))

        // When - first call
        viewModel.loadInitialDataIfNeeded()
        advanceUntilIdle()

        // When - second call (should not refetch)
        whenever(getMoviesUseCase()).thenReturn(Result.Success(emptyList()))
        viewModel.loadInitialDataIfNeeded()
        advanceUntilIdle()

        // Then - should still have original movies
        val state = viewModel.uiState.value
        assertThat(state.movies).hasSize(2)
    }

    @Test
    fun `loadInitialDataIfNeeded should set error state on failure`() = runTest {
        // Given
        whenever(getMoviesUseCase()).thenReturn(
            Result.Error(code = 500, error = "Server error")
        )

        // When
        viewModel.loadInitialDataIfNeeded()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.error).isTrue()
        assertThat(state.errorMessage).contains("500")
        assertThat(state.movies).isEmpty()
    }

    @Test
    fun `addMovie should add movie to list on success`() = runTest {
        // Given - first load some movies
        whenever(getMoviesUseCase()).thenReturn(Result.Success(testMovies))
        viewModel.loadInitialDataIfNeeded()
        advanceUntilIdle()

        val newMovie = Movie(
            category = "Drama",
            desc = "A new drama movie",
            imageUrl = "https://example.com/drama.jpg",
            link = "https://example.com/drama",
            name = "New Drama Movie"
        )
        whenever(addMovieUseCase(newMovie)).thenReturn(Result.Success(newMovie))

        // When
        var successCalled = false
        viewModel.addMovie(newMovie) { successCalled = true }
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.movies).hasSize(3)
        assertThat(state.movies.last().name).isEqualTo("New Drama Movie")
        assertThat(successCalled).isTrue()
        assertThat(state.error).isFalse()
    }

    @Test
    fun `addMovie should set error state with field errors on failure`() = runTest {
        // Given
        val newMovie = Movie(
            category = "",
            desc = "A movie without category",
            imageUrl = "https://example.com/movie.jpg",
            link = "https://example.com/movie",
            name = "Invalid Movie"
        )
        whenever(addMovieUseCase(newMovie)).thenReturn(
            Result.Error(
                code = 400,
                error = "Validation failed",
                errorMap = mapOf("category" to "Category is required")
            )
        )

        // When
        var successCalled = false
        viewModel.addMovie(newMovie) { successCalled = true }
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.error).isTrue()
        assertThat(state.fieldErrors).containsKey("category")
        assertThat(successCalled).isFalse()
    }
}
