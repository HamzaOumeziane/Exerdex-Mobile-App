package ca.qc.bdeb.c5gm.exerdex.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.R

class ItemSetHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    val layout: ConstraintLayout
    val setTitle: TextView
    val setContent: TextView
    val cancelSetBtn: ImageView

    init {
        layout = itemView as ConstraintLayout
        setTitle = itemView.findViewById(R.id.setTitle)
        setContent = itemView.findViewById(R.id.setContent)
        cancelSetBtn = itemView.findViewById(R.id.cancelSetImg)
        cancelSetBtn.setImageResource(R.drawable.baseline_cancel_24_wh)
    }

}