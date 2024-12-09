package ca.qc.bdeb.c5gm.exerdex.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.R
import ca.qc.bdeb.c5gm.exerdex.data.User

class ProfileViewHolder (
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {

    val layout: ConstraintLayout
    val mainIcon: ImageView
    val textValue: TextView
    val editIcon: ImageView

    init {
        layout = itemView as ConstraintLayout
        mainIcon = itemView.findViewById(R.id.iconValueId)
        textValue = itemView.findViewById(R.id.valueProfileId)
        editIcon = itemView.findViewById(R.id.editProfileId)
    }
}