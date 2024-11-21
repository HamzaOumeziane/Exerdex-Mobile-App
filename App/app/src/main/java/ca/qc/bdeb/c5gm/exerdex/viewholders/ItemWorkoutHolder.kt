package ca.qc.bdeb.c5gm.exerdex.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.R

class ItemWorkoutHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val layout: ConstraintLayout
    val workoutName: TextView
    val workoutDate: TextView
    val workoutVolume: TextView
    val workoutSummary: TextView
    val deleteBtn: ImageView

    init {
        layout = itemView as ConstraintLayout
        workoutName = itemView.findViewById(R.id.workoutNameTxt)
        workoutDate = itemView.findViewById(R.id.workoutDateTxt)
        workoutVolume = itemView.findViewById(R.id.workoutVolumeTxt)
        workoutSummary = itemView.findViewById(R.id.exercisesSummaryTxt)
        deleteBtn = itemView.findViewById(R.id.deleteImgBtn)
    }
}