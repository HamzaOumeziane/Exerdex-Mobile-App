package ca.qc.bdeb.c5gm.exerdex.adaptors

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.AddEditExerciseActivity
import ca.qc.bdeb.c5gm.exerdex.MainActivity
import ca.qc.bdeb.c5gm.exerdex.R
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.data.ExerciseRaw
import ca.qc.bdeb.c5gm.exerdex.viewholders.ItemExerciseRawHolder
import ca.qc.bdeb.c5gm.exerdex.viewholders.ItemSetHolder

class ExerciseRawListAdaptor (
    val ctx: Context,
    val activity: Activity,
    var data: List<ExerciseRaw>,
    private val addExercise: (isEdit: Boolean, exerciseToEdit: Exercise?, exerciseRaw: ExerciseRaw?) -> Unit,
) : RecyclerView.Adapter<ItemExerciseRawHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemExerciseRawHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.exercise_raw_item, parent, false)
        return ItemExerciseRawHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ItemExerciseRawHolder, position: Int) {
        val item = data[position]
        holder.exerciseRawName.text = item.name
        holder.itemView.setOnClickListener {
            addExercise(false, null, item)
        }
    }

}