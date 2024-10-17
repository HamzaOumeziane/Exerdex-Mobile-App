package ca.qc.bdeb.c5gm.exerdex

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private var currentEdited: Int = -1

lateinit var adapterDone: DoneListAdaptor
lateinit var adapterExercise: ExerciseListAdaptor

class ItemExerciseHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val layout: ConstraintLayout
    val exercise: TextView
    val sets: TextView
    val check: ImageView
    val edit: ImageView
    val cancel: ImageView

    init {
        layout = itemView as ConstraintLayout
        exercise = itemView.findViewById(R.id.exerciseTextView)
        sets = itemView.findViewById(R.id.setsTextView)
        check = itemView.findViewById(R.id.checkView)
        edit = itemView.findViewById(R.id.editView)
        cancel = itemView.findViewById(R.id.deleteView)
    }
}

public class ItemDoneHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val exercise: TextView
    val comeback: ImageView

    init{
        exercise = itemView.findViewById(R.id.doneTextView)
        comeback = itemView.findViewById(R.id.comebackImageView)
    }
}

public class DoneListAdaptor(
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
            }
            exercisesList.add(item)
            adapterExercise.notifyItemInserted(exercisesList.size - 1)
            doneList.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
        }
    }
}

class ExerciseListAdaptor(
    val ctx: Context,
    val activity: MainActivity,
    val database: ExerciseDatabase,
    val exercisesList: MutableList<Exercise>,
    val doneList: MutableList<Exercise>,
    private val editExercise: (isEdit: Boolean, exerciseToEdit: Exercise?) -> Unit,
) : RecyclerView.Adapter<ItemExerciseHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemExerciseHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.exercise_item, parent, false)
        return ItemExerciseHolder(view)
    }

    override fun getItemCount(): Int {
        return exercisesList.size
    }

    override fun onBindViewHolder(holder: ItemExerciseHolder, position: Int) {
        val item = exercisesList[position]

        holder.exercise.text = item.name
        /*
        * Source : https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/join-to-string.html
        * */
        if (item.setList.size > 3){
            holder.sets.text = (item.setList.joinToString("\n", "", "", 3) { it.toString() }) + " ${item.setList.size - 3} more"
        }else{
            holder.sets.text = item.setList.joinToString("\n") { it.toString() }
        }
        holder.check.setImageResource(R.drawable.baseline_check_circle_24)
        holder.edit.setImageResource(R.drawable.baseline_edit_24)
        holder.cancel.setImageResource(R.drawable.baseline_cancel_24)



        holder.check.setOnClickListener {
            item.isDone=true
            activity.lifecycleScope.launch(Dispatchers.IO){
                database.exerciseDao().updateAll(item)
            }
            doneList.add(item)
            adapterDone.notifyItemInserted(doneList.size - 1)
            exercisesList.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
        }

        holder.edit.setOnClickListener {
            currentEdited = holder.adapterPosition
            editExercise(true, item)
        }

        holder.cancel.setOnClickListener {
            activity.lifecycleScope.launch(Dispatchers.IO){
                database.exerciseDao().delete(item)
            }
            exercisesList.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
        }
        holder.itemView.setOnClickListener{
            activity.setUpPopup(item)
        }
    }
}

class MainActivity : AppCompatActivity() {

    lateinit var recyclerViewExercise: RecyclerView
    lateinit var recyclerViewDone: RecyclerView
    lateinit var deleteDone: Button
    lateinit var database: ExerciseDatabase
    lateinit var popupLayer: ConstraintLayout
    private val exercisesList: MutableList<Exercise> = mutableListOf()
    private val doneList: MutableList<Exercise> = mutableListOf()


    //val test:Exercise = Exercise(name = "Test", category = MuscleCategory.ABS, setList = listOf())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = ExerciseDatabase.getExerciseDatabase(applicationContext)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val floatingBtn: FloatingActionButton = findViewById(R.id.floatingActionBtn)
        floatingBtn.setOnClickListener {
            addExercise(false, null)
        }

        popupLayer = findViewById(R.id.popupLayer)
        val closePopupBtn: ImageView = findViewById(R.id.closePopupImg)
        closePopupBtn.setOnClickListener {
            closePopup()
        }

