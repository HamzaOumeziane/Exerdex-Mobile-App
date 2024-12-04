package ca.qc.bdeb.c5gm.exerdex.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class ExerciseRaw (
    var name: String,
    val description: String = "",
    val category: MuscleCategory,
    val imageUri: String? = null,
    var userId: String? = null,
    @PrimaryKey(autoGenerate = true) val exRawId: Int = 0
) : Parcelable