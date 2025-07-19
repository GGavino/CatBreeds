package com.example.catbreeds.repository

import com.example.catbreeds.model.CatBreed
import com.example.catbreeds.network.CatApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatRepository @Inject constructor(
    private val apiService: CatApiService
) {
    suspend fun getBreeds(limit: Int = 10): Result<List<CatBreed>> {
        return try {
            // First, get the breeds
            val breeds = apiService.getBreeds(limit = limit)

            // Then fetch images for each breed that has a reference_image_id
            val breedsWithImages = coroutineScope {
                breeds.map { breed ->
                    async {
                        if (breed.reference_image_id != null) {
                            try {
                                val image = apiService.getImage(breed.reference_image_id)
                                breed.copy(image = image)
                            } catch (e: Exception) {
                                // If image fetch fails, return breed without image
                                breed
                            }
                        } else {
                            breed
                        }
                    }
                }.awaitAll()
            }

            Result.success(breedsWithImages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
