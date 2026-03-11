package com.example.openartcompanion.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartmentDao {

    @Query("SELECT * FROM department_table")
    fun getAllDepartments(): Flow<List<DepartmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(art: DepartmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(arts: List<DepartmentEntity>)
}