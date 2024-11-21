package ca.qc.bdeb.c5gm.exerdex.adaptors

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.ArchivedWorkouts
import ca.qc.bdeb.c5gm.exerdex.R
import ca.qc.bdeb.c5gm.exerdex.data.Workout
import ca.qc.bdeb.c5gm.exerdex.viewholders.ItemWorkoutHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutListAdaptor(val ctx: Context, val activity: ArchivedWorkouts, var data: MutableList<Workout>
): RecyclerView.Adapter<ItemWorkoutHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemWorkoutHolder {
        val view = LayoutInflater.from(ctx).inflate(
            R.layout.list_workout_item,parent,false
        )
        return ItemWorkoutHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ItemWorkoutHolder, position: Int) {
        val item = data[position]

        holder.workoutName.text = item.name
        holder.workoutDate.text = item.date.toString()
        holder.deleteBtn.setImageResource(R.drawable.baseline_delete_24_wh)
        holder.workoutVolume.text = item.totalVolumne.toString()+"lbs"
        val setsStringBuilder = StringBuilder()

        item.exerciseList.forEachIndexed { index, exercise ->
            setsStringBuilder.append(exercise)
            // Pour ne pas avoir de new line au dernier element
            if (index < item.exerciseList.size - 1) {
                setsStringBuilder.append("\n")
            }
        }
        holder.workoutSummary.text = setsStringBuilder.toString()
        holder.deleteBtn.setOnClickListener {
            activity.lifecycleScope.launch(Dispatchers.IO){
                activity.database.workoutDao().delete(item)
            }
            data.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
        }
    }

}
