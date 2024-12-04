package ca.qc.bdeb.c5gm.exerdex.api

import ca.qc.bdeb.c5gm.exerdex.data.ExerciseFromAPI
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiExerciseService {
    @GET("exercises")
    suspend fun getExercisesByName(@Query("name") name: String): Response<List<ExerciseFromAPI>>
}