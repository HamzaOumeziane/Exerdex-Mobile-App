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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainer
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.exerdex.adaptors.DoneListAdaptor
import ca.qc.bdeb.c5gm.exerdex.adaptors.ExerciseListAdaptor
import ca.qc.bdeb.c5gm.exerdex.adaptors.ExerciseRawListAdaptor
import ca.qc.bdeb.c5gm.exerdex.data.Exercise
import ca.qc.bdeb.c5gm.exerdex.data.ExerciseRaw
import ca.qc.bdeb.c5gm.exerdex.data.Workout
import ca.qc.bdeb.c5gm.exerdex.fragments.ActiveWorkoutManagementFragment
import ca.qc.bdeb.c5gm.exerdex.fragments.ExerciesDoneFragment
import ca.qc.bdeb.c5gm.exerdex.fragments.ExercisePopUp
import ca.qc.bdeb.c5gm.exerdex.fragments.ExercisesToDoFragment
import ca.qc.bdeb.c5gm.exerdex.room.ExerciseDatabase
import ca.qc.bdeb.c5gm.exerdex.viewmodels.ActiveWorkoutViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date


class MainActivity : AppCompatActivity() {

    private var isAuthenticated = false
    private var currentUserId: String? = null
    private val AWViewModel: ActiveWorkoutViewModel by viewModels()
    lateinit var roomDatabase: ExerciseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        roomDatabase = ExerciseDatabase.getExerciseDatabase(applicationContext)

        if (isAuthenticated) {
            showMainFragments()
            currentUserId?.let { userId ->
                lifecycleScope.launch(Dispatchers.IO) {
                    reloadDataFromDatabase(userId)
                }
            }

        } else {
            showAuthenticationFragment()
        }

    }

    fun onLoginSuccessful(userId: String) {
        isAuthenticated = true
        currentUserId = userId
        Log.d("CurrentUser", "User logged in with ID: $currentUserId")
        runOnUiThread {
            showMainFragments()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            reloadDataFromDatabase(userId)
        }
    }

    private fun showAuthenticationFragment() {
        findViewById<FragmentContainerView>(R.id.fragmentContainerAuthentication).visibility = View.VISIBLE

        findViewById<FragmentContainerView>(R.id.AWExercisesToDoFragment).visibility = View.GONE
        findViewById<FragmentContainerView>(R.id.AWExercisesDoneFragment).visibility = View.GONE
        findViewById<FragmentContainerView>(R.id.AWManagementFragment).visibility = View.GONE
        findViewById<FragmentContainerView>(R.id.popupFragment).visibility = View.GONE
        findViewById<Toolbar>(R.id.toolbar).visibility = View.GONE
    }

    private fun showMainFragments() {
        // source : https://stackoverflow.com/questions/46551228/how-to-pass-and-get-value-from-fragment-and-activity
        val exercisesToDoFragment = ExercisesToDoFragment().apply {
            arguments = Bundle().apply {
                putString("currentUserId", currentUserId)
            }
        }
        val exercisesDoneFragment = ExerciesDoneFragment().apply {
            arguments = Bundle().apply {
                putString("currentUserId", currentUserId)
            }
        }
        val workoutManagementFragment = ActiveWorkoutManagementFragment().apply {
            arguments = Bundle().apply {
                putString("currentUserId", currentUserId)
            }
        }

        Log.d("MainActivity", "currentUserId dans showMainFragments: $currentUserId")
        findViewById<FragmentContainerView>(R.id.fragmentContainerAuthentication).visibility = View.GONE

        supportFragmentManager.beginTransaction()
            .replace(R.id.AWExercisesToDoFragment, exercisesToDoFragment)
            .replace(R.id.AWExercisesDoneFragment, exercisesDoneFragment)
            .replace(R.id.AWManagementFragment, workoutManagementFragment)
            .commit()

        findViewById<FragmentContainerView>(R.id.AWExercisesToDoFragment).visibility = View.VISIBLE
        findViewById<FragmentContainerView>(R.id.AWExercisesDoneFragment).visibility = View.VISIBLE
        findViewById<FragmentContainerView>(R.id.AWManagementFragment).visibility = View.VISIBLE


        findViewById<Toolbar>(R.id.toolbar).visibility = View.VISIBLE
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
                        roomDatabase.exerciseDao().updateAll(it)
                        // Reload data after update
                        currentUserId?.let { it1 -> reloadDataFromDatabase(it1) }
                    }
                } else {
                    currentUserId?.let { userId ->
                        it.userId = userId // Associer l'exercice à l'utilisateur actuel
                        lifecycleScope.launch(Dispatchers.IO) {
                            roomDatabase.exerciseDao().insertAll(it)
                            reloadDataFromDatabase(userId)
                        }
                    }
                }
                intent.removeExtra("exercise") // Pour pas que les exercises se rajoutent lors
                                                     // du changement d'orientation.
                Toast.makeText(this, "Exercise ${actionDone}: ${it.exerciseRawData.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                val intent = Intent(this, ArchivedWorkouts::class.java)
                intent.putExtra("currentUserId", currentUserId)
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


    fun addExercise(isEdit: Boolean, exerciseToEdit: Exercise?, exerciseRaw: ExerciseRaw?) {
        val intent = Intent(this, AddEditExerciseActivity::class.java)
        intent.putExtra("isEdit",isEdit)
        intent.putExtra("currentUserId", currentUserId)
        if (isEdit){
            intent.putExtra("exerciseToEdit",exerciseToEdit)
        }
        if (exerciseRaw != null){
            intent.putExtra("exerciseRaw",exerciseRaw)
        }
        startActivity(intent)
    }

    fun showAddExerciseDialog(){
        val dialogView = layoutInflater.inflate(R.layout.add_exercise_dialog, null)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.existingExoRawsRecycler)
        val createCustomBtn = dialogView.findViewById<Button>(R.id.createNewExoRawBtn)
        lifecycleScope.launch(Dispatchers.IO) {
            val exercisesRaw = roomDatabase.exerciseDao().loadAllExerciseRaw()
            withContext(Dispatchers.Main){
                val adaptor = ExerciseRawListAdaptor(
                    applicationContext,
                    this@MainActivity,
                    exercisesRaw
                ) { isEdit: Boolean, exerciseToEdit: Exercise?, exerciseRaw: ExerciseRaw?
                        ->
                        addExercise(isEdit, exerciseToEdit, exerciseRaw)
                }
                recyclerView.adapter = adaptor
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setView(dialogView)
                val dialog = builder.create()
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()
            }
        }
        createCustomBtn.setOnClickListener {
            val intent = Intent(this, CreateNewExerciseRaw::class.java)
            intent.putExtra("currentUserId", currentUserId)
            startActivity(intent)
        }
    }


    suspend fun reloadDataFromDatabase(userId: String) {
        Log.d("Reload", "UserID from loading : ${userId}")
        val exercisesToDoFromDB = roomDatabase.exerciseDao().loadExerciseByDone(false, userId)
        val exercisesDoneFromDB = roomDatabase.exerciseDao().loadExerciseByDone(true, userId)
        Log.d("databaseLOGS","Table, on load undone: $exercisesToDoFromDB")
        Log.d("databaseLOGS","Table, on load done: $exercisesDoneFromDB")
        runOnUiThread {
            AWViewModel.updateBothLists(exercisesToDoFromDB, exercisesDoneFromDB)
        }
    }

}