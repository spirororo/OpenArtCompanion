package com.example.openartcompanion.data.api

data class ArtObjectDto(
    val objectID: Int,
    val title: String?,
    val artistDisplayName: String?,
    val department: String?,
    val objectDate: String?,
    val medium: String?,
    val primaryImage: String?,
    val primaryImageSmall: String?,
    val isPublicDomain: Boolean?
)