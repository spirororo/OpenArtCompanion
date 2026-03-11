package com.example.openartcompanion.di

import android.content.Context
import androidx.room.Room
import com.example.openartcompanion.data.db.AppDatabase
import com.example.openartcompanion.data.db.ArtDao
import com.example.openartcompanion.data.db.DepartmentDao
import com.example.openartcompanion.data.db.LastSearchDao
import com.example.openartcompanion.data.db.SearchResult
import com.example.openartcompanion.data.db.SearchResultDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "art_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideArtDao(database: AppDatabase): ArtDao {
        return database.artDao()
    }

    @Provides
    fun provideDepartmentDao(database: AppDatabase): DepartmentDao {
        return database.departmentsDao()
    }

    @Provides
    fun provideSearchResultDao(database: AppDatabase): SearchResultDao {
        return database.searchResultDao()
    }

    @Provides
    fun provideLastSearchDao(database: AppDatabase): LastSearchDao {
        return database.lastSearchDao()
    }
}