package com.example.openartcompanion.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "department_table")
data class DepartmentEntity(
    @PrimaryKey val objectID: Int,
    val name: String?
)