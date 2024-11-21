package ca.qc.bdeb.c5gm.exerdex.adaptors

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.AddEditExerciseActivity
import ca.qc.bdeb.c5gm.exerdex.R
import ca.qc.bdeb.c5gm.exerdex.viewholders.ItemSetHolder
import ca.qc.bdeb.c5gm.exerdex.data.Set

class SetListAdaptor(val ctx: Context, val activity: AddEditExerciseActivity, var data: MutableList<Set>):
    RecyclerView.Adapter<ItemSetHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemSetHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.list_set_item, parent, false)
        return ItemSetHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ItemSetHolder, position: Int) {
        val item = data[position]

        holder.setTitle.text = activity.getString(R.string.exo_set_word)+" ${position+1}:"
        holder.setContent.text = "${item.weight} x ${item.reps}"
        holder.cancelSetBtn.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                data.removeAt(pos)
                notifyItemRemoved(pos)
                notifyItemRangeChanged(pos, data.size)
            }
        }
    }


}