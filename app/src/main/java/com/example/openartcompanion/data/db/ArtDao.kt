package com.example.openartcompanion.data.db

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtDao {

    @Query("SELECT * FROM art_table")
    fun getAllArt(): Flow<List<ArtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArt(art: ArtEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(arts: List<ArtEntity>)

    @Update
    suspend fun updateArt(art: ArtEntity)

    @Query("SELECT * FROM art_table WHERE isFavorite = 1")
    fun getFavoriteArt(): PagingSource<Int, ArtEntity>


    @Query("SELECT * FROM art_table WHERE objectID = :id")
    suspend fun getArtById(id: Int): ArtEntity?

    @Query("DELETE FROM art_table")
    suspend fun clearAllArt()
}