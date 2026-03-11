package com.example.openartcompanion.ui.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.openartcompanion.data.api.MetApiService
import com.example.openartcompanion.data.db.ArtDao
import com.example.openartcompanion.data.db.ArtEntity
import com.example.openartcompanion.data.db.SearchResultDao
import com.example.openartcompanion.data.mapper.ArtMapper
import com.example.openartcompanion.data.repository.ArtRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtPagingSourceFactory @Inject constructor(
    private val searchResultDao: SearchResultDao,
    private val api: MetApiService,
    private val artDao: ArtDao
) {
    fun create(searchId: String): ArtPagingSource {
        return ArtPagingSource(
            searchResultDao = searchResultDao,
            api = api,
            artDao = artDao,
            searchId = searchId
        )
    }
}

class ArtPagingSource(
    private val searchResultDao: SearchResultDao,
    private val api: MetApiService,
    private val artDao: ArtDao,
    private val searchId: String
) : PagingSource<Int, ArtEntity>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArtEntity> {
        val page = params.key ?: 0
        val perPage = params.loadSize

        return try {
            val totalCount = searchResultDao.getTotalCount(searchId)

            if (page * perPage >= totalCount) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (page > 0) page - 1 else null,
                    nextKey = null
                )
            }

            val ids = searchResultDao.getIdsForPage(
                searchId = searchId,
                offset = page * perPage,
                limit = perPage
            )

            val results = ids.mapNotNull { id ->
                artDao.getArtById(id) ?: run {
                    try {
                        val dto = api.getObjectDetails(id)
                        val entity = ArtMapper.dtoToEntity(dto)
                        artDao.insertArt(entity)
                        searchResultDao.markDetailsFetched(id)
                        entity
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            LoadResult.Page(
                data = results,
                prevKey = if (page > 0) page - 1 else null,
                nextKey = if ((page + 1) * perPage < totalCount) page + 1 else null,
                itemsBefore = page * perPage,
                itemsAfter = totalCount - ((page + 1) * perPage)
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ArtEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val closestPage = state.closestPageToPosition(anchorPosition)
            closestPage?.prevKey?.plus(1) ?: closestPage?.nextKey?.minus(1)
        }
    }

}
