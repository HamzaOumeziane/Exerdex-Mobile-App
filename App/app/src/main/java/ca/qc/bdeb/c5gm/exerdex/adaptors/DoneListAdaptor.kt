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
    val activity: MainActivity,
    val database: ExerciseDatabase,
    val exercisesList: MutableList<Exercise>,
    val doneList: MutableList<Exercise>,
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
        holder.exercise.text = item.name

        holder.comeback.setImageResource(R.drawable.baseline_loop_24)
        holder.comeback.setOnClickListener {
            item.isDone = false
            activity.lifecycleScope.launch(Dispatchers.IO){
                database.exerciseDao().updateAll(item)
                activity.reloadDataFromDatabase()
            }
        }
    }
}