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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArchivedWorkouts : AppCompatActivity() {

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
            val workoutsFromDB = database.workoutDao().loadAllWorkouts()
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
class ItemWorkoutHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val layout: ConstraintLayout
    val workoutName: TextView
    val workoutDate: TextView
    val workoutVolume: TextView
    val workoutSummary: TextView
    val deleteBtn: ImageView

    init {
        layout = itemView as ConstraintLayout
        workoutName = itemView.findViewById(R.id.workoutNameTxt)
        workoutDate = itemView.findViewById(R.id.workoutDateTxt)
        workoutVolume = itemView.findViewById(R.id.workoutVolumeTxt)
        workoutSummary = itemView.findViewById(R.id.exercisesSummaryTxt)
        deleteBtn = itemView.findViewById(R.id.deleteImgBtn)
    }
}
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