        deleteDone = findViewById(R.id.deleteButton)
        deleteDone.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO){
                database.exerciseDao().deleteAllExercisesDone()
            }
            doneList.clear()
            adapterDone.notifyDataSetChanged()
        }

        recyclerViewExercise = findViewById(R.id.recyclerView)
        recyclerViewDone = findViewById(R.id.doneRecyclerView)
        adapterExercise = ExerciseListAdaptor(applicationContext, this, database,
            exercisesList, doneList) { isEdit, exercise ->
            addExercise(isEdit, exercise)
        }
        adapterDone = DoneListAdaptor(applicationContext, this, database, exercisesList,
            doneList)
        recyclerViewExercise.adapter = adapterExercise
        recyclerViewDone.adapter = adapterDone

        lifecycleScope.launch(Dispatchers.IO){
            val exercisesToDoFromDB = database.exerciseDao().loadExerciseByDone(false)
            val exercisesDoneFromDB = database.exerciseDao().loadExerciseByDone(true)
            Log.d("databaseLOGS","Table, on load undone: "+exercisesToDoFromDB)
            Log.d("databaseLOGS","Table, on load done: "+exercisesDoneFromDB)


            exercisesList.clear()
            doneList.clear()
            exercisesList.addAll(exercisesToDoFromDB)
            doneList.addAll(exercisesDoneFromDB)
            runOnUiThread {
                adapterExercise.notifyDataSetChanged()
                adapterDone.notifyDataSetChanged()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent) {
        if (intent.hasExtra("exercise")) {
            var newExercise:Exercise?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                newExercise = intent.getParcelableExtra("exercise", Exercise::class.java)
            } else {
                @Suppress("DEPRECATION")
                newExercise = intent.getParcelableExtra("exercise")
            }
            val isEdit: Boolean = intent.getBooleanExtra("isEdit",false)
            newExercise?.let {
                var actionDone: String = "Added"
                if (isEdit){
                    actionDone = "Edited"
                    lifecycleScope.launch(Dispatchers.IO) {
                        database.exerciseDao().updateAll(it)
                        // Reload data after update
                        reloadDataFromDatabase()
                    }
                } else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        database.exerciseDao().insertAll(it)
                        // Reload data after insertion
                        reloadDataFromDatabase()
                    }
                }
                intent.removeExtra("exercise") // Pour pas que les exercises se rajoutent lors
                                                     // du changement d'orientation.
                Toast.makeText(this, "Exercise ${actionDone}: ${it.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    fun addExercise(isEdit: Boolean, exerciseToEdit: Exercise?) {
        val intent = Intent(this, AddEditExerciseActivity::class.java)
        intent.putExtra("isEdit",isEdit)
        if (isEdit){
            intent.putExtra("exerciseToEdit",exerciseToEdit)
        }
        startActivity(intent)
    }

    private suspend fun reloadDataFromDatabase() {
        val exercisesToDoFromDB = database.exerciseDao().loadExerciseByDone(false)
        val exercisesDoneFromDB = database.exerciseDao().loadExerciseByDone(true)
        Log.d("databaseLOGS","Table, on load undone: $exercisesToDoFromDB")
        Log.d("databaseLOGS","Table, on load done: $exercisesDoneFromDB")
        exercisesList.clear()
        doneList.clear()
        exercisesList.addAll(exercisesToDoFromDB)
        doneList.addAll(exercisesDoneFromDB)
        withContext(Dispatchers.Main) {
            adapterExercise.notifyDataSetChanged()
            adapterDone.notifyDataSetChanged()
        }
    }

    public fun setUpPopup(exerciseToDisplay:Exercise){
        popupLayer.visibility = View.VISIBLE
        val titleView: TextView = findViewById(R.id.popupExoTitle)
        val typeView: TextView = findViewById(R.id.popupExoType)
        val descView: TextView = findViewById(R.id.popupExoDesc)
        val setsView: TextView = findViewById(R.id.popupExoSets)
        val exoImage: ImageView = findViewById(R.id.popupExoImg)
        titleView.text = exerciseToDisplay.name
        descView.text = exerciseToDisplay.description
        typeView.text = exerciseToDisplay.category.toString() + " Exercise"

        val setsStringBuilder = StringBuilder()

        exerciseToDisplay.setList.forEach{
            setsStringBuilder.append(it.toString())
        }

        setsView.text = setsStringBuilder.toString()


    }
    private fun closePopup(){
        popupLayer.visibility = View.GONE
    }
}



