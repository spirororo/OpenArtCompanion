package com.example.openartcompanion.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.openartcompanion.data.db.ArtEntity

@Composable
fun ArtItem(
    art: ArtEntity,
    favourites: Boolean = false,
    onClickFavourite: (ArtEntity) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (favourites) {
                IconButton(
                    onClick = {
                        onClickFavourite(art)
                    }
                ) {
                    Icon(
                        imageVector = if (art.isFavorite)
                            Icons.Filled.Favorite
                        else
                            Icons.Outlined.FavoriteBorder,
                        contentDescription = if (art.isFavorite)
                            "Remove from favorites"
                        else
                            "Add to favorites",
                        tint = if (art.isFavorite)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }


            if (!art.primaryImage.isNullOrEmpty()) {
                AsyncImage(
                    model = art.primaryImage,
                    contentDescription = art.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = art.title ?: "Безымянный", style = MaterialTheme.typography.titleMedium)
            Text(text = art.artistDisplayName ?: "Неизвестный")
        }
    }
}