package com.example.catbreeds.viewmodel

import com.example.catbreeds.model.CatBreed
import com.example.catbreeds.repository.CatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import app.cash.turbine.test

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @Mock
    private lateinit var repository: CatRepository

    private lateinit var viewModel: MainViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleBreeds = listOf(
        CatBreed(
            id = "abys",
            name = "Abyssinian",
            description = "Active breed",
            origin = "Egypt",
            temperament = "Active, Energetic",
            life_span = "14 - 15",
            reference_image_id = "0XYvRd7oD",
            isFavorite = false
        ),
        CatBreed(
            id = "aege",
            name = "Aegean",
            description = "Natural breed",
            origin = "Greece",
            temperament = "Affectionate, Social",
            life_span = "9 - 12",
            reference_image_id = "ozEvzdVM-",
            isFavorite = true
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Test error handling when API fails but cache exists
    @Test
    fun testApiFailsWithCache() = runTest {
        // Given - Mock API failure during initialization but cache is available
        whenever(repository.initializeAppData()).thenReturn(Result.failure(Exception("Network error")))
        whenever(repository.hasCachedData()).thenReturn(true)
        whenever(repository.getBreeds(10, 0)).thenReturn(Result.failure(Exception("API unavailable")))
        whenever(repository.isOnline()).thenReturn(false)
        whenever(repository.getTotalBreedsCount()).thenReturn(Result.failure(Exception("Network error")))
        whenever(repository.getCachedBreedsCount()).thenReturn(2)

        // When - ViewModel initializes
        viewModel = MainViewModel(repository)
        advanceUntilIdle()

        // Then - Should handle the error gracefully and show appropriate state
        viewModel.uiState.test {
            val finalState = awaitItem()

            assertFalse("Loading should be complete after error handling", finalState.isLoading)
            assertNotNull("Should display error message to user", finalState.error)
            assertTrue("Should detect that cached data exists", finalState.hasCachedData)
            assertFalse("Should correctly show offline status", finalState.isOnline)

            verify(repository).initializeAppData()
            verify(repository).hasCachedData()
            verify(repository).getBreeds(10, 0)
        }
    }

    // Test error handling when both API and cache fail
    @Test
    fun testApiFailsNoCache() = runTest {
        // Given - Mock complete failure of both API and cache
        whenever(repository.initializeAppData()).thenReturn(Result.failure(Exception("Network error")))
        whenever(repository.hasCachedData()).thenReturn(false)
        whenever(repository.getBreeds(10, 0)).thenReturn(Result.failure(Exception("No data available")))
        whenever(repository.isOnline()).thenReturn(false)
        whenever(repository.getTotalBreedsCount()).thenReturn(Result.failure(Exception("Network error")))
        whenever(repository.getCachedBreedsCount()).thenReturn(0)

        // When - ViewModel tries to initialize with no data sources
        viewModel = MainViewModel(repository)
        advanceUntilIdle()

        // Then - Should show appropriate error state for complete data failure
        viewModel.uiState.test {
            val finalState = awaitItem()

            assertFalse("Loading should complete even with errors", finalState.isLoading)
            assertNotNull("Should show error message explaining the problem", finalState.error)
            assertTrue("Breeds list should be empty when no data available", finalState.breeds.isEmpty())
            assertFalse("Should indicate no cached data is available", finalState.hasCachedData)
            assertFalse("Should show offline status", finalState.isOnline)
        }
    }

    // Test that favorite toggle calls repository correctly
    @Test
    fun testToggleFavoriteSuccess() = runTest {
        // Given - Successfully initialized ViewModel with loaded breeds
        whenever(repository.initializeAppData()).thenReturn(Result.success(2))
        whenever(repository.getBreeds(10, 0)).thenReturn(Result.success(sampleBreeds))
        whenever(repository.isOnline()).thenReturn(true)
        whenever(repository.getTotalBreedsCount()).thenReturn(Result.success(2))
        whenever(repository.hasCachedData()).thenReturn(true)
        whenever(repository.toggleFavoriteStatus("abys")).thenReturn(Result.success(Unit))

        viewModel = MainViewModel(repository)
        advanceUntilIdle()

        // When - User taps favorite button on Abyssinian breed
        viewModel.toggleFavoriteStatus("abys")
        advanceUntilIdle()

        // Then - Should delegate to repository and maintain stable UI state
        verify(repository).toggleFavoriteStatus("abys")

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse("Should not show loading after favorite toggle", state.isLoading)
            assertNull("Should not show error after successful favorite operation", state.error)
        }
    }

    // Test that favorite toggle handles errors gracefully
    @Test
    fun testToggleFavoriteError() = runTest {
        // Given - Successfully initialized ViewModel
        whenever(repository.initializeAppData()).thenReturn(Result.success(2))
        whenever(repository.getBreeds(10, 0)).thenReturn(Result.success(sampleBreeds))
        whenever(repository.isOnline()).thenReturn(true)
        whenever(repository.getTotalBreedsCount()).thenReturn(Result.success(2))
        whenever(repository.hasCachedData()).thenReturn(true)
        whenever(repository.toggleFavoriteStatus("nonexistent")).thenReturn(
            Result.failure(Exception("Breed not found"))
        )

        viewModel = MainViewModel(repository)
        advanceUntilIdle()

        // When - User tries to favorite a breed that doesn't exist
        viewModel.toggleFavoriteStatus("nonexistent")
        advanceUntilIdle()

        // Then - Should handle the error without crashing
        verify(repository).toggleFavoriteStatus("nonexistent")

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse("Should not be in loading state", state.isLoading)
        }
    }

    // Test successful initialization with working API
    @Test
    fun testInitializeSuccess() = runTest {
        // Given - Mock successful API initialization and data loading
        whenever(repository.initializeAppData()).thenReturn(Result.success(2))
        whenever(repository.getBreeds(10, 0)).thenReturn(Result.success(sampleBreeds))
        whenever(repository.isOnline()).thenReturn(true)
        whenever(repository.getTotalBreedsCount()).thenReturn(Result.success(2))
        whenever(repository.hasCachedData()).thenReturn(true)

        // When - ViewModel initializes successfully
        viewModel = MainViewModel(repository)
        advanceUntilIdle()

        // Then - Should show successful state with loaded data
        viewModel.uiState.test {
            val finalState = awaitItem()

            assertFalse("Loading should be complete", finalState.isLoading)
            assertEquals("Should display all loaded breeds", 2, finalState.breeds.size)
            assertNull("Should have no error messages", finalState.error)
            assertEquals("Should show correct first breed", "Abyssinian", finalState.breeds[0].name)
            assertEquals("Should show correct second breed", "Aegean", finalState.breeds[1].name)
            assertTrue("Should indicate online status", finalState.isOnline)
        }
    }
}
