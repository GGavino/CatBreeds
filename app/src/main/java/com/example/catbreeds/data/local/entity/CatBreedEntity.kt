package com.example.catbreeds.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_breeds")
data class CatBreedEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val origin: String?,
    val temperament: String?,
    val lifeSpan: String?,
    val referenceImageId: String?,
    val imageUrl: String?,
    val imageWidth: Int?,
    val imageHeight: Int?,
    val imageMimeType: String?,
    val isFavorite: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
