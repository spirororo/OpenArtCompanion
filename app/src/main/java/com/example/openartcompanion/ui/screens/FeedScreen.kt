package com.example.openartcompanion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.openartcompanion.data.api.Department
import com.example.openartcompanion.data.db.ArtEntity
import com.example.openartcompanion.data.db.DepartmentEntity
import com.example.openartcompanion.ui.viewmodel.ArtViewModel
import com.example.openartcompanion.ui.components.ArtItem

@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: ArtViewModel
) {

    val searchResultItems = viewModel.searchResultsPagingFlow.collectAsLazyPagingItems()
    val departments by viewModel.departments.collectAsState()

    var isQueryValid = false
    var errorMessage: String? = null
    var filtersExpanded by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0 && filtersExpanded) {
                    filtersExpanded = false
                }

                return Offset.Zero
            }
        }
    }

    if (viewModel.currentFilters.departmentId != null ||
            viewModel.currentFilters.query != null
            && viewModel.currentFilters.query!!.length >= 4) {
        isQueryValid = true
    }

    if (!isQueryValid) {
        errorMessage = "Введите минимум 4 символа или выберите департамент"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .nestedScroll(nestedScrollConnection)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Open Art Companion", style = MaterialTheme.typography.titleLarge)

            TextButton(onClick = { filtersExpanded = !filtersExpanded }) {
                Text(if (filtersExpanded) "Скрыть фильтры" else "Показать фильтры")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.currentFilters.query ?: "",
            onValueChange = { viewModel.setQuery(it) },
            label = { Text("Поиск...") },
            isError = errorMessage != null,
            supportingText = errorMessage?.let {
                { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            enabled = isQueryValid,
            onClick = { viewModel.search() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Найти")
        }

        Button(
            onClick = { navController.navigate("favourites") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Избранное")
        }


        AnimatedVisibility(visible = filtersExpanded) {
            Column {
                DepartmentFilterSegmented(
                    departments,
                    viewModel.currentFilters.departmentId,
                    { id -> viewModel.setDepartmentId(id) })

                Spacer(modifier = Modifier.height(8.dp))

                TwoFilterSegmented(
                    viewModel.currentFilters.hasImages,
                    { it -> viewModel.setHasImages(it) },
                    "С изображениями",
                    "Без изображений"
                )

                Spacer(modifier = Modifier.height(8.dp))

                TwoFilterSegmented(
                    viewModel.currentFilters.isOnView,
                    { it -> viewModel.setIsOnView(it) },
                    "Сейчас выставлены",
                    "Не выставлены"
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (!viewModel.uiState.isLoading
            && searchResultItems.itemCount == 0
            && searchResultItems.loadState.append != LoadState.Loading) {
            Text(
                text = "Нет результатов",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (!viewModel.uiState.isLoading) {
            key(viewModel.searchId) {
                LazyColumn(state = rememberLazyListState()) {
                    items(count = searchResultItems.itemCount) { index ->
                        val art = searchResultItems[index]
                        art?.let {
                            ArtItem(art = art, favourites = false, {}) {
                                navController.navigate("detail/${art.objectID}")
                            }
                        }
                    }
                    item {
                        if (searchResultItems.loadState.append == LoadState.Loading) {
                            CircularProgressIndicator(
                                modifier =
                                    Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }

        if (searchResultItems.itemCount == 0 && viewModel.uiState.isLoading) {
            CircularProgressIndicator(
                modifier =
                    Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun DepartmentFilterSegmented(
    departments: List<DepartmentEntity>,
    selectedDepartmentId: Int?,
    onDepartmentSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val allDepartments = listOf(null to "All") +
            departments.map { it: DepartmentEntity -> it.objectID to it.name }

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        allDepartments.forEach { (id, name) ->
            SegmentedButton(
                selected = selectedDepartmentId == id,
                onClick = { onDepartmentSelected(id) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = allDepartments.indexOf(id to name),
                    count = allDepartments.size
                ),
                modifier = Modifier.height(56.dp),
                label = { Text(name!!) },
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,

                    inactiveContainerColor = Color.White,
                    inactiveContentColor = Color.Black,
                )
            )
        }
    }
}

@Composable
fun TwoFilterSegmented(
    bool: Boolean?,
    onBoolSelected: (Boolean) -> Unit,
    textTrue: String,
    textFalse: String,
    modifier: Modifier = Modifier
) {
    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = MaterialTheme.colorScheme.primary,
        activeContentColor = MaterialTheme.colorScheme.onPrimary,

        inactiveContainerColor = Color.White,
        inactiveContentColor = Color.Black,
    )

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        SegmentedButton(
            selected = bool == true,
            onClick = { onBoolSelected(true) },
            shape = SegmentedButtonDefaults.itemShape(
                index = 0,
                count = 2
            ),
            modifier = Modifier.height(56.dp),
            label = { Text(textTrue) },
            colors = colors
        )
        SegmentedButton(
            selected = bool == false,
            onClick = { onBoolSelected(false) },
            shape = SegmentedButtonDefaults.itemShape(
                index = 1,
                count = 2
            ),
            modifier = Modifier.height(56.dp),
            label = { Text(textFalse) },
            colors = colors
        )
    }
}
