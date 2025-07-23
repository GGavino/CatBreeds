package com.example.catbreeds.repository

import com.example.catbreeds.data.local.dao.CatBreedDao
import com.example.catbreeds.data.local.entity.CatBreedEntity
import com.example.catbreeds.data.mapper.toDomainModelList
import com.example.catbreeds.model.CatBreed
import com.example.catbreeds.model.CatImage
import com.example.catbreeds.network.CatApiService
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class CatRepositoryTest {

    @Mock
    private lateinit var apiService: CatApiService

    @Mock
    private lateinit var catBreedDao: CatBreedDao

    private lateinit var repository: CatRepository

    private val sampleBreed = CatBreed(
        id = "abys",
        name = "Abyssinian",
        description = "The Abyssinian is easy to care for",
        origin = "Egypt",
        temperament = "Active, Energetic",
        life_span = "14 - 15",
        reference_image_id = "0XYvRd7oD",
        image = null,
        isFavorite = false
    )

    private val sampleImage = CatImage(
        id = "0XYvRd7oD",
        url = "https://cdn2.thecatapi.com/images/0XYvRd7oD.jpg",
        width = 1204,
        height = 1445,
        mime_type = "image/jpeg"
    )

    private val sampleEntity = CatBreedEntity(
        id = "abys",
        name = "Abyssinian",
        description = "The Abyssinian is easy to care for",
        origin = "Egypt",
        temperament = "Active, Energetic",
        lifeSpan = "14 - 15",
        referenceImageId = "0XYvRd7oD",
        imageUrl = "https://cdn2.thecatapi.com/images/0XYvRd7oD.jpg",
        imageWidth = 1204,
        imageHeight = 1445,
        imageMimeType = "image/jpeg",
        isFavorite = false
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = CatRepository(apiService, catBreedDao)
    }

    // Test that API fetch works correctly and caches data
    @Test
    fun testGetBreedsSuccess() = runTest {
        // Given - Mock successful API responses
        val breeds = listOf(sampleBreed)
        val cachedEntities = listOf(sampleEntity)

        whenever(apiService.getBreeds(10, 0)).thenReturn(breeds)
        whenever(apiService.getImage("0XYvRd7oD")).thenReturn(sampleImage)
        whenever(catBreedDao.getBreedById("abys")).thenReturn(null)
        whenever(catBreedDao.getBreedsPaginated(10, 0)).thenReturn(cachedEntities)

        // When - Request breeds from repository
        val result = repository.getBreeds(limit = 10, page = 0)

        // Then - Should return success and perform all expected operations
        assertTrue("Expected success result", result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        verify(apiService).getBreeds(10, 0)
        verify(catBreedDao).insertBreeds(any())
        verify(catBreedDao).getBreedsPaginated(10, 0)
    }

    // Test that offline mode returns cached data when API fails
    @Test
    fun testGetBreedsOffline() = runTest {
        // Given - Mock API failure but successful cache retrieval
        val cachedEntities = listOf(sampleEntity)

        whenever(apiService.getBreeds(10, 0)).thenThrow(RuntimeException("Network error"))
        whenever(catBreedDao.getBreedsPaginated(10, 0)).thenReturn(cachedEntities)

        // When - Request breeds when API is down
        val result = repository.getBreeds(limit = 10, page = 0)

        // Then - Should return cached data successfully
        assertTrue("Expected success result from cache", result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Abyssinian", result.getOrNull()?.first()?.name)
        verify(catBreedDao).getBreedsPaginated(10, 0)
        verify(catBreedDao, never()).insertBreeds(any())
    }

    // Test that favorite toggle updates database correctly
    @Test
    fun testToggleFavoriteSuccess() = runTest {
        // Given - Mock existing breed with favorite status false
        val breedId = "abys"
        val existingEntity = sampleEntity.copy(isFavorite = false)

        whenever(catBreedDao.getBreedById(breedId)).thenReturn(existingEntity)

        // When - Toggle favorite status
        val result = repository.toggleFavoriteStatus(breedId)

        // Then - Should successfully toggle from false to true
        assertTrue("Expected successful toggle", result.isSuccess)
        verify(catBreedDao).getBreedById(breedId)
        verify(catBreedDao).updateFavoriteStatus(breedId, true)
    }

    // Test that favorite toggle fails gracefully for non-existent breed
    @Test
    fun testToggleFavoriteNotFound() = runTest {
        // Given - Mock breed that doesn't exist in database
        val breedId = "nonexistent"

        whenever(catBreedDao.getBreedById(breedId)).thenReturn(null)

        // When - Try to toggle favorite on non-existent breed
        val result = repository.toggleFavoriteStatus(breedId)

        // Then - Should return failure with descriptive error message
        assertTrue("Expected failure result", result.isFailure)
        assertTrue("Expected specific error message",
            result.exceptionOrNull()?.message?.contains("Breed not found") == true)
        verify(catBreedDao).getBreedById(breedId)
        verify(catBreedDao, never()).updateFavoriteStatus(any(), any())
    }

    // Test that online check returns true when API is reachable
    @Test
    fun testIsOnlineTrue() = runTest {
        // Given - Mock successful API response
        whenever(apiService.getBreeds(1, 0)).thenReturn(listOf(sampleBreed))

        // When - Check if device is online
        val result = repository.isOnline()

        // Then - Should return true for online status
        assertTrue("Expected online status to be true", result)
        verify(apiService).getBreeds(1, 0)
    }

    // Test that online check returns false when API is unreachable
    @Test
    fun testIsOnlineFalse() = runTest {
        // Given - Mock API failure (network error)
        whenever(apiService.getBreeds(1, 0)).thenThrow(RuntimeException("Network error"))

        // When - Check if device is online
        val result = repository.isOnline()

        // Then - Should return false for offline status
        assertFalse("Expected online status to be false", result)
        verify(apiService).getBreeds(1, 0)
    }

    // Test that app initialization fetches and caches all breeds
    @Test
    fun testInitializeSuccess() = runTest {
        // Given - Mock successful API responses for initialization
        val allBreeds = listOf(sampleBreed, sampleBreed.copy(id = "aege", name = "Aegean"))

        whenever(apiService.getAllBreeds()).thenReturn(allBreeds)
        whenever(apiService.getImage("0XYvRd7oD")).thenReturn(sampleImage)
        whenever(catBreedDao.getBreedById(any())).thenReturn(null)

        // When - Initialize app data
        val result = repository.initializeAppData()

        // Then - Should successfully cache all data and return count
        assertTrue("Expected successful initialization", result.isSuccess)
        assertEquals(2, result.getOrNull())
        verify(apiService).getAllBreeds()
        verify(catBreedDao).insertBreeds(any())
    }
}
