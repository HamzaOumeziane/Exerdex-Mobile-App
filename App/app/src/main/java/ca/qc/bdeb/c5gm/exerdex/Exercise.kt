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

@Database(entities = [Exercise::class],version = 2)
@TypeConverters(Converters::class)
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
}
