package com.example.catbreeds.data.mapper

import com.example.catbreeds.data.local.entity.CatBreedEntity
import com.example.catbreeds.model.CatBreed
import com.example.catbreeds.model.CatImage

fun CatBreed.toEntity(): CatBreedEntity {
    return CatBreedEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        origin = this.origin,
        temperament = this.temperament,
        lifeSpan = this.life_span,
        referenceImageId = this.reference_image_id,
        imageUrl = this.image?.url,
        imageWidth = this.image?.width,
        imageHeight = this.image?.height,
        imageMimeType = this.image?.mime_type
    )
}

fun CatBreedEntity.toDomainModel(): CatBreed {
    val image = if (imageUrl != null && imageWidth != null && imageHeight != null) {
        CatImage(
            id = referenceImageId ?: "",
            url = imageUrl,
            width = imageWidth,
            height = imageHeight,
            mime_type = imageMimeType
        )
    } else null

    return CatBreed(
        id = this.id,
        name = this.name,
        description = this.description,
        origin = this.origin,
        temperament = this.temperament,
        life_span = this.lifeSpan,
        reference_image_id = this.referenceImageId,
        image = image
    )
}

fun List<CatBreed>.toEntityList(): List<CatBreedEntity> {
    return this.map { it.toEntity() }
}

fun List<CatBreedEntity>.toDomainModelList(): List<CatBreed> {
    return this.map { it.toDomainModel() }
}
