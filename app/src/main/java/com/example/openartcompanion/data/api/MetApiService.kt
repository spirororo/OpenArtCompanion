package com.example.openartcompanion.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MetApiService {

    @GET("search")
    suspend fun searchObjects(
        @Query("q") query: String? = null,
        @Query("hasImages") hasImages: Boolean? = null,
        @Query("isOnView") isOnView: Boolean? = null,
        @Query("departmentId") departmentId: Int? = null,
    ): SearchResponse

    @GET("objects/{objectID}")
    suspend fun getObjectDetails(
        @Path("objectID") objectId: Int
    ): ArtObjectDto

    @GET("departments")
    suspend fun getDepartments(): DepartmentsResponse
}