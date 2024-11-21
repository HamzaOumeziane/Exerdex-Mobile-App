package ca.qc.bdeb.c5gm.exerdex.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.R

class ItemDoneHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val exercise: TextView
    val comeback: ImageView

    init{
        exercise = itemView.findViewById(R.id.doneTextView)
        comeback = itemView.findViewById(R.id.comebackImageView)
    }
}