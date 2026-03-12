package com.example.openartcompanion.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "search_results",
    primaryKeys = ["searchId", "objectId"],
    indices = [Index("searchId"), Index("objectId"), Index("isDetailsFetched")]
)
data class SearchResult(
    val searchId: String,
    val objectId: Int,
    val position: Int,
    val isDetailsFetched: Boolean = false
)

@Entity(tableName = "last_search")
data class LastSearch(
    @PrimaryKey
    val id: Int = 1,
    val searchId: String,
    val timestamp: Long = System.currentTimeMillis()
)
