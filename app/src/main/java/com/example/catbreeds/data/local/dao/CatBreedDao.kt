package com.example.catbreeds.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.catbreeds.data.local.entity.CatBreedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatBreedDao {

    @Query("SELECT * FROM cat_breeds ORDER BY name ASC")
    fun getAllBreeds(): Flow<List<CatBreedEntity>>

    @Query("SELECT * FROM cat_breeds ORDER BY name ASC LIMIT :limit OFFSET :offset")
    suspend fun getBreedsPaginated(limit: Int, offset: Int): List<CatBreedEntity>

    @Query("SELECT * FROM cat_breeds WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchBreeds(query: String): Flow<List<CatBreedEntity>>

    @Query("SELECT * FROM cat_breeds WHERE id = :breedId")
    suspend fun getBreedById(breedId: String): CatBreedEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreeds(breeds: List<CatBreedEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreed(breed: CatBreedEntity)

    @Query("DELETE FROM cat_breeds")
    suspend fun deleteAllBreeds()

    @Query("SELECT COUNT(*) FROM cat_breeds")
    suspend fun getBreedsCount(): Int

    @Query("SELECT MAX(lastUpdated) FROM cat_breeds")
    suspend fun getLastUpdateTime(): Long?
}
