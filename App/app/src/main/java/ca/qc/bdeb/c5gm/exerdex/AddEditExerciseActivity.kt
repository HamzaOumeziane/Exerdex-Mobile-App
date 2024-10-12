package ca.qc.bdeb.c5gm.exerdex

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AddEditExerciseActivity : AppCompatActivity() {
    lateinit var repsTextView: TextView
    lateinit var weightTextView: TextView
    private lateinit var setsRecyclerView: RecyclerView
    private lateinit var setListAdapter: SetListAdaptor
    private lateinit var exerciseTitleView: TextView
    private lateinit var exerciseDescriptionView: TextView
    private lateinit var selectedCategory: MuscleCategory
    private var setsList: MutableList<Set> = mutableListOf()
    private var isEditing: Boolean = false
    private var exerciseBeingEditedId: Int? = null

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
        exerciseTitleView = findViewById(R.id.exerciseNameInput)
        exerciseDescriptionView = findViewById(R.id.exerciseDescInput)
        setsRecyclerView = findViewById(R.id.setsRecyclerView)
        setListAdapter = SetListAdaptor(applicationContext, this, setsList)
        setsRecyclerView.adapter = setListAdapter
        initializeCategorySpinnner()

        val addSetBtn: Button = findViewById(R.id.addSetBtn)
        addSetBtn.setOnClickListener{
            addNewSet()
        }
        val finalizeExerciseBtn: FloatingActionButton = findViewById(R.id.finalizeExerciseBtn)
        val cancelExerciseBtn: FloatingActionButton = findViewById(R.id.cancelExerciseBtn)
        finalizeExerciseBtn.setOnClickListener{
            finalizeExercise()
        }
        cancelExerciseBtn.setOnClickListener{
            cancelExercise()
        }
    }

    override fun onResume() {
        super.onResume()
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent) {
        var exerciseToEdit: Exercise? = Exercise("haha","haha",MuscleCategory.ABS, listOf())
        if (intent.hasExtra("isEdit")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isEditing = intent.getBooleanExtra("isEdit", false)
                if (isEditing){
                    exerciseToEdit = intent.getParcelableExtra("exerciseToEdit", Exercise::class.java)
                }
            } else {
                @Suppress("DEPRECATION")
                isEditing = intent.getBooleanExtra("isEdit", false)
                if (isEditing){
                    @Suppress("DEPRECATION")
                    exerciseToEdit = intent.getParcelableExtra("exerciseToEdit")
                }
            }
        }
        if (isEditing && exerciseToEdit != null) {
            exerciseBeingEditedId = exerciseToEdit.exId
            exerciseTitleView.text = exerciseToEdit.name
            exerciseDescriptionView.text = exerciseToEdit.description

            val spinner: Spinner = findViewById(R.id.muscleCategorySpinner)
            val categoryName = exerciseToEdit.category.name.lowercase().replaceFirstChar { it.uppercase() }
            val position = (spinner.adapter as ArrayAdapter<String>).getPosition(categoryName)
            if (position >= 0) {
                spinner.setSelection(position)
            }
            setsList.clear()
            setsList.addAll(exerciseToEdit.setList)
            Log.d("SETS", setsList.toString())
            setListAdapter.notifyDataSetChanged()
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

    private fun finalizeExercise(){
        if (exerciseTitleView.text.toString().isBlank() or setsList.isEmpty()){
            Toast.makeText(this,"Make sure to enter a title and at least one set!", Toast.LENGTH_SHORT).show()
            return
        }

        val newExercise: Exercise = Exercise(exerciseTitleView.text.toString(),
            exerciseDescriptionView.text.toString(),
            selectedCategory,
            setsList,
            exId = exerciseBeingEditedId?: 0
            )
        val intent = Intent(this,MainActivity::class.java)
        intent.putExtra("exercise",newExercise)
        intent.putExtra("isEdit",isEditing)
        startActivity(intent)
    }

    private fun cancelExercise(){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }

    private fun initializeCategorySpinnner(){
        val categoryEnum: MuscleCategory
        val spinner: Spinner = findViewById(R.id.muscleCategorySpinner)
        val categoryValues = MuscleCategory.values()
        val categoryStrings = categoryValues.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoryStrings) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.WHITE)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(Color.WHITE)
                view.setBackgroundColor(Color.parseColor("#121212"))
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = MuscleCategory.valueOf(categoryStrings[position].uppercase())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        spinner.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
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