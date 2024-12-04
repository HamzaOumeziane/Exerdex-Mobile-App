package ca.qc.bdeb.c5gm.exerdex.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.data.ExerciseRaw

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM Exercise WHERE isDone = :isDone AND userId = :userId")
    suspend fun loadExerciseByDone(isDone: Boolean, userId: String): MutableList<Exercise>
    @Update
    suspend fun updateAll(vararg exercises: Exercise)
    @Delete
    suspend fun delete(exercise: Exercise)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg exercises: Exercise)
    @Query("DELETE FROM Exercise WHERE userId = :userId")
    suspend fun deleteAllExercises(userId: String)
    @Query("DELETE FROM Exercise WHERE isDone = 1 AND userId = :userId")
    suspend fun deleteAllExercisesDone(userId: String)
    @Query("SELECT * FROM Exercise WHERE exerciseRawId = :exerciseRawId AND userId = :userId")
    suspend fun loadExercisesByExerciseRaw(exerciseRawId: Int, userId: String): List<Exercise>
    @Query("Select * FROM ExerciseRaw")
    suspend fun loadAllExerciseRaw(): List<ExerciseRaw>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseRaw(vararg exercisesRaw: ExerciseRaw)
}