package ca.qc.bdeb.c5gm.exerdex

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Exercise(
    val name: String,
    val description: String = "",
    val category: MuscleCategory,
    val setList: List<Set>
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