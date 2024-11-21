package ca.qc.bdeb.c5gm.exerdex

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import ca.qc.bdeb.c5gm.exerdex.adaptors.DoneListAdaptor
import ca.qc.bdeb.c5gm.exerdex.adaptors.ExerciseListAdaptor
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.data.Workout
import ca.qc.bdeb.c5gm.exerdex.room.ExerciseDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date


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
                Toast.makeText(this,getString(R.string.toast_new_workout_missing_info_error),Toast.LENGTH_SHORT).show()
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
            setsStringBuilder.append(", ${getString(R.string.heaviest_set)}: ${heaviestSet.weight}x${heaviestSet.reps}")
            setList.add(setsStringBuilder.toString())
            val totalVolume = exercise.setList.sumOf { (it.weight * it.reps).toInt() }
            totalWorkoutVolume += totalVolume
        }

        val workout: Workout = Workout(newWorkoutName.text.toString(), Date(System.currentTimeMillis()), setList
            ,totalWorkoutVolume)

        runOnUiThread {
            newWorkoutName.text = ""
        }
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
            R.id.action_about -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.info_title)
                    .setMessage(R.string.info_message)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                builder.create().show()
                true
            }
            R.id.action_settings -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.info_title)
                    .setMessage(R.string.settings_message)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                builder.create().show()
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

    fun setUpPopup(exerciseToDisplay:Exercise){
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