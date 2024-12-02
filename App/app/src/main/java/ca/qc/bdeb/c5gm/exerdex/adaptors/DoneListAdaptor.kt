package ca.qc.bdeb.c5gm.exerdex.adaptors

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.MainActivity
import ca.qc.bdeb.c5gm.exerdex.R
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.room.ExerciseDatabase
import ca.qc.bdeb.c5gm.exerdex.viewholders.ItemDoneHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DoneListAdaptor(
    val ctx: Context,
    var doneList: MutableList<Exercise>,
    private val returnExercise: (item: Exercise) -> Unit,
) : RecyclerView.Adapter<ItemDoneHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemDoneHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.exercise_done_item, parent, false)
        return ItemDoneHolder(view)
    }

    override fun getItemCount(): Int {
        return doneList.size
    }

    override fun onBindViewHolder(holder: ItemDoneHolder, position: Int) {
        val item = doneList[holder.adapterPosition]
        holder.exercise.text = item.exerciseRawData.name

        holder.comeback.setImageResource(R.drawable.baseline_loop_24)
        holder.comeback.setOnClickListener {
            returnExercise(item)
        }
    }
}