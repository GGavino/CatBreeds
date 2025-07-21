package com.example.catbreeds.repository

import com.example.catbreeds.data.local.dao.CatBreedDao
import com.example.catbreeds.data.mapper.toDomainModel
import com.example.catbreeds.data.mapper.toDomainModelList
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

            cacheBreeds(breedsWithImages)
            Result.success(breedsWithImages)

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

            cacheBreeds(breedsWithImages)
            Result.success(breedsWithImages)

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

    suspend fun getBreedById(breedId: String): CatBreed? {
        return catBreedDao.getBreedById(breedId)?.toDomainModel()
    }

    // Uses REPLACE strategy - updates existing breeds or inserts new ones
    private suspend fun cacheBreeds(breeds: List<CatBreed>) {
        val entities = breeds.toEntityList()
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
}
