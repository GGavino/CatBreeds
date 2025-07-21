package com.example.catbreeds.network

import com.example.catbreeds.model.CatBreed
import com.example.catbreeds.model.CatImage
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CatApiService {
    @GET("breeds")
    suspend fun getBreeds(
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int = 0
    ): List<CatBreed>

    @GET("breeds")
    suspend fun getAllBreeds(): List<CatBreed>

    @GET("images/{image_id}")
    suspend fun getImage(
        @Path("image_id") imageId: String
    ): CatImage

    @GET("breeds/search")
    suspend fun searchBreeds(
        @Query("q") query: String
    ): List<CatBreed>
}