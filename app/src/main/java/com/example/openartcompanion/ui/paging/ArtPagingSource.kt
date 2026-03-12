package com.example.openartcompanion.ui.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.openartcompanion.data.api.MetApiService
import com.example.openartcompanion.data.db.ArtDao
import com.example.openartcompanion.data.db.ArtEntity
import com.example.openartcompanion.data.db.SearchResultDao
import com.example.openartcompanion.data.mapper.ArtMapper
import javax.inject.Inject
import kotlin.math.max

class ArtPagingSource @Inject constructor(
    private val searchResultDao: SearchResultDao,
    private val api: MetApiService,
    private val artDao: ArtDao,
) : PagingSource<Int, ArtEntity>() {

    lateinit var searchId: String

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArtEntity> {
        val searchId = searchId
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
                itemsAfter = max(totalCount - ((page + 1) * perPage), 0)
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
