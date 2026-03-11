package com.example.openartcompanion.data.api

data class DepartmentsResponse(
    val departments: List<Department>
)

data class Department(
    val departmentId: Int,
    val displayName: String
)