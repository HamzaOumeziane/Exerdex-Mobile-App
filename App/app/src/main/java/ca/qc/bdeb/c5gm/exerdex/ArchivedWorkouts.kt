package ca.qc.bdeb.c5gm.exerdex

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.adaptors.WorkoutListAdaptor
import ca.qc.bdeb.c5gm.exerdex.data.Workout
import ca.qc.bdeb.c5gm.exerdex.room.ExerciseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArchivedWorkouts : AppCompatActivity() {
    var currentUserId: String? = null
    lateinit var recyclerView: RecyclerView
    lateinit var adaptor: WorkoutListAdaptor
    var workoutsList: MutableList<Workout> = mutableListOf()
    lateinit var database: ExerciseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_archived_workouts)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentUserId = intent.getStringExtra("currentUserId")
        Log.d("ArchivedWorkouts", "User logged in with ID: $currentUserId")
        database = ExerciseDatabase.getExerciseDatabase(applicationContext)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_keyboard_return_24_wh)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        recyclerView = findViewById(R.id.archivedWorkouts)
        recyclerView.layoutManager = LinearLayoutManager(this)  // Add this line
        adaptor = WorkoutListAdaptor(applicationContext, this, workoutsList)
        recyclerView.adapter = adaptor
    }

    override fun onResume() {
        super.onResume()
        getLatestData()
    }

    fun getLatestData(){
        lifecycleScope.launch(Dispatchers.IO){
            val workoutsFromDB = database.workoutDao().loadWorkoutsByUserId(currentUserId ?: "" )
            Log.d("databaseLOGS","Table, workouts: "+workoutsFromDB)
            workoutsList.clear()
            workoutsList.addAll(workoutsFromDB)
            runOnUiThread {
                adaptor.notifyDataSetChanged()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

