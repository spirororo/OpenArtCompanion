package com.example.openartcompanion.data.api

data class SearchResponse(
    val total: Int,
    val objectIDs: List<Int>?
)