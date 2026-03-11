package com.example.openartcompanion.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.openartcompanion.ui.components.ArtItem
import com.example.openartcompanion.ui.viewmodel.ArtViewModel

@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: ArtViewModel
) {
    val favourites = viewModel.favouritesPagingFlow.collectAsLazyPagingItems()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = { navController.navigateUp() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (favourites.itemCount == 0) {
            Text(
                text = "Нет результатов",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        LazyColumn {
            items(count = favourites.itemCount) { index ->
                val art = favourites[index]
                art?.let {
                    ArtItem(
                        art = art,
                        favourites = true,
                        { art -> viewModel.toggleFavorite(art) }) {
                        navController.navigate("detail/${art.objectID}")
                    }
                }
            }
            if (favourites.loadState.append == LoadState.Loading) {
                item {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

}