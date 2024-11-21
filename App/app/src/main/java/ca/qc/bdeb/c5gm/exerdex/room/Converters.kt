package ca.qc.bdeb.c5gm.exerdex.room

import androidx.room.TypeConverter
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.data.Set
import ca.qc.bdeb.c5gm.exerdex.data.MuscleCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.sql.Date

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