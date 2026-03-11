package com.example.openartcompanion.data.db

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface SearchResultDao {

    @Query("""
        SELECT art_table.* FROM search_results
        JOIN art_table ON search_results.objectId = art_table.objectID
        WHERE search_results.searchId = :searchId
        ORDER BY search_results.position
    """)
    fun getSearchResults(searchId: String): PagingSource<Int, ArtEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(results: List<SearchResult>)

    @Query("DELETE FROM search_results WHERE searchId = :searchId")
    suspend fun clearSearchResults(searchId: String)

    @Query("UPDATE search_results SET isDetailsFetched = 1 WHERE objectId = :objectId")
    suspend fun markDetailsFetched(objectId: Int)

    @Query("""
        SELECT objectId FROM search_results 
        WHERE searchId = :searchId 
        ORDER BY position 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getIdsForPage(
        searchId: String,
        offset: Int,
        limit: Int
    ): List<Int>


    @Query("SELECT COUNT(*) FROM search_results WHERE searchId = :searchId")
    suspend fun getTotalCount(searchId: String): Int
}

@Dao
interface LastSearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLastSearch(lastSearch: LastSearch)

    @Query("SELECT * FROM last_search WHERE id = 1")
    suspend fun getLastSearch(): LastSearch?

    @Query("SELECT searchId FROM last_search WHERE id = 1")
    suspend fun getLastSearchId(): String?

    @Query("DELETE FROM last_search")
    suspend fun clearLastSearch()
}