/*
private fun setUpDone(): MutableList<Exercise>{
    return mutableListOf(
        Exercise(
            name = "Pull ups",
            description = "An exercise for back and biceps",
            category = MuscleCategory.BACK,
            setList = listOf(
                Set(setOrder = 1, weight = 135f, reps = 10),
                Set(setOrder = 2, weight = 145f, reps = 8),
                Set(setOrder = 3, weight = 155f, reps = 6),
                Set(setOrder = 4, weight = 185f, reps = 10),
                Set(setOrder = 5, weight = 205f, reps = 8),
                Set(setOrder = 6, weight = 225f, reps = 6)
            )
        )
    )
}

private fun setUpExercises(): MutableList<Exercise> {
    return mutableListOf(
        Exercise(
            name = "Bench Press",
            description = "A compound exercise targeting the chest, triceps, and shoulders.",
            category = MuscleCategory.CHEST,
            setList = listOf(
                Set(setOrder = 1, weight = 135f, reps = 10),
                Set(setOrder = 2, weight = 145f, reps = 8),
                Set(setOrder = 3, weight = 155f, reps = 6),
                Set(setOrder = 4, weight = 185f, reps = 10),
                Set(setOrder = 5, weight = 205f, reps = 8),
                Set(setOrder = 6, weight = 225f, reps = 6)
            )
        ),
        Exercise(
            name = "Squat",
            description = "A lower body compound exercise targeting the quadriceps, hamstrings, and glutes.",
            category = MuscleCategory.QUADS,
            setList = listOf(
                Set(setOrder = 1, weight = 185f, reps = 10),
                Set(setOrder = 2, weight = 205f, reps = 8),
                Set(setOrder = 3, weight = 225f, reps = 6)
            )
        ),
        Exercise(
            name = "Deadlift",
            description = "A compound movement working the entire posterior chain.",
            category = MuscleCategory.BACK,
            setList = listOf(
                Set(setOrder = 1, weight = 225f, reps = 8),
                Set(setOrder = 2, weight = 245f, reps = 6),
                Set(setOrder = 3, weight = 265f, reps = 4)
            )
        ),
        Exercise(
            name = "Overhead Press",
            description = "An upper body exercise focusing on the shoulders and triceps.",
            category = MuscleCategory.SHOULDERS,
            setList = listOf(
                Set(setOrder = 1, weight = 95f, reps = 10),
                Set(setOrder = 2, weight = 105f, reps = 8),
                Set(setOrder = 3, weight = 115f, reps = 6)
            )
        ),
        Exercise(
            name = "Barbell Row",
            description = "An exercise to target the back, focusing on the lats and rhomboids.",
            category = MuscleCategory.BACK,
            setList = listOf(
                Set(setOrder = 1, weight = 135f, reps = 10),
                Set(setOrder = 2, weight = 145f, reps = 8),
                Set(setOrder = 3, weight = 155f, reps = 6)
            )
        ),
        Exercise(
            name = "Bicep Curl",
            description = "An isolation exercise for the biceps.",
            category = MuscleCategory.BICEPS,
            setList = listOf(
                Set(setOrder = 1, weight = 25f, reps = 12),
                Set(setOrder = 2, weight = 30f, reps = 10),
                Set(setOrder = 3, weight = 35f, reps = 8),
                Set(setOrder = 4, weight = 30f, reps = 10),
                Set(setOrder = 5, weight = 35f, reps = 8)
            )
        ),
        Exercise(
            name = "Tricep Pushdown",
            description = "An isolation exercise for the triceps using a cable machine.",
            category = MuscleCategory.TRICEPS,
            setList = listOf(
                Set(setOrder = 1, weight = 40f, reps = 12),
                Set(setOrder = 2, weight = 45f, reps = 10),
                Set(setOrder = 3, weight = 50f, reps = 8)
            )
        )
    )
}
*/
