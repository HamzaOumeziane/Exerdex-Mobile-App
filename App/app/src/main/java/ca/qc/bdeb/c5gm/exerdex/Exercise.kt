package ca.qc.bdeb.c5gm.exerdex

import android.content.Context
import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity
data class Exercise(
    val name: String,
    val description: String = "",
    val category: MuscleCategory,
    val setList: List<Set>,
    var isDone: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    val exId: Int = -1,
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

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM Exercise WHERE isDone = :isDone")
    suspend fun loadExerciseByDone(isDone: Boolean): List<Exercise>
    @Update
    suspend fun updateAll(vararg exercises: Exercise)
    @Delete
    suspend fun delete(exercise: Exercise)
}

@Database(entities = [Exercise::class],version = 1)
    abstract class ExerciseDatabase: RoomDatabase(){
        abstract fun exerciseDao(): ExerciseDao

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
