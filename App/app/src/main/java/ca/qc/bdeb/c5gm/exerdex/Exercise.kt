package ca.qc.bdeb.c5gm.exerdex

import android.content.Context
import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize
import java.sql.Date


@Parcelize
@Entity
data class Exercise(
    var name: String,
    val description: String = "",
    val category: MuscleCategory,
    val setList: List<Set>,
    var isDone: Boolean = false,
    var imageUri: String? = null,
    @PrimaryKey(autoGenerate = true) val exId: Int = 0
) : Parcelable

enum class MuscleCategory {
    CHEST, BACK, BICEPS, TRICEPS, QUADS, HAMSTRINGS, CALVES, ABS, FOREARMS, SHOULDERS, TRAPS, NECK, CARDIO
}

@Parcelize
data class Set(
    val setOrder: Int,
    val weight: Float,
    val reps: Int
) : Parcelable {
    override fun toString(): String {
        return "Set $setOrder : $weight : lbs x $reps reps"
    }
}

@Parcelize
@Entity
data class Workout(
    val name: String,
    val date: Date,
    val exerciseList: List<String>, // Non une relation car ils sont utilisé pour affichage seulement
    // et sont effacés de la table 'Exercise'. On ne peut plus les modifier après une finalisation workout.
    val totalVolumne: Int,
    @PrimaryKey(autoGenerate = true) val workoutId: Int = 0
) : Parcelable

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

@Dao
interface WorkoutDao {
    @Delete
    suspend fun delete(workout: Workout)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg workouts: Workout)
    @Query("SELECT * FROM Workout")
    suspend fun loadAllWorkouts(): MutableList<Workout>

}

@Database(entities = [Exercise::class, Workout::class],version = 5)
@TypeConverters(Converters::class)
    abstract class ExerciseDatabase: RoomDatabase(){
        abstract fun exerciseDao(): ExerciseDao
        abstract fun workoutDao(): WorkoutDao

        companion object {
            @Volatile
            private var INSTANCE: ExerciseDatabase? = null
            fun getExerciseDatabase(context: Context): ExerciseDatabase{
                return INSTANCE?: synchronized(this){
                    val instance =
                        Room.databaseBuilder(context.applicationContext,
                            ExerciseDatabase::class.java, "sqllite_database").
                                fallbackToDestructiveMigration().
                                build()
                    INSTANCE = instance
                    instance
                }
            }
        }
    }

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromSetList(setList: List<Set>): String {
        return gson.toJson(setList)
    }

    @TypeConverter
    fun toSetList(setListString: String): List<Set> {
        val listType = object : TypeToken<List<Set>>() {}.type
        return gson.fromJson(setListString, listType)
    }

    @TypeConverter
    fun fromMuscleCategory(category: MuscleCategory): String {
        return category.name
    }

    @TypeConverter
    fun toMuscleCategory(categoryString: String): MuscleCategory {
        return MuscleCategory.valueOf(categoryString)
    }

    @TypeConverter
    fun fromDate(date: Date): Long = date.time

    @TypeConverter
    fun toDate(timestamp: Long): Date = Date(timestamp)

    @TypeConverter
    fun fromExerciseList(exercises: List<Exercise>): String = gson.toJson(exercises)

    @TypeConverter
    fun toExerciseList(exercisesString: String): List<Exercise> {
        val listType = object : TypeToken<List<Exercise>>() {}.type
        return gson.fromJson(exercisesString, listType)
    }
    @TypeConverter
    fun fromStringList(stringList: List<String>): String {
        return gson.toJson(stringList)
    }

    @TypeConverter
    fun toStringList(stringListString: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(stringListString, listType)
    }
}
