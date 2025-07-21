package com.example.catbreeds.di

import android.content.Context
import androidx.room.Room
import com.example.catbreeds.data.local.database.CatBreedsDatabase
import com.example.catbreeds.data.local.dao.CatBreedDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CatBreedsDatabase {
        return Room.databaseBuilder(
            context,
            CatBreedsDatabase::class.java,
            CatBreedsDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideCatBreedDao(database: CatBreedsDatabase): CatBreedDao {
        return database.catBreedDao()
    }
}
