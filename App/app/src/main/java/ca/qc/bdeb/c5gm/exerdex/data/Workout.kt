package ca.qc.bdeb.c5gm.exerdex.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.sql.Date

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
