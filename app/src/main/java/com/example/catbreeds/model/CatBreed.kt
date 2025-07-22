package com.example.catbreeds.model

data class CatImage(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int,
    val mime_type: String? = null
)

data class CatBreed(
    val id: String,
    val name: String,
    val description: String?,
    val origin: String?,
    val temperament: String?,
    val life_span: String?,
    val reference_image_id: String?,
    val image: CatImage? = null, // This field will be populated later when fetching the image
    val isFavorite: Boolean = false
)