package ca.qc.bdeb.c5gm.exerdex.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Exercise(
    var name: String,
    val description: String = "",
    val category: MuscleCategory,
    val setList: List<Set>,
    var isDone: Boolean = false,
    var isImportant: Boolean,
    var imageUri: String? = null,
    @PrimaryKey(autoGenerate = true) val exId: Int = 0
) : Parcelable