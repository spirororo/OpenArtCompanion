package com.example.openartcompanion.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "art_table")
data class ArtEntity(
    @PrimaryKey val objectID: Int,
    val title: String?,
    val artistDisplayName: String?,
    val department: String?,
    val objectDate: String?,
    val medium: String?,
    val primaryImage: String?,
    val isFavorite: Boolean = false
)