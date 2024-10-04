package ca.qc.bdeb.c5gm.exerdex

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class AddEditExerciseActivity : AppCompatActivity() {
    lateinit var repsTextView: TextView
    lateinit var weightTextView: TextView
    private lateinit var setsRecyclerView: RecyclerView
    private lateinit var setListAdapter: SetListAdaptor
    private var setsList: MutableList<Set> = mutableListOf(
        Set(1,185f,5),
        Set(2,165f,12)
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_edit_exercise)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repsTextView = findViewById(R.id.newSetReps)
        weightTextView = findViewById(R.id.newSetWeight)
        setsRecyclerView = findViewById(R.id.setsRecyclerView)
        setListAdapter = SetListAdaptor(applicationContext, this, setsList)

        setsRecyclerView.adapter = setListAdapter

        val addSetBtn: Button = findViewById(R.id.addSetBtn)
        addSetBtn.setOnClickListener{
            addNewSet()
        }
    }

    private fun addNewSet() {
        val repsInput = repsTextView.text.toString()
        val weightInput = weightTextView.text.toString()

        if (repsInput.isBlank() || weightInput.isBlank()) {
            Toast.makeText(this, "Please enter both reps and weight.", Toast.LENGTH_SHORT).show()
            return
        }

        val reps: Int
        val weight: Float

        try {
            reps = repsInput.toInt()
            weight = weightInput.toFloat()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter valid numbers for reps and weight.", Toast.LENGTH_SHORT).show()
            return
        }

        val setOrder = setsList.size + 1

        val newSet = Set(
            setOrder = setOrder,
            weight = weight,
            reps = reps
        )

        setsList.add(newSet)
        setListAdapter.notifyItemInserted(setsList.size - 1)

        repsTextView.text=""
        weightTextView.text=""
    }
}

class SetItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

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

class SetListAdaptor(val ctx: Context, val activity: AddEditExerciseActivity, var data: MutableList<Set>):
    RecyclerView.Adapter<SetItemHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetItemHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.list_set_item, parent, false)
        return SetItemHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: SetItemHolder, position: Int) {
        val item = data[position]

        holder.setTitle.text = "Set ${position+1}:"
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