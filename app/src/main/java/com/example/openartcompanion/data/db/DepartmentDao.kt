package com.example.openartcompanion.data.db

import androidx.room.*

@Dao
interface DepartmentDao {

    @Query("SELECT * FROM department_table")
    suspend fun getAllDepartments(): List<DepartmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(art: DepartmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(arts: List<DepartmentEntity>)
}