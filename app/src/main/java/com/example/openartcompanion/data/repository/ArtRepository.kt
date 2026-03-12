package com.example.openartcompanion.data.repository

import androidx.paging.PagingSource
import com.example.openartcompanion.data.api.MetApiService
import com.example.openartcompanion.data.db.ArtDao
import com.example.openartcompanion.data.db.ArtEntity
import com.example.openartcompanion.data.db.DepartmentDao
import com.example.openartcompanion.data.db.DepartmentEntity
import com.example.openartcompanion.data.db.LastSearch
import com.example.openartcompanion.data.db.LastSearchDao
import com.example.openartcompanion.data.db.SearchResult
import com.example.openartcompanion.data.db.SearchResultDao
import com.example.openartcompanion.data.mapper.ArtMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtRepository @Inject constructor(
    private val api: MetApiService,
    private val dao: ArtDao,
    private val departmentsDao: DepartmentDao,
    private val searchResultDao: SearchResultDao,
    private val lastSearchDao: LastSearchDao,
) {

    fun getFavoriteArt(): PagingSource<Int, ArtEntity> = dao.getFavoriteArt()

    suspend fun getAllDepartments(): List<DepartmentEntity> {
        return departmentsDao.getAllDepartments()
    }

    suspend fun getArtById(objectID: Int): ArtEntity? {
        val local = dao.getArtById(objectID)
        return local ?: run {
            val remote = api.getObjectDetails(objectID)
            val entity = ArtMapper.dtoToEntity(remote)
            dao.insertArt(entity)
            entity
        }
    }

    suspend fun clearArt() {
        dao.clearAllArt()
    }

    suspend fun updateArt(art: ArtEntity) {
        dao.updateArt(art)
    }

    suspend fun loadDepartments() : List<DepartmentEntity> {
        var departmentsList = departmentsDao.getAllDepartments()
        if (departmentsList.isEmpty()) {
            val remote = api.getDepartments()
            departmentsList = remote.departments.mapNotNull {
                ArtMapper.departmentDtoToEntity(it)
            }
            departmentsDao.insertAll(departmentsList)
        }

        return departmentsList
    }

    private fun generateSearchId(
        query: String?,
        hasImages: Boolean?,
        isOnView: Boolean?,
        departmentId: Int?
    ): String {
        return listOf(
            query ?: "",
            hasImages.toString(),
            isOnView.toString(),
            departmentId.toString()
        ).joinToString("_").hashCode().toString()
    }

    suspend fun performSearch(
        query: String? = null,
        hasImages: Boolean? = true,
        isOnView: Boolean? = null,
        departmentId: Int? = null
    ): String {
        val searchId = generateSearchId(query, hasImages, isOnView, departmentId)

        var q = query
        if (query == null && departmentId != null) q = "" // 502 ошибка на null q

        val response = api.searchObjects(q, hasImages, isOnView, departmentId)
        val objectIds = response.objectIDs ?: emptyList()

        searchResultDao.clearSearchResults(searchId)

        val searchResults = objectIds.mapIndexed { index, id ->
            SearchResult(
                searchId = searchId,
                objectId = id,
                position = index,
                isDetailsFetched = false
            )
        }

        searchResults.chunked(1000).forEach { chunk ->
            searchResultDao.insertSearchResults(chunk)
        }

        lastSearchDao.insertLastSearch(
            LastSearch(
                id = 1,
                searchId = searchId
            )
        )

        return searchId
    }

    suspend fun getLastSearchId(): String? {
        return lastSearchDao.getLastSearchId()
    }
}
