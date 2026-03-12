package com.example.openartcompanion.data.mapper

import com.example.openartcompanion.data.api.ArtObjectDto
import com.example.openartcompanion.data.api.Department
import com.example.openartcompanion.data.db.ArtEntity
import com.example.openartcompanion.data.db.DepartmentEntity

object ArtMapper {

    fun dtoToEntity(dto: ArtObjectDto): ArtEntity {
        return ArtEntity(
            objectID = dto.objectID,
            title = dto.title,
            artistDisplayName = dto.artistDisplayName,
            department = dto.department,
            objectDate = dto.objectDate,
            medium = dto.medium,
            primaryImage = dto.primaryImage,
            isFavorite = false
        )
    }

    fun departmentDtoToEntity(dto: Department): DepartmentEntity {
        return DepartmentEntity(
            objectID = dto.departmentId,
            name = dto.displayName,
        )
    }
}
