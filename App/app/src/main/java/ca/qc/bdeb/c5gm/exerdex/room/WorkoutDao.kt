package ca.qc.bdeb.c5gm.exerdex.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ca.qc.bdeb.c5gm.exerdex.data.Workout

@Dao
interface WorkoutDao {
    @Delete
    suspend fun delete(workout: Workout)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg workouts: Workout)
    @Query("SELECT * FROM Workout")
    suspend fun loadAllWorkouts(): MutableList<Workout>

}