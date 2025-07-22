package com.example.catbreeds.repository

import com.example.catbreeds.data.local.dao.CatBreedDao
import com.example.catbreeds.data.mapper.toDomainModel
import com.example.catbreeds.data.mapper.toDomainModelList
import com.example.catbreeds.data.mapper.toEntity
import com.example.catbreeds.data.mapper.toEntityList
import com.example.catbreeds.model.CatBreed
import com.example.catbreeds.network.CatApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatRepository @Inject constructor(
    private val apiService: CatApiService,
    private val catBreedDao: CatBreedDao
) {

    // Fetches all breeds from API and caches them on app startup
    suspend fun initializeAppData(): Result<Int> {
        return try {
            val allBreeds = apiService.getAllBreeds()

            val breedsWithImages = coroutineScope {
                allBreeds.map { breed ->
                    async {
                        if (breed.reference_image_id != null) {
                            try {
                                val image = apiService.getImage(breed.reference_image_id)
                                breed.copy(image = image)
                            } catch (e: Exception) {
                                breed
                            }
                        } else {
                            breed
                        }
                    }
                }.awaitAll()
            }

            cacheBreeds(breedsWithImages)
            Result.success(allBreeds.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isOnline(): Boolean {
        return try {
            apiService.getBreeds(limit = 1, page = 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getTotalBreedsCount(): Result<Int> {
        return try {
            val allBreeds = apiService.getAllBreeds()
            Result.success(allBreeds.size)
        } catch (e: Exception) {
            try {
                val cachedCount = catBreedDao.getBreedsCount()
                Result.success(cachedCount)
            } catch (cacheException: Exception) {
                Result.failure(e)
            }
        }
    }

    // Online-first: tries API first, falls back to cached data on failure
    suspend fun getBreeds(limit: Int = 10, page: Int = 0): Result<List<CatBreed>> {
        return try {
            val breeds = apiService.getBreeds(limit = limit, page = page)

            val breedsWithImages = coroutineScope {
                breeds.map { breed ->
                    async {
                        if (breed.reference_image_id != null) {
                            try {
                                val image = apiService.getImage(breed.reference_image_id)
                                breed.copy(image = image)
                            } catch (e: Exception) {
                                breed
                            }
                        } else {
                            breed
                        }
                    }
                }.awaitAll()
            }

            // Cache the breeds (this preserves favorite status)
            cacheBreeds(breedsWithImages)

            // Return the cached breeds with correct favorite status, not the API breeds
            val offset = page * limit
            val cachedBreeds = catBreedDao.getBreedsPaginated(limit, offset)
            Result.success(cachedBreeds.toDomainModelList())

        } catch (e: Exception) {
            try {
                val offset = page * limit
                val cachedBreeds = catBreedDao.getBreedsPaginated(limit, offset)
                Result.success(cachedBreeds.toDomainModelList())
            } catch (cacheException: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchBreeds(query: String): Result<List<CatBreed>> {
        return try {
            val breeds = apiService.searchBreeds(query)

            val breedsWithImages = coroutineScope {
                breeds.map { breed ->
                    async {
                        if (breed.reference_image_id != null) {
                            try {
                                val image = apiService.getImage(breed.reference_image_id)
                                breed.copy(image = image)
                            } catch (e: Exception) {
                                breed
                            }
                        } else {
                            breed
                        }
                    }
                }.awaitAll()
            }

            // Cache the breeds (this preserves favorite status)
            cacheBreeds(breedsWithImages)

            // Return the cached breeds with correct favorite status
            val searchResults = catBreedDao.searchBreeds(query).first()
            Result.success(searchResults.toDomainModelList())

        } catch (e: Exception) {
            try {
                val searchResults = catBreedDao.searchBreeds(query).first()
                Result.success(searchResults.toDomainModelList())
            } catch (cacheException: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getBreedsFlow(): Flow<List<CatBreed>> {
        return catBreedDao.getAllBreeds().map { it.toDomainModelList() }
    }

    suspend fun getBreedById(breedId: String): Result<CatBreed> {
        return try {
            val breed = catBreedDao.getBreedById(breedId)?.toDomainModel()
            if (breed != null) {
                Result.success(breed)
            } else {
                Result.failure(Exception("Breed not found with ID: $breedId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Uses REPLACE strategy - updates existing breeds or inserts new ones while preserving favorite status
    private suspend fun cacheBreeds(breeds: List<CatBreed>) {
        val entities = breeds.map { breed ->
            // Check if this breed already exists in the database
            val existingBreed = catBreedDao.getBreedById(breed.id)
            val preservedFavoriteStatus = existingBreed?.isFavorite ?: false

            // Convert to entity while preserving the favorite status
            breed.copy(isFavorite = preservedFavoriteStatus).toEntity()
        }
        catBreedDao.insertBreeds(entities)
    }

    suspend fun hasCachedData(): Boolean {
        return catBreedDao.getBreedsCount() > 0
    }

    suspend fun getCachedBreedsCount(): Int {
        return catBreedDao.getBreedsCount()
    }

    suspend fun clearCache(): Result<Unit> {
        return try {
            catBreedDao.deleteAllBreeds()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Favorite operations
    suspend fun toggleFavoriteStatus(breedId: String): Result<Unit> {
        return try {
            val currentBreed = catBreedDao.getBreedById(breedId)
            if (currentBreed != null) {
                val newFavoriteStatus = !currentBreed.isFavorite
                catBreedDao.updateFavoriteStatus(breedId, newFavoriteStatus)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Breed not found with ID: $breedId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFavoriteBreeds(): Flow<List<CatBreed>> {
        return catBreedDao.getFavoriteBreeds().map { it.toDomainModelList() }
    }

    suspend fun getFavoriteBreedsCount(): Int {
        return catBreedDao.getFavoriteBreedsCount()
    }
}
