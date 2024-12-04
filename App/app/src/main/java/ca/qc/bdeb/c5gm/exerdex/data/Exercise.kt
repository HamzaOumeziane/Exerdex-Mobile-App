package ca.qc.bdeb.c5gm.exerdex.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Exercise(
    val exerciseRawData: ExerciseRaw,
    val exerciseRawId: Int,
    val setList: List<Set>,
    var isDone: Boolean = false,
    var isImportant: Boolean,
    var userId: String,
    @PrimaryKey(autoGenerate = true) val exId: Int = 0
) : Parcelable