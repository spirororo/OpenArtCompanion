package com.example.openartcompanion.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ArtEntity::class,
        DepartmentEntity::class,
        SearchResult::class,
        LastSearch::class],
    version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artDao(): ArtDao
    abstract fun departmentsDao(): DepartmentDao
    abstract fun searchResultDao(): SearchResultDao
    abstract fun lastSearchDao(): LastSearchDao
}
