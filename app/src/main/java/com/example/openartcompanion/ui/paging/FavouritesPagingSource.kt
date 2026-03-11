package com.example.openartcompanion.ui.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.openartcompanion.data.db.ArtEntity
import com.example.openartcompanion.data.repository.ArtRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlin.math.min
import android.util.Log

class FavouritesPagingSource(
    private val repository: ArtRepository,
) : PagingSource<Int, ArtEntity>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArtEntity> {
        return try {
            val page = params.key ?: 0
            val perPage = params.loadSize


            val artworks: List<ArtEntity> = listOf()

            val start = min(perPage * page, artworks.size)
            val end = min(start + perPage, artworks.size)
            val result = artworks.slice(start..<end)

            if (start >= artworks.size) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (page > 0) page - 1 else null,
                    nextKey = null
                )
            }

            LoadResult.Page(
                data = result,
                prevKey = if (page > 0) page - 1 else null,
                nextKey = if (artworks.isNotEmpty()) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ArtEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
