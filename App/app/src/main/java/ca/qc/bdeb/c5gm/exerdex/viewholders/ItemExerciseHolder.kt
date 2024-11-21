package ca.qc.bdeb.c5gm.exerdex.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.R

class ItemExerciseHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val layout: ConstraintLayout
    val exercise: TextView
    val category: TextView
    val important: ImageView
    val sets: TextView
    val check: ImageView
    val edit: ImageView
    val cancel: ImageView

    init {
        layout = itemView as ConstraintLayout
        exercise = itemView.findViewById(R.id.exerciseTextView)
        category = itemView.findViewById(R.id.categoryTextView)
        important = itemView.findViewById(R.id.importantImageView)
        sets = itemView.findViewById(R.id.setsTextView)
        check = itemView.findViewById(R.id.checkView)
        edit = itemView.findViewById(R.id.editView)
        cancel = itemView.findViewById(R.id.deleteView)
    }
}