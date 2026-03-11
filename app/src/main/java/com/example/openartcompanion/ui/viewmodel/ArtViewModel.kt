package com.example.openartcompanion.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openartcompanion.data.db.ArtEntity
import com.example.openartcompanion.data.repository.ArtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.openartcompanion.ui.paging.ArtPagingSource
import com.example.openartcompanion.ui.paging.ArtPagingSourceFactory
import com.example.openartcompanion.ui.paging.FavouritesPagingSource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class UIState(
    val isLoading: Boolean = false,
    var art: ArtEntity? = null,
    val error: String? = null
)

data class Filters(
    val query: String? = null,
    val hasImages: Boolean? = null,
    val isOnView: Boolean? = null,
    val departmentId: Int? = null
)

@HiltViewModel
class ArtViewModel @Inject constructor(
    private val repository: ArtRepository,
    private val pagingSourceFactory: ArtPagingSourceFactory
) : ViewModel() {
    var uiState by mutableStateOf(UIState())

    val departments = repository.getAllDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.loadDepartments()
        }

        viewModelScope.launch {
            val lastSearchId = repository.getLastSearchId()
            if (lastSearchId != null) {
                _searchId.value = lastSearchId
            } else {
                // искать по умолчанию
            }
        }
    }

    var currentFilters by mutableStateOf(Filters())

    private val _searchId = MutableStateFlow<String?>(null)
    val searchId = _searchId.asStateFlow()

    val searchResultsPagingFlow = searchId
        .filterNotNull()
        .flatMapLatest { searchId ->
            Pager(
                PagingConfig(
                    pageSize = 5,
                    enablePlaceholders = false,
                    initialLoadSize = 5
                )
            ) {
                pagingSourceFactory.create(searchId = searchId)
            }.flow
        }
        .cachedIn(viewModelScope)

    val favouritesPagingFlow = Pager(PagingConfig(pageSize = 5)) {
        repository.getFavoriteArt()
    }.flow.cachedIn(viewModelScope)

    fun loadArtDetail(objectId: Int) {
        uiState = uiState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val art = repository.getArtById(objectId)
                uiState = uiState.copy(
                    isLoading = false,
                    art = art
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка в загрузке элемента"
                )
            }
        }
    }

    fun search() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val newSearchId = repository.performSearch(
                query = currentFilters.query,
                hasImages = currentFilters.hasImages,
                isOnView = currentFilters.isOnView,
                departmentId = currentFilters.departmentId
            )
            _searchId.value = newSearchId
            uiState = uiState.copy(isLoading = false)
        }
    }

    fun toggleFavorite(art: ArtEntity) {
        viewModelScope.launch {
            repository.updateArt(art.copy(isFavorite = !art.isFavorite))
            uiState = uiState.copy(art = art.copy(isFavorite = !art.isFavorite))
        }
    }

    fun clearArt() {
        viewModelScope.launch {
            repository.clearArt()
        }
    }

    fun setQuery(query: String) {
        currentFilters = currentFilters.copy(query = query)
    }

    fun setHasImages(hasImages: Boolean?) {
        currentFilters = currentFilters.copy(hasImages = hasImages)
    }

    fun setIsOnView(isOnView: Boolean?) {
        currentFilters = currentFilters.copy(isOnView = isOnView)
    }

    fun setDepartmentId(departmentId: Int?) {
        currentFilters = currentFilters.copy(departmentId = departmentId)
    }

}