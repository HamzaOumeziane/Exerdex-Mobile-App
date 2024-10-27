package ca.qc.bdeb.c5gm.exerdex

import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
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
import java.sql.Date

private var currentEdited: Int = -1

class ItemExerciseHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val layout: ConstraintLayout
    val exercise: TextView
    val category: TextView
    val important: ImageView
    val sets: TextView
    val check: ImageView
    val edit: ImageView
    val cancel: ImageView

    init {
        layout = itemView as ConstraintLayout
        exercise = itemView.findViewById(R.id.exerciseTextView)
        category = itemView.findViewById(R.id.categoryTextView)
        important = itemView.findViewById(R.id.importantImageView)
        sets = itemView.findViewById(R.id.setsTextView)
        check = itemView.findViewById(R.id.checkView)
        edit = itemView.findViewById(R.id.editView)
        cancel = itemView.findViewById(R.id.deleteView)
    }
}

class ItemDoneHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val exercise: TextView
    val comeback: ImageView

    init{
        exercise = itemView.findViewById(R.id.doneTextView)
        comeback = itemView.findViewById(R.id.comebackImageView)
    }
}

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
        exercisesList.sortBy { it.category }

        Log.d("Sorting...",exercisesList.toString())

        val item = exercisesList[position]

        if(position == 0 || exercisesList[position -1].category != item.category){
            holder.category.visibility = View.VISIBLE
            holder.category.text = item.category.name.replaceFirstChar { it.uppercase() }
        }else{
            holder.category.visibility = View.GONE
        }

        holder.exercise.text = item.name

        if(!item.isImportant){
            holder.important.visibility = View.GONE
        }else{
            holder.important.setImageResource(R.drawable.baseline_star_24)
        }
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
                activity.reloadDataFromDatabase()
            }
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
    lateinit var newWorkoutName: TextView
    lateinit var adapterDone: DoneListAdaptor
    lateinit var adapterExercise: ExerciseListAdaptor
    private val exercisesList: MutableList<Exercise> = mutableListOf()
    private val doneList: MutableList<Exercise> = mutableListOf()

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

        newWorkoutName = findViewById(R.id.newWorkoutName)

        popupLayer = findViewById(R.id.popupLayer)
        val closePopupBtn: ImageView = findViewById(R.id.closePopupImg)
        closePopupBtn.setOnClickListener {
            closePopup()
        }

        deleteDone = findViewById(R.id.archiveButton)
        deleteDone.setOnClickListener {
            if (doneList.isEmpty() || newWorkoutName.text.toString().length < 2){
                Toast.makeText(this,"Enter a workout name and at least one done exercise",Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch(Dispatchers.IO){
                    addDoneToArchive()
                    database.exerciseDao().deleteAllExercisesDone()
                }
            }
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

    private suspend fun addDoneToArchive(){
        val setList: MutableList<String> = mutableListOf()
        var totalWorkoutVolume: Int = 0
        doneList.forEach { exercise: Exercise ->
            Log.d("exerciseAddingLogs","Adding Exercise: ${exercise}")
            val setsStringBuilder = StringBuilder()
            setsStringBuilder.append(exercise.name)
            val heaviestSet = exercise.setList.maxBy { it.weight }
            setsStringBuilder.append(", heaviest set: ${heaviestSet.weight}x${heaviestSet.reps}")
            setList.add(setsStringBuilder.toString())
            val totalVolume = exercise.setList.sumOf { (it.weight * it.reps).toInt() }
            totalWorkoutVolume += totalVolume
        }

        val workout:Workout = Workout(newWorkoutName.text.toString(), Date(System.currentTimeMillis()), setList
            ,totalWorkoutVolume)
        newWorkoutName.text = ""
        Log.d("exerciseAddingLogs","Workout Added: ${workout}")
        database.workoutDao().insertAll(workout)
        runOnUiThread {
            doneList.clear()
            adapterDone.notifyDataSetChanged()
        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                val intent = Intent(this, ArchivedWorkouts::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
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

    suspend fun reloadDataFromDatabase() {
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
        if(exerciseToDisplay.description.isNullOrEmpty()){
            descView.visibility = View.GONE
        }else{
            descView.text = exerciseToDisplay.description
        }

        typeView.text = exerciseToDisplay.category.toString() + " Exercise"


        val setsStringBuilder = StringBuilder()

        exerciseToDisplay.setList.forEachIndexed { index, item ->
            setsStringBuilder.append(item.toString())
            // Pour ne pas avoir de new line au dernier element
            if (index < exerciseToDisplay.setList.size - 1) {
                setsStringBuilder.append("\n")
            }
        }
        setsView.text = setsStringBuilder.toString()

        if(!exerciseToDisplay.imageUri.isNullOrEmpty()){
            val uri = Uri.parse(exerciseToDisplay.imageUri)
            exoImage.setImageURI(uri)
        }


    }
    private fun closePopup(){
        popupLayer.visibility = View.GONE
    }
}