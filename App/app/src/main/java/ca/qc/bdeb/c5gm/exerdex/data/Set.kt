package ca.qc.bdeb.c5gm.exerdex.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Set(
    val setOrder: Int,
    val weight: Float,
    val reps: Int
) : Parcelable {
    override fun toString(): String {
        return "Set $setOrder : $weight lbs x $reps reps"
    }
}