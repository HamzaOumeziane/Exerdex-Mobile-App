package ca.qc.bdeb.c5gm.exerdex.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.R

class ItemExerciseRawHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val layout: ConstraintLayout
    val exerciseRawName: TextView

    init {
        layout = itemView as ConstraintLayout
        exerciseRawName = itemView.findViewById(R.id.exerciseRawName)
    }
}