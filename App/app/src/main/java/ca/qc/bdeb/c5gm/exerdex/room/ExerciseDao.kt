package ca.qc.bdeb.c5gm.exerdex.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ca.qc.bdeb.c5gm.exerdex.data.Exercise

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM Exercise WHERE isDone = :isDone")
    suspend fun loadExerciseByDone(isDone: Boolean): MutableList<Exercise>
    @Update
    suspend fun updateAll(vararg exercises: Exercise)
    @Delete
    suspend fun delete(exercise: Exercise)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg exercises: Exercise)
    @Query("DELETE FROM Exercise")
    suspend fun deleteAllExercises()
    @Query("DELETE FROM Exercise WHERE isDone = 1")
    suspend fun deleteAllExercisesDone()
}