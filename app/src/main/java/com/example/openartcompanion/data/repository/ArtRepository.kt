package com.example.openartcompanion.data.repository

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import coil.util.Logger
import com.example.openartcompanion.data.api.Department
import com.example.openartcompanion.data.api.MetApiService
import com.example.openartcompanion.data.api.SearchResponse
import com.example.openartcompanion.data.db.ArtDao
import com.example.openartcompanion.data.db.ArtEntity
import com.example.openartcompanion.data.db.DepartmentDao
import com.example.openartcompanion.data.db.DepartmentEntity
import com.example.openartcompanion.data.db.LastSearch
import com.example.openartcompanion.data.db.LastSearchDao
import com.example.openartcompanion.data.db.SearchResult
import com.example.openartcompanion.data.db.SearchResultDao
import com.example.openartcompanion.data.mapper.ArtMapper
import com.example.openartcompanion.di.IoDispatcher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class ArtRepository @Inject constructor(
    private val api: MetApiService,
    private val dao: ArtDao,
    private val departmentsDao: DepartmentDao,
    private val searchResultDao: SearchResultDao,
    private val lastSearchDao: LastSearchDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    // Получить все работы из базы
    fun getAllArt(): Flow<List<ArtEntity>> = dao.getAllArt()

    // Получить избранное
    fun getFavoriteArt(): PagingSource<Int, ArtEntity> = dao.getFavoriteArt()

    // Получить список департаментов
    fun getAllDepartments(): Flow<List<DepartmentEntity>> = departmentsDao.getAllDepartments()

    // Получить детальную работу по ID (сначала из базы, если есть)
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
        val local = departmentsDao.getAllDepartments()
        var departments: List<DepartmentEntity> = listOf();

        local.collect { list ->
            if (list.isEmpty()) {
                val remote = api.getDepartments()
                val entities = remote.departments.mapNotNull {
                        it -> ArtMapper.departmentDtoToEntity(it)
                }
                departmentsDao.insertAll(entities)
                departments = entities
            } else {
                departments = list
            }
        }

        return departments
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

        withContext(ioDispatcher) {
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
        }

        return searchId
    }

    suspend fun getLastSearchId(): String? {
        return withContext(ioDispatcher) {
            lastSearchDao.getLastSearchId()
        }
    }
}
