package ca.qc.bdeb.c5gm.exerdex.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.data.Workout

@Database(entities = [Exercise::class, Workout::class],version = 6)
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