package com.example.catbreeds.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.catbreeds.data.local.dao.CatBreedDao
import com.example.catbreeds.data.local.entity.CatBreedEntity

@Database(
    entities = [CatBreedEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CatBreedsDatabase : RoomDatabase() {

    abstract fun catBreedDao(): CatBreedDao

    companion object {
        const val DATABASE_NAME = "cat_breeds_database"
    }
}
